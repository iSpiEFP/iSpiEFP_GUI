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

package org.ispiefp.app.installer;

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
    public static String LIBEFP_OUTPUTS;
    public static String LIBEFP_PARAMETERS;
    public static String LIBEFP_COORDINATES;
    public static String PARAMETERS;            /* Parameter subdirectories and permanent single JSON file      */
    public static String USER_PARAMETERS;       /* For user-generated EFP parameters                            */
    public static String LIBRARY_PARAMETERS;    /* For local copies of the default library parameters           */
    public static String MASTER_META_FILE;      /* Contains the meta data of library parameters from start-up   */
    public static String META_DATA_GENERATION;  /* Contains all of the generated MetaDatas at runtime           */

    public LocalBundleManager() {
        try {
            workingDirectory = getJarPath();
            WORKSPACE = workingDirectory + FILE_SEPERATOR + "iSpiWorkSpace";
            PARAMETERS = workingDirectory + FILE_SEPERATOR + "parameters";
            USER_PARAMETERS = PARAMETERS + FILE_SEPERATOR + "user_parameters";
            LIBRARY_PARAMETERS = PARAMETERS + FILE_SEPERATOR + "library_parameters";
            MASTER_META_FILE = PARAMETERS + FILE_SEPERATOR + "libraryMeta.json";

            META_DATA_GENERATION = WORKSPACE + FILE_SEPERATOR + "MetaDataGeneration" + FILE_SEPERATOR;
            GAMESS = WORKSPACE + FILE_SEPERATOR + "Gamess";
            GAMESS_SRC = GAMESS + FILE_SEPERATOR + "src";
            GAMESS_INPUTS = GAMESS + FILE_SEPERATOR + "Inputs";
            GAMESS_SRC_EXE = GAMESS_SRC + FILE_SEPERATOR + "gms";

            LIBEFP = WORKSPACE + FILE_SEPERATOR + "Libefp";
            LIBEFP_SRC = LIBEFP + FILE_SEPERATOR + "src";
            LIBEFP_INPUTS = LIBEFP + FILE_SEPERATOR + "Inputs";
            LIBEFP_OUTPUTS = LIBEFP + FILE_SEPERATOR + "Outputs";
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

        //Create MetaData generation directory
        if (!(new File(META_DATA_GENERATION)).exists()) {
            missingFiles.add(META_DATA_GENERATION);
        }

        if (!(new File(USER_PARAMETERS).exists())){
            missingFiles.add(USER_PARAMETERS);
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
