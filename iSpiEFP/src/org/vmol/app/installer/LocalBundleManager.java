package org.vmol.app.installer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Optional;


import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LocalBundleManager {
    /**
     *     These are directory Constants.
     *     
     *     Example: LIBEFP is referring to Libefp/src in the main working directory
     */
    //public static final String workingDirectory = System.getProperty("user.dir");
    //public static final String workingDirectory = (new File(ClassLoader.getSystemClassLoader().getResource(".").getPath())).getAbsolutePath();
    //public static final String workingDirectory = (new File(ClassLoader.getSystemClassLoader().getResource(".").getPath())).getAbsolutePath();

    public static final String FILE_SEPERATOR = System.getProperty("file.separator");
    /*
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
    */
    public static String workingDirectory;
    public static String WORKSPACE;
    
    public static String GAMESS;
    public static String GAMESS_SRC;
    public static String GAMESS_INPUTS;
    public static String GAMESS_SRC_EXE;
    
    public static String LIBEFP;
    public static String LIBEFP_SRC;
    public static String LIBEFP_SRC_EXE;
    public static String LIBEFP_INPUTS;
    public static String LIBEFP_PARAMETERS;
    public static String LIBEFP_COORDINATES;
    
    public LocalBundleManager() {
        try {
            this.workingDirectory = getJarPath();
            this.WORKSPACE = workingDirectory + FILE_SEPERATOR + "iSpiWorkSpace";
            
            this.GAMESS = WORKSPACE + FILE_SEPERATOR + "Gamess";
            this.GAMESS_SRC = GAMESS + FILE_SEPERATOR + "src";
            this.GAMESS_INPUTS = GAMESS + FILE_SEPERATOR + "Inputs";
            this.GAMESS_SRC_EXE = GAMESS_SRC + FILE_SEPERATOR + "gms";
            
            this.LIBEFP = WORKSPACE + FILE_SEPERATOR + "Libefp";
            this.LIBEFP_SRC = LIBEFP + FILE_SEPERATOR + "src";
            this.LIBEFP_INPUTS = LIBEFP + FILE_SEPERATOR + "Inputs";
            this.LIBEFP_PARAMETERS = LIBEFP + FILE_SEPERATOR + "Parameters";
            this.LIBEFP_COORDINATES = LIBEFP + FILE_SEPERATOR + "Coordinates"; 
            this.LIBEFP_SRC_EXE = LIBEFP_SRC + FILE_SEPERATOR + "efpmd";

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Bundle Install Wizard");
        alert.setHeaderText("Header ");
        alert.setContentText("Path:"+workingDirectory);
        
        
      
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        
        File jarDir = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
        System.out.println(jarDir.getAbsolutePath()); */
    }
    
    public String getJarPath() throws UnsupportedEncodingException {
        URL url = LocalBundleManager.class.getProtectionDomain().getCodeSource().getLocation();
        String jarPath = URLDecoder.decode(url.getFile(), "UTF-8");
        String parentPath = new File(jarPath).getParentFile().getPath();
        return parentPath;
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
                new File(filename).mkdirs();
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
