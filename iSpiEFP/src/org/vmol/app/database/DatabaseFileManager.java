package org.vmol.app.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.vmol.app.installer.LocalBundleManager;

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
    
    public DatabaseFileManager(String path) {
        this.workingDirectoryPath = path;
        initWorkingDir();
    }
    
    private void initWorkingDir() {
        this.mainDBDirectory = LocalBundleManager.LIBEFP;         //needed for db file storage
        this.xyzDirectory = LocalBundleManager.LIBEFP_COORDINATES;  //storage for db incoming xyz files
        this.efpDirectory = LocalBundleManager.LIBEFP_PARAMETERS;  //storage for db incoming efp files
        
        //createDir(mainDBDirectory);
        //createDir(xyzDirectory);
        //createDir(efpDirectory);
    }
    
    private void createDir(String path) {
        new File(path).mkdirs();
    }
    
    //INPUT: Raw response from Database
    public ArrayList<ArrayList<String []>> processDBresponse(ArrayList<String> response) {
        ArrayList<ArrayList<String []>> files = new ArrayList<ArrayList<String []>>();

        for(String res : response){
            String reply = res;
            reply = reply.substring(1);
            
            String[] content = reply.split("\\$NEXT\\$");    
            ArrayList<String []> fragment_files = new ArrayList<String []>();

            if(content.length <= 1){
                //no response
            } else {
                //parse response and dump in folders for each file line
                for(int i = 1; i < content.length; i++) {
                   
                    //seperate xyz file and efp file
                    String [] xyz_and_efp_pair = content[i].split("\\$EFP\\$");
                    System.out.println("xyz file:"+xyz_and_efp_pair[0]);
                    System.out.println("efp file:"+xyz_and_efp_pair[1]);
                    
                    String parsed_xyz_file = parseXYZresponse(xyz_and_efp_pair[0]);
                   
                    xyz_and_efp_pair[0] = parsed_xyz_file;
                    
                    fragment_files.add(xyz_and_efp_pair);
                }
           }
           files.add(fragment_files);     
        }
        return files;
    }
    
    private String parseXYZresponse(String rawFile) {
        ArrayList<String> result = new ArrayList<String>();
        
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
            
            name = name.substring(1);
            System.out.println("name:"+name);
            if(name.charAt(0) == 'B'){
                System.out.println("bond encountered");
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
}
