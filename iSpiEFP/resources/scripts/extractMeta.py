from enum import IntEnum
import math
import json
import re
import sys


class CurrentField(IntEnum):
    '''
    This class only serves the purpose of being used as an enum
    for the types of sections in a .efp file.
    '''

    COORDINATES = 0
    MONOPOLES = 1
    DIPOLES = 2
    QUADRUPOLES = 3
    OCTUPOLES = 4
    POLARIZABLE_PTS = 5
    DYN_POLARIZABLE_PTS = 6
    PROJECTION_BASIS = 7
    MULTIPLICITY = 8
    PROJECTION_WAVEFUNCTION = 9
    FOCK_MATRIX_ELEMENTS = 10
    LMO_CENTROIDS = 11
    CANONVEC = 12
    CANONFOK = 13
    SCREEN2 = 14
    SCREEN = 15
    INITIAL = 16
    STOP = 17

class Coordinate_Line:
    '''
    Class containing all the fields of a line in the coordinate section
    of the .efp file for the purposes of formatting the json output
    
    ...

    Attributes
    ----------
    atomID : str
        String identifier for the atom in this line
    x : float
        the x coordinate of the (x,y,z) coordinate tuple
    y : float
        the y coordinate of the (x,y,z) coordinate tuple
    z : float
        the z cooedinate of the (x,y,z) coordinate tuple
    mass : float
        the mass of the atom specified by the atomID
    charge : float
        the charge of the atom specified by the atomID
    
    ...

    Methods
    -------
    getJSONString() -> str
        returns a string of the JSON representation of the Coordinate_Line instance
    '''

    def __init__(self, line_string):
        tokens = line_string.split()
        self.atomID = tokens[0]
        self.x = float (tokens[1])
        self.y = float (tokens[2])
        self.z = float (tokens[3])
        self.mass = float (tokens[4])
        self.charge = float (tokens[5])

    def getJSONString(self) -> str:
        return json.dumps(self.__dict__)
       
        


class Metadata:
    '''
    A class used to determine what information is stored in a .efp file
    
    ...

    Attributes
    ----------
    bitmap : int
        bitmap where the bit in the nth position refers to CurrentField.xxx = n
    state : CurrentField
        enum that represents the current section of the .efp file we are examining
    fromFile : str
        filename of the file we are extracting this meta data from
    scftype : str
        The self-consistent field method used in the generation of the efp parameters
    basisSet : str
        The basis set that the efp parameters were generated in
    coordinates : str[]
        List of strings where each element is the raw text from each line after COORDINATES
        but before STOP. Necssary to explicitly state for rendering in GUI

    Methods
    -------
    checkField(str[] tokenList)
        Searches all of the strings in tokenList to determine what section of the file
        the script is currently examining
    toJSON()
        Returns a JSON notation of the meta-data of the form:

        {
            fromFile: <fileName.efp>,
            fragmentName: <fragment_name>,
            scf_type: <scf_type>,
            basisSet: <basis_set>,
            coordinates: [,,,],
            bitmap: <integer>
        }
    '''

    def __init__(self, fileName):
        self.bitmap = 0
        self.state = CurrentField.INITIAL
        self.fromFile = fileName.encode('unicode_escape')
        self.scftype = ""
        self.basisSet = ""
        self.coordinates = []
    
        lines = []
        with open(fileName, "r") as efpFile:
            lines = efpFile.readlines()
        efpFile.close()
        for line in lines: 
            tokens = line.split() #Split each line of the file by whitespace and put each token in a list
            if self.state == CurrentField.INITIAL:
                for token in tokens:
                    if (token[0] == "$"): # $ Denotes that the rest of the string is the fragment name
                        self.fragmentName = token[1:]
                    elif (token[:7] == "SCFTYP="): #Rest of token contains the type of scf
                        self.scftype = token[7:]
                    elif (token[:4] == "SET="): #Rest of token contains the basis set
                        self.basisSet = token[4:]
                    elif (token == "COORDINATES"):
                        self.checkField(tokens)
                        break
            elif(len(tokens) > 0 and tokens[0] == "STOP"):  #The next line will be a keyword denoting the title of a section
                self.state = CurrentField.STOP
                continue
            elif (self.state == CurrentField.STOP): #This line will be a keyword denoting the title of a section
                self.checkField(tokens)
            elif (self.state == CurrentField.COORDINATES): #The current section is coordinates, save the text of all lines
                self.coordinates.append(Coordinate_Line(line).getJSONString().replace("\\", ""))
            elif(len(tokens) > 0 and (tokens[0] == "FOCK" or tokens[0] == "LMO" or tokens[0] =="CANONFOK")):
                self.checkField(tokens)

    def checkField(self, tokenList):
        """
        Searches all of the strings in tokenList to determine what section of the file
        the script is currently examining

        Parameters
        ----------
        tokenList: str[]
            A list of strings which are eached checked to see if they match the title
            of a section of the .efp file. If they do, then the state of the file is
            changed.
        """

        for token in tokenList:
            if (token == "COORDINATES"): 
                self.state = CurrentField.COORDINATES
                break
            elif (token == "MONOPOLES"):
                self.state = CurrentField.MONOPOLES
                break
            elif (token == "DIPOLES"):
                self.state = CurrentField.DIPOLES
                break
            elif (token == "QUADRUPOLES"):
                self.state = CurrentField.QUADRUPOLES
                break
            elif (token == "OCTUPOLES"):
                self.state = CurrentField.OCTUPOLES
                break
            elif (token == "POLARIZABLE"):
                self.state = CurrentField.POLARIZABLE_PTS
                break
            elif (token == "DYNAMIC"):
                self.state = CurrentField.DYN_POLARIZABLE_PTS
                break
            elif (token == "BASIS"):
                self.state = CurrentField.PROJECTION_BASIS
                break
            elif (token == "MULTIPLICITY"):
                self.state = CurrentField.MULTIPLICITY
                break
            elif (token == "WAVEFUNCTION"):
                self.state = CurrentField.PROJECTION_WAVEFUNCTION
                break
            elif (token == "FOCK"):
                self.state = CurrentField.FOCK_MATRIX_ELEMENTS
                break
            elif (token == "LMO"):
                self.state = CurrentField.LMO_CENTROIDS
                break
            elif (token == "CANONVEC"):
                self.state = CurrentField.CANONVEC
                break
            elif (token == "CANONFOK"):
                self.state = CurrentField.CANONFOK
                break
            elif (token == "SCREEN2"):
                self.state = CurrentField.SCREEN2
                break
            elif (token == "SCREEN"):
                self.state = CurrentField.SCREEN
                break
        if (self.state != CurrentField.STOP and self.state != CurrentField.INITIAL):
            self.bitmap += math.pow(2, self.state)

    def toJSON(self) -> str:
        """
        Populates the JSON_Object with a JSON Object containing information on the meta-data of the form:
        
        {
            fromFile: <fileName.efp>,
            fragmentName: <fragment_name>,
            scf_type: <scf_type>,
            basisSet: <basis_set>,
            coordinates: [
                {atomID: <atomID>, x: <x>, y: <y>,z: <z>, mass: <mass>, charge: <charge>},
                ...,
            ],
            bitmap: <integer>
        }

        to a new file which will be the fragment name <fragment_name>.json
        """
        json_data = {
            "fromFile": self.fromFile, 
            "fragmentName": self.fragmentName.lower(), #Modifies fragment name to be in all lower case
            "scf_type": self.scftype,
            "basisSet": self.basisSet,
            "coordinates": self.coordinates,
            "bitmap": int(self.bitmap)
        }
        jsonString = json.dumps(json_data, indent=4).replace("\\", "")
        jsonString.replace("\"{", "{")
        jsonString.replace("}\"", "}")
        jsonString = re.sub("\"{", "{", jsonString)
        jsonString = re.sub("}\"", "}", jsonString)
        with open(sys.argv[2] + "/" + self.fragmentName.lower() + ".json", "w") as outFile:
            print(jsonString, file=outFile)

def main():
    meta = Metadata(sys.argv[1])
    meta.toJSON()

if __name__ == '__main__':
    main()