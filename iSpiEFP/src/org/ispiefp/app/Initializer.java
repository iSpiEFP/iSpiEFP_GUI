package org.ispiefp.app;

import org.ispiefp.app.MetaData.*;
import org.ispiefp.app.installer.BundleManager;
import org.ispiefp.app.installer.LocalBundleManager;
import org.ispiefp.app.util.UserPreferences;

import java.io.*;

public class Initializer {
    private BundleManager bundleManager;
    private LocalFragmentTree localFragmentTree;

    public Initializer() {
        bundleManager = new BundleManager("LOCAL");
    }

    public void init() {
        bundleManager.manageLocal();
        Main.fragmentTree = new LocalFragmentTree();
        generateMetas(LocalBundleManager.USER_PARAMETERS);
        generateMetas(LocalBundleManager.LIBRARY_PARAMETERS);
        addMetasToTree();
        UserPreferences preferences = new UserPreferences();
        preferences.initializePreferences();
    }

    /**
     * Generates MetaData.json files for all of the .efp files contained within the directory which was passed as an
     * argument to the function and outputs them to the /out/production/iSpiWorkSpace/MetaDataGeneration
     *
     * @param directoryPath the directory containing all of the .efp files as a String
     */
    public void generateMetas(String directoryPath) {
        String extractMetaScriptPath = getClass().getResource("/scripts/extractMeta.py").getPath();
        System.out.println(extractMetaScriptPath);
        File dir = new File(directoryPath);
        File[] dirFiles = dir.listFiles();
        if (dirFiles != null) {
            for (File child : dirFiles) {
                try {
                    String commandInput = String.format("python %s %s %s", extractMetaScriptPath,
                            child.getCanonicalPath(),
                            LocalBundleManager.META_DATA_GENERATION);
                    System.out.println(commandInput);
                    Process p = Runtime.getRuntime().exec(commandInput);   /* The path of the directory to write to */
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    String s;
                    while ((s = reader.readLine()) != null){
                        System.out.println("Anything");
                        System.out.println(s);
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
                //child.delete();
            }
        }
    }
}