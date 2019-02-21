package org.vmol.app.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.jmol.viewer.Viewer;
import org.vmol.app.Main;
import org.vmol.app.MainViewController;
import org.vmol.app.installer.LocalBundleManager;
import org.vmol.app.util.Atom;
import org.vmol.app.util.PDBParser;
import org.vmol.app.visualizer.ViewerHelper;

import com.google.gson.Gson;

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
    
    private void initWorkingDir() {
        this.mainDBDirectory = LocalBundleManager.LIBEFP;         //needed for db file storage
        this.xyzDirectory = LocalBundleManager.LIBEFP_COORDINATES;  //storage for db incoming xyz files
        this.efpDirectory = LocalBundleManager.LIBEFP_PARAMETERS;  //storage for db incoming efp files
        
    }
    
    //INPUT: Raw response from Database
    //public ArrayList<ArrayList<String []>> processDBresponse(JsonDatabaseResponse response) {
    public ArrayList<ArrayList<String []>> processDBresponse(JsonFilePair[][] response) {
        ArrayList<ArrayList<String []>> files = new ArrayList<ArrayList<String []>>(this.groupNames.size());

        System.out.println("size:"+this.groupNames.size());
        
        
        for(JsonFilePair[] group: response) {
            ArrayList<String []> list = new ArrayList<String []>();

            for(JsonFilePair pair : group){
                System.out.println("rolling a pair");
             
                if(!pair.xyz_file.isEmpty()){
                    String parsed_xyz_file = parseXYZresponse(pair.xyz_file);
                    
                    //seperate xyz file and efp file
                    String [] xyz_efp_rmsd = new String[3];
         
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
    
    private String parseXYZresponse(String rawFile) {
        ArrayList<String> result = new ArrayList<String>();
        System.out.println("printing raw xyz");
        System.out.println(rawFile);
        if(rawFile.isEmpty()) {
            System.out.println("raw file is empty");
        }
        String[] lines = rawFile.split("n', ");
        System.out.println(lines.length);
        
        for(String line : lines){
            System.out.println(line);
            String[] pieces = line.split("\\s+");
            String name = (pieces[0]);
            String x_coord = (pieces[1]);
            String y_coord = (pieces[2]);
            String z_coord = (pieces[3]);
            
            //fix name; dirty current fix
            //char atom_name = name.charAt(3);
            //name = Character.toString(atom_name);
            
            //name = name.replaceAll("^\\s+", "");

            //I apologize for this mess, I initially tried to just make this all work. Then I realized we needed Json
            //To really make this happen, then I got lazy, it should all be compatable to json and not need parsing
            //Oh well... if it aint broke dont fix it
            if(name.charAt(0) == '[') {
                name = name.substring(1);
            }
            name = name.substring(1);

            System.out.println("name:"+name);
            if(name.charAt(0) == 'B'){
                System.out.println("bond encountered");
            } else if (name.endsWith("H000")) {
                System.out.println("artificial hydrogen bond encountered");
            } else {
                //parse name
                name = name.substring(1);
                for(int u = 0; u < name.length(); u++){
                    char ch = name.charAt(u);
                    if(ch >= 'A' && ch <= 'Z'){
                        name = Character.toString(ch);
                        break;
                    }
                }
                name = Character.toString(name.charAt(name.length()-1));
                line = name + "      " + x_coord + "   " + y_coord + "   " + z_coord + "\n";
                result.add(line);
            }
        }
        String xyzFile = buildXYZfile(result);
        return xyzFile;
    }
    
    private String buildXYZfile(ArrayList<String> content) {
        StringBuilder result = new StringBuilder();
        
        //unroll content
        int line_count = content.size();
        result.append(Integer.toString(line_count) + "\n\n");
        
        for(String line: content) {
            result.append(line);
        }
        return result.toString();
    }
    
    private String parseCHEMNAMEfromEFPfile(String efpFile) {
        StringBuilder chemicalName = new StringBuilder();
        int index = efpFile.indexOf('$');
        char ch = '$';
        while(true) {
            ch = efpFile.charAt(++index);
            if(ch != ' ' && ch != '\r' && ch != '\n' && ch != '\t') {
                chemicalName.append(ch);
            } else {
                break;
            }
        }
        return chemicalName.toString();
    }

    public ArrayList<ArrayList<String[]>> writeFiles(ArrayList<ArrayList<String[]>> group_files) {
        ArrayList<ArrayList<String[]>> filenames = new ArrayList<ArrayList<String []>>(); //ArrayList for each group of relevant file matches
        
        for(ArrayList<String []> group : group_files){
            ArrayList<String []> pairNames = new ArrayList<String []>();
            
            Integer i = 1;
            for(String [] pair : group){
                String chemicalName = parseCHEMNAMEfromEFPfile(pair[1]);
                System.out.println("###############"+chemicalName);
                
                //create file names
                String xyz = chemicalName +"_"+i+".xyz";
                String efp = chemicalName +"_"+i+".efp";
                System.out.println(xyz);
                
                createFile(this.xyzDirectory, xyz, pair[0]);
                createFile(this.efpDirectory, efp, pair[1]);
                
                String[] pairName = { xyz, efp, pair[2]}; //pair[2] is rmsd
                pairNames.add(pairName);
                i++;
            }
            filenames.add(pairNames);
        }
        return filenames;
    }
    
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
        } finally{
            try{
                if(bufferedWriter != null) bufferedWriter.close();
            } catch(Exception ex){
                 
            }
        }
    }
    
    public String generateJsonQuery(ArrayList<ArrayList> groups) throws IOException {        
        ViewerHelper viewerHelper = new ViewerHelper();
        Viewer viewer = Main.jmolPanel.viewer;
        JsonFragment[] jsonFragments = new JsonFragment[groups.size()];
       
        for (int x = 0; x < groups.size(); x ++) {
                jsonFragments[x] = new JsonFragment();       
                ArrayList<JsonCoordinatePair> jsonCoordPairs = new ArrayList<JsonCoordinatePair>();
                TreeMap<String, Integer> symbolMap = new TreeMap<String, Integer>(); //helper for calculating chemical formula

                //get parameter for each atom in fragment
                for (int j = 0; j < groups.get(x).size(); j ++) {
                    int atomNum = (int) groups.get(x).get(j);
                    org.jmol.modelset.Atom atom = viewer.ms.at[atomNum];
                    JsonCoordinatePair coord = new JsonCoordinatePair();
                    String symbol = atom.getAtomName();
                    
                    coord.symbol = symbol;
                    coord.x = atom.x;
                    coord.y = atom.y;
                    coord.z = atom.z;
                    jsonCoordPairs.add(coord);
                    
                    //update symbol map
                    Integer value = symbolMap.get(symbol);
                    if(value == null){
                        symbolMap.put(symbol, 1);
                    } else {
                        symbolMap.put(symbol, value+1);
                    }     
                }
                //calculate chemical formula
                String chemFormula = viewerHelper.getChemicalFormula2(symbolMap);
                System.out.println("new chem formula:"+chemFormula);
                
                //add parameters
                this.groupNames.add(chemFormula);
                jsonFragments[x].chemicalFormula = chemFormula;
                jsonFragments[x].coords = jsonCoordPairs;
        }
        String json = new Gson().toJson(jsonFragments);
        return "Query2$END$"+json+"$ENDALL$";
    }
}
