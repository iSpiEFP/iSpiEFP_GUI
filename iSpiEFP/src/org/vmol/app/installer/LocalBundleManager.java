package org.vmol.app.installer;

import java.io.File;
import java.util.ArrayList;

public class LocalBundleManager {
    /**
     *     These are directory Constants.
     *     
     *     Example: LIBEFP is referring to Libefp/src in the main working directory
     */
    public static final String workingDirectory = System.getProperty("user.dir");
 
    public static final String WORKSPACE = workingDirectory + "/WorkSpace";
    
    public static final String GAMESS = workingDirectory + "/WorkSpace/Gamess";
    public static final String GAMESS_SRC = workingDirectory + "/WorkSpace/Gamess/src";
    public static final String GAMESS_INPUTS = workingDirectory + "/WorkSpace/Gamess/Inputs";
    public static final String GAMESS_SRC_EXE = workingDirectory + "/WorkSpace/Gamess/src/gms";
    
    public static final String LIBEFP = workingDirectory + "/WorkSpace/Libefp";
    public static final String LIBEFP_SRC = workingDirectory + "/WorkSpace/Libefp/src";
    public static final String LIBEFP_SRC_EXE = workingDirectory + "/WorkSpace/Libefp/src/efpmd";
    public static final String LIBEFP_INPUTS = workingDirectory + "/WorkSpace/Libefp/Inputs";
    public static final String LIBEFP_PARAMETERS = workingDirectory + "/WorkSpace/Libefp/Parameters";
    public static final String LIBEFP_COORDINATES = workingDirectory + "/WorkSpace/Libefp/Coordinates";
    
    public LocalBundleManager() {
        
    }

    public ArrayList<String> checkIfWorkingDirectoryIsReady() {
        ArrayList<String> missingFiles = new ArrayList<String>();
        
        //main directory
        if(!(new File(WORKSPACE)).exists()) {
            missingFiles.add(WORKSPACE);    
        } 
      
        //Gamess Files
        if(!(new File(GAMESS)).exists()) {
            missingFiles.add(GAMESS); 
        }
        if(!(new File(GAMESS_SRC)).exists()) {
            missingFiles.add(GAMESS_SRC);   
        }
        if(!(new File(GAMESS_SRC_EXE)).exists()) {
            missingFiles.add(GAMESS_SRC_EXE);
        }
        if(!(new File(GAMESS_INPUTS)).exists()) {
            missingFiles.add(GAMESS_INPUTS);
        }
        
        //Libefp Files
        if(!(new File(LIBEFP)).exists()) {
            missingFiles.add(LIBEFP);
        }  
        if(!(new File(LIBEFP_SRC)).exists()) {
            missingFiles.add(LIBEFP_SRC);
        }
        if(!(new File(LIBEFP_SRC_EXE)).exists()) {
            missingFiles.add(LIBEFP_SRC_EXE);
        }
        if(!(new File(LIBEFP_INPUTS)).exists()) {
            missingFiles.add(LIBEFP_INPUTS);
        }
        if(!(new File(LIBEFP_PARAMETERS)).exists()) {
            missingFiles.add(LIBEFP_PARAMETERS);
        }
        if(!(new File(LIBEFP_COORDINATES)).exists()) {
            missingFiles.add(LIBEFP_COORDINATES);
        }
        
        return missingFiles;      
    }

    public void installMissingFiles(ArrayList<String> missingFiles, String bundleType) {
        for(String filename : missingFiles) {
            if(filename.equals(GAMESS_SRC_EXE)) {
                if(bundleType.equals("GAMESS")) {
                    installGamess();
                }
            } else if(filename.equals(LIBEFP_SRC_EXE)) { 
                if(bundleType.equals("LIBEFP")) {
                    installLibefp();
                }
            } else {
                System.out.println("Creating file: "+filename);
                new File(this.workingDirectory+filename).mkdirs();
            }
        }
    }
    
    private void installGamess() {
        System.out.println("This function needs to be implemented, no local installation of Gamess found");
    }
    
    private void installLibefp() {
        System.out.println("This function needs to be implemented, no local installation of Libefp found");
    }
}
