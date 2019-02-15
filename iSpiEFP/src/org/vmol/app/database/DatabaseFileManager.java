package org.vmol.app.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

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
        
        //createDir(mainDBDirectory);
        //createDir(xyzDirectory);
        //createDir(efpDirectory);
    }
    
    //INPUT: Raw response from Database
    public ArrayList<ArrayList<String []>> processDBresponse(ArrayList<String> response) {
        ArrayList<ArrayList<String []>> files = new ArrayList<ArrayList<String []>>();

        for(String res : response){
            String reply = res;
            //reply = reply.substring(1);
            
            String[] content = reply.split("\\$NEXT\\$");    
            ArrayList<String []> fragment_files = new ArrayList<String []>();

            if(content.length <= 1){
                //no response
            } else {
                //parse response and dump in folders for each file line
                for(int i = 1; i < content.length; i++) {
                   
                    //seperate xyz file and efp file
                    String [] xyz_and_efp_pair = content[i].split("\\$EFP\\$");
                    //System.out.println("xyz file:"+xyz_and_efp_pair[0]);
                    //System.out.println("efp file:"+xyz_and_efp_pair[1]);
                    
                    String parsed_xyz_file = parseXYZresponse(xyz_and_efp_pair[0]);
                   
                    xyz_and_efp_pair[0] = parsed_xyz_file;
                    
                    fragment_files.add(xyz_and_efp_pair);
                }
           }
           files.add(fragment_files);     
        }
        return files;
    }
    
  //INPUT: Raw response from Database
    public ArrayList<ArrayList<String []>> processDBresponse2(JsonFilePair[] response) {
        ArrayList<ArrayList<String []>> files = new ArrayList<ArrayList<String []>>(this.groupNames.size());

        System.out.println("size:"+this.groupNames.size());
        
        //initialize file list
        for(int i = 0; i < this.groupNames.size(); i++) {
            //String[] strArr = new String[2];
            ArrayList<String []> list = new ArrayList<String []>();
            //list.add(strArr);
            files.add(list);
        }
        
        for(JsonFilePair pair : response){
            //String reply = res;
            //reply = reply.substring(1);
            
            //String[] content = reply.split("\\$NEXT\\$");    
            //ArrayList<String []> fragment_files = new ArrayList<String []>();

            //if(content.length <= 1){
                //no response
            //} else {
                //parse response and dump in folders for each file line
           // for(int i = 1; i < content.length; i++) {
            if(!pair.xyz_file.isEmpty()){
                String parsed_xyz_file = parseXYZresponse(pair.xyz_file);
                int index = groupNames.indexOf(pair.chemicalFormula);
                
                //seperate xyz file and efp file
                String [] xyz_and_efp_pair = new String[2];
                //System.out.println("xyz file:"+xyz_and_efp_pair[0]);
                //System.out.println("efp file:"+xyz_and_efp_pair[1]);
                
                xyz_and_efp_pair[0] = parsed_xyz_file;
                xyz_and_efp_pair[1] = pair.efp_file;
                        
                //fragment_files.add(xyz_and_efp_pair);
                //    }
               //}
                //files.get(index).add(xyz_and_efp_pair);
                ArrayList<String []> list = files.get(index);
                list.add(xyz_and_efp_pair);
           }
           
                //files.add(fragment_files);     
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
            
            for(String [] pair : group){
                String chemicalName = parseCHEMNAMEfromEFPfile(pair[1]);
                System.out.println("###############"+chemicalName);
                
                //create file names
                String xyz = chemicalName + ".xyz";
                String efp = chemicalName + ".efp";
                System.out.println(xyz);
                
                createFile(this.xyzDirectory, xyz, pair[0]);
                createFile(this.efpDirectory, efp, pair[1]);
                
                String[] pairName = { xyz, efp};
                pairNames.add(pairName);
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
        Gson gson = new Gson();
        //TypeDTO[] myTypes = gson.fromJson(new FileReader("input.json"), TypeDTO[].class);
        
        ArrayList<Atom> pdb;
        
        pdb = PDBParser.get_atoms(new File(MainViewController.getLastOpenedFile()));
       
        System.out.println("Atoms count: " + groups);
        
        JsonFragment[] jsonFragments = new JsonFragment[groups.size()];
        System.out.println(groups.size());
        System.out.print(jsonFragments.length);
        for (int x = 0; x < groups.size(); x ++) {
                
                jsonFragments[x] = new JsonFragment();
                                
                int[] symbols = new int[26];
                ArrayList<JsonCoordinatePair> jsonCoordPairs = new ArrayList<JsonCoordinatePair>();
                
                for (int j = 0; j < groups.get(x).size(); j ++) {
                    JsonCoordinatePair coord = new JsonCoordinatePair();
                    Atom current_atom = (Atom) pdb.get((Integer) groups.get(x).get(j));
                    if (current_atom.type.matches(".*\\d+.*")) { // atom symbol has digits, treat as charged atom

                        String symbol = current_atom.type;
                        String sign = symbol.substring(symbol.length() - 1);
                        String digits = symbol.replaceAll("\\D+", "");
                        String real_symbol = symbol.substring(0, symbol.length() - 2 - digits.length());
                        //query += "$END$" + real_symbol + "  " + current_atom.x + "  " + current_atom.y + "  " + current_atom.z;
                        System.out.println("symbol:"+real_symbol);
                        int index = real_symbol.charAt(0) - 'A';
                        symbols[index]++;
                        
                        coord.symbol = real_symbol;
                        coord.x = current_atom.x;
                        coord.y = current_atom.y;
                        coord.z = current_atom.z;
                        
                    } else {
                        String symbol = current_atom.type;
                        System.out.println("symbol:"+symbol);
                        
                        int index = symbol.charAt(0) - 'A';
                        symbols[index]++;
                        
                        coord.symbol = symbol;
                        coord.x = current_atom.x;
                        coord.y = current_atom.y;
                        coord.z = current_atom.z;
                        //query += "$END$" + current_atom.type + "  " + current_atom.x + "  " + current_atom.y + "  " + current_atom.z;
                    }
                    jsonCoordPairs.add(coord);
                }
                String chemFormula = (new ViewerHelper()).getChemicalFormula(symbols);
                this.groupNames.add(chemFormula);
                jsonFragments[x].chemicalFormula = chemFormula;
                jsonFragments[x].coords = jsonCoordPairs;
                //query+="$ENDALL$";
                
                //String chemFormula2 = (new ViewerHelper()).getChemicalFormula2(symbols);
                //System.out.println("Chemical Formula:"+chemFormula2);  
        }
        String json = new Gson().toJson(jsonFragments);
        return "Query2$END$"+json+"$ENDALL$";
    }
}
