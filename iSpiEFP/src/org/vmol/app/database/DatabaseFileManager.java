package org.vmol.app.database;

import com.google.gson.Gson;
import org.jmol.viewer.Viewer;
import org.vmol.app.Main;
import org.vmol.app.installer.LocalBundleManager;
import org.vmol.app.visualizer.ViewerHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;

/*
DatabaseFileManager Class processes database response from server,
and parses, and writes content into two seperate directories.
One directory for xyz files and another for their efp counterparts.
DBFileManager Responds with a list of files, each fragment was able to retrieve from the database
*/

public class DatabaseFileManager {
    private String workingDirectoryPath;
    private String mainDBDirectory;
    private String xyzDirectory;
    private String efpDirectory;
    private ArrayList<String> groupNames;

    public DatabaseFileManager() {
        groupNames = new ArrayList<String>();
    }

    public DatabaseFileManager(String path) {
        this.workingDirectoryPath = path;
        initWorkingDir();
        groupNames = new ArrayList<String>();

    }

    /**
     * Initialize the working directory parameters
     */
    private void initWorkingDir() {
        this.mainDBDirectory = LocalBundleManager.LIBEFP;         //needed for db file storage
        this.xyzDirectory = LocalBundleManager.LIBEFP_COORDINATES;  //storage for db incoming xyz files
        this.efpDirectory = LocalBundleManager.LIBEFP_PARAMETERS;  //storage for db incoming efp files

    }

    //INPUT: Raw response from Database

    /**
     * Process the JSON response from the Database into a list of filenames with parsed properties
     *
     * @param response JSON file pair[][] with fields 'rmsd', 'xyz', and 'efp'
     * @return List of files with parsed file values
     */
    public ArrayList<ArrayList<String[]>> processDBresponse(JsonFilePair[][] response) {
        ArrayList<ArrayList<String[]>> files = new ArrayList<ArrayList<String[]>>(this.groupNames.size());

        for (JsonFilePair[] group : response) {
            ArrayList<String[]> list = new ArrayList<String[]>();
            for (JsonFilePair pair : group) {
                if (!pair.xyz_file.isEmpty()) {
                    String parsed_xyz_file = parseXYZresponse(pair.xyz_file);

                    //seperate xyz file and efp file
                    String[] xyz_efp_rmsd = new String[3];

                    xyz_efp_rmsd[0] = parsed_xyz_file;
                    xyz_efp_rmsd[1] = pair.efp_file;
                    xyz_efp_rmsd[2] = pair.rmsd;

                    list.add(xyz_efp_rmsd);
                }
            }
            files.add(list);
        }
        return files;
    }

    /**
     * Parse xyz file content from the raw database Response
     * This function is fucked up, I realize but I am too lazy to fix since it works
     *
     * @param rawFile
     * @return
     */
    private String parseXYZresponse(String rawFile) {
        ArrayList<String> result = new ArrayList<String>();
        String[] lines = rawFile.split("n', ");

        for (String line : lines) {
            String[] pieces = line.split("\\s+");
            String name = (pieces[0]);
            String x_coord = (pieces[1]);
            String y_coord = (pieces[2]);
            String z_coord = (pieces[3]);

            //I apologize for this mess, I initially tried to just make this all work. Then I realized we needed Json
            //To really make this happen, then I got lazy, it should all be compatable to json and not need parsing
            //Oh well... if it aint broke dont fix it
            if (name.charAt(0) == '[') {
                name = name.substring(1);
            }
            name = name.substring(1);

            System.out.println("name:" + name);
            if (name.charAt(0) == 'B') {
                System.out.println("bond encountered");
            } else if (name.endsWith("H000")) {
                System.out.println("artificial hydrogen bond encountered");
            } else {
                //parse name
                name = name.substring(1);
                for (int u = 0; u < name.length(); u++) {
                    char ch = name.charAt(u);
                    if (ch >= 'A' && ch <= 'Z') {
                        name = Character.toString(ch);
                        break;
                    }
                }
                name = Character.toString(name.charAt(name.length() - 1));

                //convert gamess bohr output to angstroms
                double x = ViewerHelper.convertBohrToAngstrom(Double.parseDouble(x_coord));
                double y = ViewerHelper.convertBohrToAngstrom(Double.parseDouble(y_coord));
                double z = ViewerHelper.convertBohrToAngstrom(Double.parseDouble(z_coord));

                line = name + "      " + x + "   " + y + "   " + z + "\n";
                result.add(line);
            }
        }
        String xyzFile = buildXYZfile(result);
        return xyzFile;
    }

    /**
     * Build an xyz file from an array of lines helper function
     *
     * @param content xyz file lines
     * @return a single string containing the file contents
     */
    private String buildXYZfile(ArrayList<String> content) {
        StringBuilder result = new StringBuilder();

        //unroll content
        int line_count = content.size();
        result.append(line_count + "\n\n");

        for (String line : content) {
            result.append(line);
        }
        return result.toString();
    }

    /**
     * Parse the chemical Name from the top of the EFP file
     *
     * @param efpFile the efp file content
     * @return a string of the chemical name from this efp file
     */
    private String parseCHEMNAMEfromEFPfile(String efpFile) {
        StringBuilder chemicalName = new StringBuilder();
        int index = efpFile.indexOf('$');
        char ch = '$';
        while (true) {
            ch = efpFile.charAt(++index);
            if (ch != ' ' && ch != '\r' && ch != '\n' && ch != '\t') {
                chemicalName.append(ch);
            } else {
                break;
            }
        }
        return chemicalName.toString();
    }

    /**
     * Write the files to disk
     *
     * @param group_files list of files with filenames and content
     * @return list of written filenames
     */
    public ArrayList<ArrayList<String[]>> writeFiles(ArrayList<ArrayList<String[]>> group_files) {
        ArrayList<ArrayList<String[]>> filenames = new ArrayList<ArrayList<String[]>>(); //ArrayList for each group of relevant file matches

        for (ArrayList<String[]> group : group_files) {
            ArrayList<String[]> pairNames = new ArrayList<String[]>();

            Integer i = 1;
            for (String[] pair : group) {
                String chemicalName = parseCHEMNAMEfromEFPfile(pair[1]);

                //create file names
                String xyz = chemicalName + "_" + i + ".xyz";
                String efp = chemicalName + "_" + i + ".efp";

                pair[1] = pair[1].replaceFirst(chemicalName, chemicalName + "_" + i); //replace first string with correct chem name
                System.out.println(xyz);

                createFile(this.xyzDirectory, xyz, pair[0]);
                createFile(this.efpDirectory, efp, pair[1]);

                String[] pairName = {xyz, efp, pair[2]}; //pair[2] is rmsd
                pairNames.add(pairName);
                i++;
            }
            filenames.add(pairNames);
        }
        return filenames;
    }

    /**
     * Write this file to disk
     *
     * @param path    path of the file
     * @param name    file name
     * @param content file content
     */
    private void createFile(String path, String name, String content) {
        BufferedWriter bufferedWriter = null;
        try {
            String filename = path + "\\" + name;
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            Writer writer = new FileWriter(file);
            bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null) bufferedWriter.close();
            } catch (Exception ex) {

            }
        }
    }

    /**
     * Generate proper JSON Query for sending to the database
     * A proper query is in JSON format and contains a chemical symbol with x,y,and z coordinates
     */
    public String generateJsonQuery(ArrayList<ArrayList> groups) throws IOException {
        ViewerHelper viewerHelper = new ViewerHelper();
        Viewer viewer = Main.jmolPanel.viewer;
        JsonFragment[] jsonFragments = new JsonFragment[groups.size()];

        for (int x = 0; x < groups.size(); x++) {
            jsonFragments[x] = new JsonFragment();
            ArrayList<JsonCoordinatePair> jsonCoordPairs = new ArrayList<JsonCoordinatePair>();
            TreeMap<String, Integer> symbolMap = new TreeMap<String, Integer>(); //helper for calculating chemical formula

            //get parameter for each atom in fragment
            for (int j = 0; j < groups.get(x).size(); j++) {
                int atomNum = (int) groups.get(x).get(j);
                org.jmol.modelset.Atom atom = viewer.ms.at[atomNum];
                JsonCoordinatePair coord = new JsonCoordinatePair();
                String symbol = atom.getElementSymbol();

                coord.symbol = symbol;
                coord.x = ViewerHelper.convertAngstromToBohr(atom.x);
                coord.y = ViewerHelper.convertAngstromToBohr(atom.y);
                coord.z = ViewerHelper.convertAngstromToBohr(atom.z);
                jsonCoordPairs.add(coord);

                //update symbol map
                Integer value = symbolMap.get(symbol);
                if (value == null) {
                    symbolMap.put(symbol, 1);
                } else {
                    symbolMap.put(symbol, value + 1);
                }
            }
            //calculate chemical formula
            String chemFormula = viewerHelper.getChemicalFormula2(symbolMap);
            System.out.println("new chem formula:" + chemFormula);

            //add parameters
            this.groupNames.add(chemFormula);
            jsonFragments[x].chemicalFormula = chemFormula;
            jsonFragments[x].coords = jsonCoordPairs;
        }
        String json = new Gson().toJson(jsonFragments);
        return "Query$END$" + json + "$ENDALL$";
    }
}
