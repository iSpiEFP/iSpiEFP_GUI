package org.ispiefp.app.util;
import org.ispiefp.app.installer.LocalBundleManager;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class UserPreferences {
    /* Keys for user preferences */
    private static final String USER_PARAMETER_PATH_KEY = "userParameterDirectory";
    private static final String PYTHON_PATH_KEY = "pythonInterpreterPath";
    private static final String GAMESS_SERVER_KEY = "gamessServerKey";
    private static final String GAMESS_USERNAME_KEY = "gamessUsername";
    private static final String GAMESS_PASSWORD_KEY = "gamessPassword";
    private static final String GAMESS_OUTPUT_KEY = "gamessOutputPath";
    private static final String LIBEFP_SERVER_KEY = "libefpServerKey";
    private static final String LIBEFP_USERNAME_KEY ="libefpUsername";
    private static final String LIBEFP_PASSWORD_KEY = "libefpPassword";
    private static final String LIBEFP_OUTPUT_KEY = "libefpOutputPath";

    private static String userParameterPath = null;
    private static String pythonPath = null;
    private static String gamessServer = null;
    private static String gamessUsername = null;
    private static String gamessPassword = null;
    private static String gamessOutputPath = null;
    private static String libefpServer = null;
    private static String libefpUsername = null;
    private static String libefpPassword = null;
    private static String libefpOutputPath =null;

    private static boolean pythonPathExists = false;
    private static Preferences userPrefs;

    public UserPreferences() {
        userPrefs = Preferences.userNodeForPackage(UserPreferences.class);
    }

    /**
     * Called at start-up checks in a system-independent manner for the following variables
     * and assigns them the following default variables these variables will persist upon
     * program exit:
     * <p>
     * userParameterPath:
     * Description - The directory where the user stores their EFP files
     * Key - USER_PARAMETER_PATH_KEY
     * Default Value - The user parameter directory created upon first start-up
     * <p>
     * pythonPath:
     * Description - Path to the user's python interpreter if it exists. Check pythonPathExists first.
     * Key - PYTHON_PATH_KEY
     * Default Value - The path returned by the environment variable PYTHONPATH or an error message if DNE
     */
    public void initializePreferences() {
        if ((userParameterPath = userPrefs.get(USER_PARAMETER_PATH_KEY, "check")).equals("check")) {
            userPrefs.put(USER_PARAMETER_PATH_KEY, LocalBundleManager.USER_PARAMETERS);
            userParameterPath = LocalBundleManager.USER_PARAMETERS;
        }
        if ((pythonPath = userPrefs.get(PYTHON_PATH_KEY, "check")).equals("check")) {
            if (System.getenv("PYTHONPATH") != null) {
                userPrefs.put(PYTHON_PATH_KEY, System.getenv("PYTHONPATH"));
                pythonPath = System.getenv("PYTHONPATH");
                pythonPathExists = true;
            } else {
                pythonPath = "Could not automatically find python interpreter";
            }
        } else {
            pythonPathExists = true;
        }

        /* Gamess Settings Initialization */
        gamessServer = userPrefs.get(GAMESS_SERVER_KEY, "check");
        gamessUsername = userPrefs.get(GAMESS_USERNAME_KEY, "check");
        gamessPassword = userPrefs.get(GAMESS_PASSWORD_KEY, "check");
        gamessOutputPath = userPrefs.get(GAMESS_OUTPUT_KEY, "check");

        /* libEFP Settings Initialization */
        libefpServer = userPrefs.get(LIBEFP_SERVER_KEY, "check");
        libefpUsername = userPrefs.get(LIBEFP_USERNAME_KEY, "check");
        libefpPassword = userPrefs.get(LIBEFP_PASSWORD_KEY, "check");
        libefpOutputPath = userPrefs.get(LIBEFP_OUTPUT_KEY, "check");
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

    public static String getGamessServer() {
        return gamessServer;
    }

    public static String getGamessUsername() {
        return gamessUsername;
    }

    public static String getGamessPassword() {
        return gamessPassword;
    }

    public static String getGamessOutputPath() {
        return gamessOutputPath;
    }

    public static String getLibefpServer(){
        return libefpServer;
    }

    public static String getLibefpUsername(){
        return libefpUsername;
    }

    public static String getLibefpPassword(){
        return libefpPassword;
    }

    public static String getLibefpOutputPath(){
        return libefpOutputPath;
    }

    public static void setUserParameterPath(String value) {
        userPrefs.put(USER_PARAMETER_PATH_KEY, value);
        userParameterPath = userPrefs.get(USER_PARAMETER_PATH_KEY, "check");
    }

    public static void setPythonPath(String value) {
        userPrefs.put(PYTHON_PATH_KEY, value);
        pythonPathExists = true;
        pythonPath = userPrefs.get(PYTHON_PATH_KEY, "check");
    }

    public static void setGamessServer(String value) {
        userPrefs.put(GAMESS_SERVER_KEY, value);
        gamessServer = userPrefs.get(GAMESS_SERVER_KEY, "check");
    }

    public static void setGamessUsername(String value) {
        userPrefs.put(GAMESS_USERNAME_KEY, value);
        gamessUsername = userPrefs.get(GAMESS_USERNAME_KEY, "check");
    }

    public static void setGamessPassword(String value) {
        userPrefs.put(GAMESS_PASSWORD_KEY, value);
        gamessPassword = userPrefs.get(GAMESS_PASSWORD_KEY, "check");
    }

    public static void setGamessOutputPath(String value) {
        userPrefs.put(GAMESS_OUTPUT_KEY, value);
        gamessOutputPath = userPrefs.get(GAMESS_OUTPUT_KEY, "check");
    }

    public static void setLibefpServer(String value){
        userPrefs.put(LIBEFP_SERVER_KEY, value);
        libefpOutputPath = userPrefs.get(LIBEFP_SERVER_KEY, "check");
    }

    public static void setLibefpUsername(String value){
        userPrefs.put(LIBEFP_USERNAME_KEY, value);
        libefpUsername = userPrefs.get(LIBEFP_USERNAME_KEY, "check");
    }

    public static void setLibefpPassword(String value){
        userPrefs.put(LIBEFP_PASSWORD_KEY, value);
        libefpPassword = userPrefs.get(LIBEFP_PASSWORD_KEY, "check");
    }

    public static void setLibefpOutputPath(String value){
        userPrefs.put(LIBEFP_OUTPUT_KEY, value);
        libefpOutputPath = userPrefs.get(LIBEFP_OUTPUT_KEY, "check");
    }
}
