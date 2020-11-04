package org.ispiefp.app;

import org.apache.commons.io.FilenameUtils;
import org.ispiefp.app.installer.BundleManager;
import org.ispiefp.app.installer.LocalBundleManager;
import org.ispiefp.app.metaData.LocalFragmentTree;
import org.ispiefp.app.util.ExecutePython;
import org.ispiefp.app.util.UserPreferences;

import java.io.File;
import java.io.IOException;


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
        Thread thread = new Thread(UserPreferences.getJobsMonitor());
        thread.start();
    }

    /**
     * Generates MetaData.json files for all of the .efp files contained within the directory which was passed as an
     * argument to the function and outputs them to the /out/production/iSpiWorkSpace/MetaDataGeneration
     *
     * @param directoryPath the directory containing all of the .efp files as a String
     */
    public void generateMetas(String directoryPath) {
//        //String extractMetaScriptPath = getClass().getResource("/scripts/extractMeta.py").getPath();
//        String jarLocation;
//        File extractMetaScript = null;
////        FileSystem fileSystem;
//        try {
////            URL resource = getClass().getResource("/scripts/extractMeta.py");
////            System.out.println(resource.toURI());
////            File file = Paths.get(resource.toURI()).toFile();
////            extractMetaScriptPath = file.getAbsolutePath();
//            jarLocation = new File(Initializer.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
//            Path path = Paths.get(jarLocation);
//            URI uri = new URI("jar", path.toUri().toString(), null);
//
//            Map<String, String> env = new HashMap<>();
//            env.put("create", "true");
//
////            FileSystem fileSystem = FileSystems.newFileSystem(uri, env);
//            java.nio.file.FileSystem fileSystem = FileSystems.newFileSystem(uri, env);
//            InputStream is = getClass().getResourceAsStream("/scripts/extractMeta.py");
//            extractMetaScript = File.createTempFile("extractMeta.py", null);
//            extractMetaScript.deleteOnExit();
//            FileUtils.copyInputStreamToFile(is, extractMetaScript);
//        } catch (Exception e){
//            e.printStackTrace();
//            return;
//        }
        File dir = new File(directoryPath);
        File[] dirFiles = dir.listFiles();
        if (dirFiles != null) {
            for (File child : dirFiles) {
                try {
                    if (!FilenameUtils.getExtension(child.getName()).equals("efp")) continue;
                    String arguments = String.format("%s %s",
                            child.getCanonicalPath(),
                            LocalBundleManager.META_DATA_GENERATION);
                    ExecutePython.runPythonScript("extractMeta.py", arguments);
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