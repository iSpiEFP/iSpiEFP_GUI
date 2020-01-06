package org.ispiefp.app;

import org.apache.commons.io.FilenameUtils;
import org.ispiefp.app.MetaData.*;
import org.ispiefp.app.installer.BundleManager;
import org.ispiefp.app.installer.LocalBundleManager;
import org.ispiefp.app.util.UserPreferences;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;


public class Initializer {
    private BundleManager bundleManager;

    public Initializer() {
        bundleManager = new BundleManager("LOCAL");
    }

    /**
     * Initializes all data structures required for iSpiEFP. Be warned that this is order dependent.
     * Most notably, bundleManger,manageLocal() must be invoked first, then UserPreferences and its initialization
     * the tree must also be constructed before attempting to addMetas to it
     */
    public void init() {
        bundleManager.manageLocal();
        UserPreferences preferences = new UserPreferences();
        preferences.initializePreferences();
        Main.fragmentTree = new LocalFragmentTree();
        generateMetas(UserPreferences.getUserParameterPath());
        addMetasToTree();
        Runtime.getRuntime().addShutdownHook(new Thread(){
          @Override
          public void run(){
              deleteDirectory(new File(LocalBundleManager.META_DATA_GENERATION));
          }
        });
    }

    /**
     * Generates MetaData.json files for all of the .efp files contained within the directory which was passed as an
     * argument to the function and outputs them to the /out/production/iSpiWorkSpace/MetaDataGeneration
     *
     * @param directoryPath the directory containing all of the .efp files as a String
     */
    public void generateMetas(String directoryPath) {
        //String extractMetaScriptPath = getClass().getResource("/scripts/extractMeta.py").getPath();
        String extractMetaScriptPath = null;
        try {
            URL resource = getClass().getResource("/scripts/extractMeta.py");
            File file = Paths.get(resource.toURI()).toFile();
            extractMetaScriptPath = file.getAbsolutePath();
        } catch (URISyntaxException e){
            e.printStackTrace();
        }
        File dir = new File(directoryPath);
        File[] dirFiles = dir.listFiles();
        if (dirFiles != null) {
            for (File child : dirFiles) {
                try {
                    if (!FilenameUtils.getExtension(child.getName()).equals("efp")) continue;
                    String commandInput = String.format("%s %s %s %s",UserPreferences.getPythonPath(),
                            extractMetaScriptPath,
                            child.getCanonicalPath(),
                            LocalBundleManager.META_DATA_GENERATION);
                    System.out.println(commandInput);
                    Process p = Runtime.getRuntime().exec(commandInput);   /* The path of the directory to write to */
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    String s;
                    while ((s = reader.readLine()) != null){
                    }
                } catch (IOException e) {
                    System.err.println("Was unable to generate Meta Data for the file " + child.getPath());
                }
            }
        }
    }

    public void addMetasToTree() {
        File dir = new File(LocalBundleManager.META_DATA_GENERATION);
        File[] dirFiles = dir.listFiles();
        if (dirFiles != null) {
            for (File child : dirFiles) {
                Main.fragmentTree.addFragment(child.getPath());
            }
        }
    }

    public boolean deleteDirectory(File directory){
        File[] dirFiles = directory.listFiles();
        if (dirFiles != null) {
            for (File child : dirFiles) {
                deleteDirectory(child);
            }
        }
        return directory.delete();
    }


}