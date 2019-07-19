package org.libefp.app.installer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * Handle Local bundles including setting local paths, and loading local directories
 */
public class LocalBundleManager {
    /**
     * These are directory Constants.
     * <p>
     * Example: LIBEFP is referring to Libefp/src in the main working directory
     */
    public static final String FILE_SEPERATOR = System.getProperty("file.separator");

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
            workingDirectory = getJarPath();
            WORKSPACE = workingDirectory + FILE_SEPERATOR + "iSpiWorkSpace";

            GAMESS = WORKSPACE + FILE_SEPERATOR + "Gamess";
            GAMESS_SRC = GAMESS + FILE_SEPERATOR + "src";
            GAMESS_INPUTS = GAMESS + FILE_SEPERATOR + "Inputs";
            GAMESS_SRC_EXE = GAMESS_SRC + FILE_SEPERATOR + "gms";

            LIBEFP = WORKSPACE + FILE_SEPERATOR + "Libefp";
            LIBEFP_SRC = LIBEFP + FILE_SEPERATOR + "src";
            LIBEFP_INPUTS = LIBEFP + FILE_SEPERATOR + "Inputs";
            LIBEFP_PARAMETERS = LIBEFP + FILE_SEPERATOR + "Parameters";
            LIBEFP_COORDINATES = LIBEFP + FILE_SEPERATOR + "Coordinates";
            LIBEFP_SRC_EXE = LIBEFP_SRC + FILE_SEPERATOR + "efpmd";

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Get Path of (this) jar applicaton (iSpiEFP application) this allows jar to be moved around and still work
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    public String getJarPath() throws UnsupportedEncodingException {
        URL url = LocalBundleManager.class.getProtectionDomain().getCodeSource().getLocation();
        String jarPath = URLDecoder.decode(url.getFile(), "UTF-8");
        String parentPath = new File(jarPath).getParentFile().getPath();
        return parentPath;
    }

    /**
     * Check if any directories are missing and if they are recreate them
     *
     * @return
     */
    public ArrayList<String> checkIfWorkingDirectoryIsReady() {
        ArrayList<String> missingFiles = new ArrayList<String>();

        //main directory
        if (!(new File(WORKSPACE)).exists()) {
            missingFiles.add(WORKSPACE);
        }

        //Gamess Files
        if (!(new File(GAMESS)).exists()) {
            missingFiles.add(GAMESS);
        }
        if (!(new File(GAMESS_SRC)).exists()) {
            missingFiles.add(GAMESS_SRC);
        }
        if (!(new File(GAMESS_SRC_EXE)).exists()) {
            missingFiles.add(GAMESS_SRC_EXE);
        }
        if (!(new File(GAMESS_INPUTS)).exists()) {
            missingFiles.add(GAMESS_INPUTS);
        }

        //Libefp Files
        if (!(new File(LIBEFP)).exists()) {
            missingFiles.add(LIBEFP);
        }
        if (!(new File(LIBEFP_SRC)).exists()) {
            missingFiles.add(LIBEFP_SRC);
        }
        if (!(new File(LIBEFP_SRC_EXE)).exists()) {
            missingFiles.add(LIBEFP_SRC_EXE);
        }
        if (!(new File(LIBEFP_INPUTS)).exists()) {
            missingFiles.add(LIBEFP_INPUTS);
        }
        if (!(new File(LIBEFP_PARAMETERS)).exists()) {
            missingFiles.add(LIBEFP_PARAMETERS);
        }
        if (!(new File(LIBEFP_COORDINATES)).exists()) {
            missingFiles.add(LIBEFP_COORDINATES);
        }

        return missingFiles;
    }

    /**
     * Create all the missing directories
     *
     * @param missingFiles the missing list of files
     * @param bundleType   the type of missing package
     */
    public void installMissingFiles(ArrayList<String> missingFiles, String bundleType) {
        for (String filename : missingFiles) {
            if (filename.equals(GAMESS_SRC_EXE)) {
                if (bundleType.equals("GAMESS")) {
                    installGamess();
                }
            } else if (filename.equals(LIBEFP_SRC_EXE)) {
                if (bundleType.equals("LIBEFP")) {
                    installLibefp();
                }
            } else {
                System.out.println("Creating file: " + filename);
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
