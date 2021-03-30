/*
 *     iSpiEFP is an open source workflow optimization program for chemical simulation which provides an interactive GUI and interfaces with the existing libraries GAMESS and LibEFP.
 *     Copyright (C) 2021  Lyudmila V. Slipchenko
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please direct all questions regarding iSpiEFP to Lyudmila V. Slipchenko (lslipche@purdue.edu)
 */

package org.ispiefp.app;

import org.apache.commons.io.FilenameUtils;
import org.ispiefp.app.MetaData.LocalFragmentTree;
import org.ispiefp.app.installer.BundleManager;
import org.ispiefp.app.installer.LocalBundleManager;
import org.ispiefp.app.jobSubmission.JobsMonitor;
import org.ispiefp.app.util.ExecutePython;
import org.ispiefp.app.util.UserPreferences;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


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
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                deleteDirectory(new File(LocalBundleManager.META_DATA_GENERATION));
            }
        });
        JobsMonitor jobsMonitor = UserPreferences.getJobsMonitor();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(jobsMonitor, 0, 3, TimeUnit.SECONDS);
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