package org.ispiefp.app.metaData;

import com.google.gson.Gson;
import org.ispiefp.app.efpFileRetriever.GithubRequester;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

/* This class is essentially a wrapper for extracting all of the fields from a JSON String          */
public class MetaData {

    private String fromFile;            /* The file from which this metadata was extracted          */
    private String fragmentName;        /* The name of the fragment from the file                   */
    private String scf_type;            /* The type of self-consistent field method used            */
    private String basisSet;            /* The basisSet in which this calculations was performed    */
    private Coordinates[] coordinates;  /* A list of coordinates strings                            */
    private File efpFile;               /* The .efp file for this fragment. Only populated after
                                           selecting the fragment from file->Select Fragment        */
    /* Coordinate Object format:
            <atomId> <x_coord> <y_coord> <z_coord> <mass> <charge>

                Field       Type
                ------------------
                atomID:     String
                x:          double
                y:          double
                z:          double
                mass:       double
                charge:     double
                                                                                               */
    private int bitmap;         /* Integer from which fields present in original
                                                   file will be extracted                              */

    /**
     * Constructor for a MetaData object
     *
     * @param metaDataFilePath The path of the file containing the meta data JSON
     */
    public MetaData(String metaDataFilePath) {
        String metaDataFileString;      /* The content of the metDataFile as a JSON string or null if IOException */
        Gson gson = new Gson();         /* Instance of Gson used to parse the string to an object                 */
        try {
            metaDataFileString = new String(Files.readAllBytes(Paths.get(metaDataFilePath)));
        } catch (IOException e) {
            metaDataFileString = null;
        }

        if (metaDataFileString != null) {
            MetaData inferredClass = gson.fromJson(metaDataFileString, MetaData.class);
            this.fromFile = inferredClass.fromFile;
            this.fragmentName = inferredClass.fragmentName;
            this.scf_type = inferredClass.scf_type;
            this.basisSet = inferredClass.basisSet;
            this.coordinates = inferredClass.coordinates;
            this.bitmap = inferredClass.bitmap;
        }
        if (this.fromFile == null || this.fragmentName == null || this.scf_type == null
                || this.basisSet == null || this.coordinates.length == 0 || this.bitmap == 0) {
            System.err.println("Malformed meta data file");
        }
    }

    /**
     * Returns bitmap encoding of contained fields
     *
     * @return int value of bitmap
     */
    public int getBitmap() {
        return bitmap;
    }

    /**
     * Returns the basis set the parameter generation was performed in
     *
     * @return the basis set as a String
     */
    public String getBasisSet() {
        return basisSet;
    }

    /**
     * Returns the name of the fragment
     *
     * @return the fragment as a String
     */
    public String getFragmentName() {
        return fragmentName;
    }

    /**
     * Returns the file from which this meta data was extracted
     *
     * @return the file name as a String
     */
    public String getFromFile() {
        return fromFile;
    }

    /**
     * Returns the self-conistent field method used to generate these parameters
     *
     * @return the self-consistent field method as a String
     */
    public String getScf_type() {
        return scf_type;
    }

    /**
     * Returns the coordinates of every atom in the fragment as a String in the following representation:
     * <p>
     * <atomId> <x_coord> <y_coord> <z_coord> <mass> <charge>
     * <p>
     * Field       Type
     * ------------------
     * atomId:     String
     * x_coord:    double
     * y_coord:    double
     * z_coord:    double
     * mass:       double
     * charge:     double
     *
     * @return an array of Strings of size number of coordinates
     */
    public Coordinates[] getCoordinates() {
        return coordinates;
    }

    class Coordinates {
        String atomID;
        double x;
        double y;
        double z;
        double mass;
        double charge;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MetaData)) return false;
        for (int i = 0; i < coordinates.length; i++){
            try {
                if (!coordinates[i].atomID.equals(((MetaData) obj).coordinates[i].atomID) ||
                        coordinates[i].charge != ((MetaData) obj).coordinates[i].charge ||
                        coordinates[i].mass != ((MetaData) obj).coordinates[i].mass ||
                        coordinates[i].x != ((MetaData) obj).coordinates[i].x ||
                        coordinates[i].y != ((MetaData) obj).coordinates[i].y ||
                        coordinates[i].z != ((MetaData) obj).coordinates[i].z) return false;
            } catch(ArrayIndexOutOfBoundsException e){
                return false;
            }
        }
        return ((MetaData) obj).basisSet.equals(this.basisSet) &&
                ((MetaData) obj).bitmap == this.bitmap &&
                ((MetaData) obj).fragmentName.equals(this.fragmentName) &&
                ((MetaData) obj).scf_type.equals(this.scf_type);
    }

    /**
     * Returns true iff original efp file contained coordinates
     *
     * @return Returns true iff original efp file contained coordinates
     */
    public boolean containsCoordinates() {
        int bitmask = 1;    /* bitmask = 0000 0000 0000 0000 0000 0000 0000 0001 */
        return (bitmap & bitmask) == 1;
    }

    /**
     * Returns true iff original efp file contained monopoles
     *
     * @return true iff original efp file contained monopoles
     */
    public boolean containsMonopoles() {
        int bitmask = 2;    /* bitmask = 0000 0000 0000 0000 0000 0000 0000 0010 */
        return (bitmap & bitmask) == 2;
    }

    /**
     * Returns true iff original efp file contained dipoles
     *
     * @return true iff original efp file contained dipoles
     */
    public boolean containsDipoles() {
        int bitmask = 4;    /* bitmask = 0000 0000 0000 0000 0000 0000 0000 0100 */
        return (bitmap & bitmask) == 4;
    }

    /**
     * Returns true iff original efp file contained quadrupoles
     *
     * @return true iff original efp file contained quadrupoles
     */
    public boolean containsQuadrupoles() {
        int bitmask = 8;    /* bitmask = 0000 0000 0000 0000 0000 0000 0000 1000 */
        return (bitmap & bitmask) == 8;
    }

    /**
     * Returns true iff original efp file contained octupoles
     *
     * @return true iff original efp file contained octupoles
     */
    public boolean containsOctupoles() {
        int bitmask = 16;    /* bitmask = 0000 0000 0000 0000 0000 0000 0001 0000 */
        return (bitmap & bitmask) == 16;
    }

    /**
     * Returns true iff original efp file contained Polarizable Points
     *
     * @return true iff original efp file contained Polarizable Points
     */
    public boolean containsPolarizablePts() {
        int bitmask = 32;    /* bitmask = 0000 0000 0000 0000 0000 0000 0010 0000 */
        return (bitmap & bitmask) == 32;
    }

    /**
     * Returns true iff original efp file contained Dynamic Polarizable Points
     *
     * @return true iff original efp file contained Dynamic Polarizable Points
     */
    public boolean containsDynPolarizablePts() {
        int bitmask = 64;    /* bitmask = 0000 0000 0000 0000 0000 0000 0100 0000 */
        return (bitmap & bitmask) == 64;
    }

    /**
     * Returns true iff original efp file contained projection basis
     *
     * @return true iff original efp file contained projection basis
     */
    public boolean containsProjectionBasis() {
        int bitmask = 128;    /* bitmask = 0000 0000 0000 0000 0000 0000 1000 0000 */
        return (bitmap & bitmask) == 128;
    }

    /**
     * Returns true iff original efp file contained multiplicity
     *
     * @return true iff original efp file contained multiplicity
     */
    public boolean containsMultiplicity() {
        int bitmask = 256;    /* bitmask = 0000 0000 0000 0000 0000 0001 0000 0000 */
        return (bitmap & bitmask) == 256;
    }

    /**
     * Returns true iff original efp file contained projection wavefunction
     *
     * @return true iff original efp file contained projection wavefunction
     */
    public boolean containsProjectionWavefunction() {
        int bitmask = 512;    /* bitmask = 0000 0000 0000 0000 0000 0010 0000 0000 */
        return (bitmap & bitmask) == 512;
    }

    /**
     * Returns true iff original efp file contained Fock matrix elements
     *
     * @return true iff original efp file contained Fock matrix elements
     */
    public boolean containsFockMatrixElements() {
        int bitmask = 1024;    /* bitmask = 0000 0000 0000 0000 0000 0100 0000 0000 */
        return (bitmap & bitmask) == 1024;
    }

    /**
     * Returns true iff original efp file contained LMO Centroids
     *
     * @return true iff original efp file contained LMO Centroids
     */
    public boolean containsLMOCentroids() {
        int bitmask = 2048;    /* bitmask = 0000 0000 0000 0000 0000 1000 0000 0000 */
        return (bitmap & bitmask) == 2048;
    }

    /**
     * Returns true iff original efp file contained canonical vectors
     *
     * @return true iff original efp file contained canonical vectors
     */
    public boolean containsCanonVec() {
        int bitmask = 4096;    /* bitmask = 0000 0000 0000 0000 0001 0000 0000 0000 */
        return (bitmap & bitmask) == 4096;
    }

    /**
     * Returns true iff original efp file contained canonical Fock Matrix
     *
     * @return true iff original efp file contained canonical Fock Matrix
     */
    public boolean containsCanonFock() {
        int bitmask = 8192;    /* bitmask = 0000 0000 0000 0000 0010 0000 0000 0000 */
        return (bitmap & bitmask) == 8192;
    }

    /**
     * Returns true iff original efp file contained screen2
     *
     * @return true iff original efp file contained screen2
     */
    public boolean containsScreen2() {
        int bitmask = 16384;    /* bitmask = 0000 0000 0000 0000 0100 0000 0000 0000 */
        return (bitmap & bitmask) == 16384;
    }

    /**
     * Returns true iff original efp file contained screen
     *
     * @return true iff original efp file contained screen
     */
    public boolean containsScreen() {
        int bitmask = 32768;    /* bitmask = 0000 0000 0000 0000 1000 0000 0000 0000 */
        return (bitmap & bitmask) == 32768;
    }

    /**
     * Returns true iff original efp file contained all of the information for electrostatics
     * A file has all of the electrostatic information if it has monopoles, dipoles, quadrupoles, and octupoles
     *
     * @return true iff original efp file contained all of the information for electrostatics
     */
    public boolean containsElectrostatics() {
        return containsCoordinates() && containsMonopoles() && containsDipoles()
                && containsQuadrupoles() && containsOctupoles() && containsScreen2();
    }

    /**
     * Returns true iff original efp file contained all of the information for Polarization
     * A file has all of the Exchange Repulsion information if it has Polarizable Points
     *
     * @return true iff original efp file contained all of the information for Polarization
     */
    public boolean containsPolarization() {
        return containsPolarizablePts();
    }

    /**
     * Returns true iff original efp file contained all of the information for Exchange Repulsion
     * A file has all of the Exchange Repulsion information if it has Projection Basis Set, Multiplicity,
     * Projection Wave Function, Fock Matrix Elements, LMO Centroids
     *
     * @return true iff original efp file contained all of the information for Exchange Repulsion
     */
    public boolean containsExchangeRepulsion() {
        return containsProjectionBasis() && containsMultiplicity() && containsProjectionWavefunction()
                && containsFockMatrixElements() && containsLMOCentroids();
    }

    /**
     * Returns true iff original efp file contained all of the information for Dispersion
     * A file has all of the Exchange Repulsion information if it has Dynamic Polarizable Points
     *
     * @return true iff original efp file contained all of the information for Dispersion
     */
    public boolean containsDispersion() {
        return containsDynPolarizablePts();
    }

    /**
     * The next four methods are just wrappers for the contains methods because JavaFX requires these naming
     * conventions in order to automatically find the properties for the TableView. Therefore the metaDataSelector
     * controller is dependent on these next 4 methods
     * @return The respective boolean values for each
     */
    public boolean getElectrostatics(){ return containsElectrostatics(); }

    public boolean getExchangeRepulsion(){ return containsExchangeRepulsion(); }

    public boolean getPolarization(){ return containsPolarization(); }

    public boolean getDispersion(){ return containsDispersion(); }

    /**
     * This method creates an xyz file from the MetaData's coordinate field. The file will automatically be deleted
     * upon exiting iSpiEFP, so it will not take up system resources permanently. These files are to be used for
     * rendering fragments through the file->select fragment option. I suppose a user could use up all their system
     * resources by selecting fragments all day and never closing iSpiEFP, but this seems unlikely.
     * @return
     * @throws IOException
     */
    public File createTempXYZ() throws IOException {
        BufferedWriter bw = null;
        File xyzFile = null;
        double numAngstromsInBohr = 0.52918;
        try{
            //Create a temp xyz file
            xyzFile = File.createTempFile(fromFile, ".xyz");
            xyzFile.deleteOnExit();
            bw = new BufferedWriter(new FileWriter(xyzFile));

            //Write number of atoms not including dummy atoms in XYZ file
            int count = 0;
            for (int i = 0; i < coordinates.length; i++){
                if (coordinates[i].atomID.startsWith("B")) continue;
                count++;
            }
            bw.write(String.valueOf(count));
            //Blank line
            bw.write(System.getProperty("line.separator"));
            bw.write(System.getProperty("line.separator"));

            //Write the coordinates of each atom to the file
            for (int i = 0; i < coordinates.length; i++){
                //Don't include dummy atoms
                if (coordinates[i].atomID.startsWith("B")){
                    continue;
                }
                //Get the atom type by stripping all numbers from the atomID and removing the leading A
                String atomType = coordinates[i].atomID.replaceAll("[^A-Za-z]", "");
                atomType = atomType.substring(1);
                //Follow XYZ file format for each atom
                bw.write(String.format("%s\t%.5f\t%.5f\t%.5f%n",
                        atomType,
                        coordinates[i].x * numAngstromsInBohr,
                        coordinates[i].y * numAngstromsInBohr,
                        coordinates[i].z * numAngstromsInBohr));
            }
        } catch(IOException e){
            System.err.println("Unable to create a temporary file.");
        }
        finally {
            if (bw != null) bw.close();
        }
        return xyzFile;
    }

    /**
     * This method returns as a string the coordinates of the atoms as they would be listed in an XYZ file without
     * creating the XYZ file. Method is useful for libEFPInputController which requires coordinates as Strings
     * @return the above
     */
    public String getXYZCoords() {
        StringBuilder sb = new StringBuilder();
        double numAngstromsInBohr = 0.52918;
            //Write the coordinates of each atom to the file
            for (int i = 0; i < coordinates.length; i++){
                //Don't include dummy atoms
                if (coordinates[i].atomID.startsWith("B")){
                    continue;
                }
                //Get the atom type by stripping all numbers from the atomID and removing the leading A
                String atomType = coordinates[i].atomID.replaceAll("[^A-Za-z]", "");
                atomType = atomType.substring(1);
                //Follow XYZ file format for each atom
                sb.append(String.format("%s\t%.5f\t%.5f\t%.5f%n",
                        atomType,
                        coordinates[i].x * numAngstromsInBohr,
                        coordinates[i].y * numAngstromsInBohr,
                        coordinates[i].z * numAngstromsInBohr));
            }
        return sb.toString();
    }

    /**
     * Sets the .efp file for this fragment. In order to conserve disk space, this method is only ran when a fragment
     * is selected from File->Select Fragment. If the file is not already local (the case if it is a user-generated
     * parameter), it will be deleted upon system exit because it can be obtained again in the next session as long
     * as the user is connected to the internet. As of 12/26/2019 this does not handle the case that the fragment is
     * neither located in the Github repo or on the local system todo: Prompt the user to generate a custom EFP file
     * through GAMESS and save the file to the user-generated parameters directory.
     */
    public void setEfpFile() {
        File checkLocalFile = new File(fromFile);
        if (new File(fromFile).exists()){
            efpFile = checkLocalFile;
            return;
        }
        GithubRequester requester = new GithubRequester(fromFile);
        efpFile = requester.getEFPFile();
        System.out.printf("When setting, size of file is %d%n", efpFile.length());
        requester.cleanUp();
    }

    /**
     * Getter for the .efp File for this fragment.
     * @return the File which is set after selecting this fragment form File->Select Fragment. Note: will be null
     * if it was never set.
     */
    public File getEfpFile() {
        return efpFile;
    }

    /**
     * Gets the chemical formula for a metaData object. The atoms and their numbers are listed in alphabetical order
     * according to their atomic string
     * @return The chemical formula as a string in the form <atom1><numAtom1><atom2><numAtom2>...
     */
    public String getChemFormula(){
        HashMap<String, Integer> atomTypeMap = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>();
        for (int i = 0; i < coordinates.length; i++){
            String atomName = coordinates[i].atomID.replaceAll("[^A-Za-z]", "");
            if (atomName.startsWith("B")) continue;
            else atomName = atomName.substring(1);
            if (atomTypeMap.containsKey(atomName)){
                atomTypeMap.put(atomName, atomTypeMap.get(atomName) + 1);
            }
            else atomTypeMap.put(atomName, 1);
        }
        Iterator<String> keysItr = atomTypeMap.keySet().iterator();
        while (keysItr.hasNext()){
            StringBuilder sb = new StringBuilder();
            String key = keysItr.next();
            sb.append(key);
            sb.append(atomTypeMap.get(key));
            pq.add(sb.toString());
        }
        String returnString = "";
        while (!pq.isEmpty()){
            returnString += pq.poll();
        }
        return returnString;
    }
}
