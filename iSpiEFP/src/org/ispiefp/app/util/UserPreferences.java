package org.ispiefp.app.util;
import org.ispiefp.app.installer.LocalBundleManager;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class UserPreferences {
    /* Keys for user preferences */
    private static final String USER_PARAMETER_PATH_KEY = "userParameterDirectory";
    private static final String PYTHON_PATH_KEY = "pythonInterpreterPath";
    private static String userParameterPath = null;
    private static String pythonPath = null;
    private static boolean pythonPathExists = false;
    private static Preferences userPrefs;

    public UserPreferences(){
        userPrefs = Preferences.userNodeForPackage(UserPreferences.class);
    }

    /**
     * Called at start-up checks in a system-independent manner for the following variables
     * and assigns them the following default variables these variables will persist upon
     * program exit:
     *
     * userParameterPath:
     *  Description - The directory where the user stores their EFP files
     *  Key - USER_PARAMETER_PATH_KEY
     *  Default Value - The user parameter directory created upon first start-up
     *
     * pythonPath:
     *  Description - Path to the user's python interpreter if it exists. Check pythonPathExists first.
     *  Key - PYTHON_PATH_KEY
     *  Default Value - The path returned by the environment variable PYTHONPATH or an error message if DNE
     */
    public void initializePreferences() {
        if ((userParameterPath = userPrefs.get(USER_PARAMETER_PATH_KEY, "check")).equals("check")){
            userPrefs.put(USER_PARAMETER_PATH_KEY, LocalBundleManager.USER_PARAMETERS);
            userParameterPath = LocalBundleManager.USER_PARAMETERS;
        }
        if ((pythonPath = userPrefs.get(PYTHON_PATH_KEY, "check")).equals("check")) {
            if (System.getenv("PYTHONPATH") != null) {
                userPrefs.put(PYTHON_PATH_KEY, System.getenv("PYTHONPATH"));
                pythonPath = System.getenv("PYTHONPATH");
                pythonPathExists = true;
            }
            else{
                pythonPath = "Could not automatically find python interpreter";
            }
        }
        else{
            pythonPathExists = true;
        }
    }

    public static String getUserParameterPath() {
        return userParameterPath;
    }

    public static String getPythonPath() {
        return pythonPath;
    }

    public static boolean pythonPathExists() {
        return pythonPathExists;
    }

    public static void setUserParameterPath(String value){
        userPrefs.put(USER_PARAMETER_PATH_KEY, value);
        userParameterPath = userPrefs.get(USER_PARAMETER_PATH_KEY, "check");
    }

    public static void setPythonPath(String value){
        userPrefs.put(PYTHON_PATH_KEY, value);
        pythonPathExists = true;
        pythonPath = userPrefs.get(PYTHON_PATH_KEY, "check");
    }
}
