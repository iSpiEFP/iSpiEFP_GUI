package org.ispiefp.app.util;
import org.ispiefp.app.installer.LocalBundleManager;
import org.ispiefp.app.libEFP.CalculationPreset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.ispiefp.app.util.AESEncryption.decrypt;
import static org.ispiefp.app.util.AESEncryption.encrypt;
import java.security.SecureRandom;


public class UserPreferences {
    /* Keys for user preferences */
    /* Random string implementation borrowed from mkyong.com*/
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
    private static final String ENCRYPT_KEY = "encryptionKey";
    private static final String LIBEFP_PRESETS_KEY = "libefpPresets";
    private static final String LIBEFP_RJOBS_KEY = "libefprunningjobs";

    private static String userParameterPath = null;
    private static String pythonPath = null;
    private static String gamessServer = null;
    private static String gamessUsername = null;
    private static String gamessPassword = null;
    private static String gamessOutputPath = null;
    private static String libefpServer = null;
    private static String libefpUsername = null;
    private static String libefpPassword = null;
    private static String libefpOutputPath = null;
    private static String libefpRunningJobs = null;
    private static HashMap<String, CalculationPreset> libefpPresets;

    private static SecureRandom random = new SecureRandom();
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";

    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;


    //These fields were added for security. User's GAMESS and LibEFP usernames and passwords are encrypted using AES method
    // with ad hoc class called AESEncryption.java.
    private static String encrypGamessUser = null;
    private static String encryGamessPass = null;
    private static String encrypLibEFPUser = null;

    private static String encrypLibEFPPass = null;
    private static String secretKey = generateRandomString(12);

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


        userPrefs.put(ENCRYPT_KEY, secretKey);
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

        /* libEFP preset Initialization */
        libefpPresets = new HashMap<>();
        String encodedString = userPrefs.get(LIBEFP_PRESETS_KEY, "check");
        if (!encodedString.equals("check")){
            String [] predefinedStringArray = encodedString.split("%@%");
            for (int i = 0; i < predefinedStringArray.length; i++){
                CalculationPreset newCP = new CalculationPreset(predefinedStringArray[i]);
                libefpPresets.put(newCP.getTitle(), newCP);
            }
        }
    }

    public static void addLibEFPPreset(CalculationPreset cp){
        String encodedString = userPrefs.get(LIBEFP_PRESETS_KEY, "check");
        System.out.printf("encoded string is %s%n", encodedString);
        if (encodedString.equals("check")){
            userPrefs.put(LIBEFP_PRESETS_KEY, cp.getCalculationPresetDefinedString());
        }
        else {
            if (!libefpPresets.containsKey(cp.getTitle())) {
                userPrefs.put(LIBEFP_PRESETS_KEY, encodedString + "%@%" + cp.getCalculationPresetDefinedString());
            }
            libefpPresets.put(cp.getTitle(), cp);
        }
    }
    public static void removeLibEFPPreset(String name){
        libefpPresets.remove(name);
        String encodedString = userPrefs.get(LIBEFP_PRESETS_KEY, "check");
        String [] simpleString = encodedString.split("%@%");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < simpleString.length; i++){
            if (simpleString[i].equals(name)) continue;
            sb.append(simpleString[i]);
            if (i != simpleString.length - 1) sb.append("%@%");
        }
        userPrefs.put(LIBEFP_PRESETS_KEY, sb.toString());
    }

    public static HashMap<String, CalculationPreset> getLibEFPPresets(){
        return libefpPresets;
    }

    public static String generateRandomString(int length) {
        if (length < 1) throw new IllegalArgumentException();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {

            // 0-62 (exclusive), random returns 0-61
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

            // debug
            System.out.format("%d\t:\t%c%n", rndCharAt, rndChar);

            sb.append(rndChar);

        }

        return sb.toString();

    }

    public static String getUserParameterPath() {
        return userParameterPath;
    }

    public static String getPythonPath() {
        return pythonPath;
    }

    public static String getSecretKey() {
        return userPrefs.get(ENCRYPT_KEY, "check");
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

    public static Set<String> getLibEFPPresetNames(){
        return libefpPresets.keySet();
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

        try {
            encrypGamessUser = encrypt(value, secretKey);
            userPrefs.put(GAMESS_USERNAME_KEY, encrypGamessUser);
//        userPrefs.put(GAMESS_USERNAME_KEY, value);
            gamessUsername = decrypt(userPrefs.get(GAMESS_USERNAME_KEY, "check"), secretKey);
        }

        catch(Exception e) {
            System.out.println("Problem accessing GAMESS username and/or with its encryption");
            e.printStackTrace();
        }
    }

    public static void setGamessPassword(String value) {
       try {
           encryGamessPass = encrypt(value, secretKey);

           userPrefs.put(GAMESS_USERNAME_KEY, encryGamessPass);
           //userPrefs.put(GAMESS_PASSWORD_KEY, value);
           gamessPassword = decrypt(userPrefs.get(GAMESS_PASSWORD_KEY, "check"), secretKey);
       }
       catch(Exception e) {
           System.out.println("Problem accessing GAMESS password and/or with its encryption");
           e.printStackTrace();
       }
    }

    public static void setGamessOutputPath(String value) {
        userPrefs.put(GAMESS_OUTPUT_KEY, value);
        gamessOutputPath = userPrefs.get(GAMESS_OUTPUT_KEY, "check");
    }

    public static void setLibefpServer(String value){
        userPrefs.put(LIBEFP_SERVER_KEY, value);
        libefpOutputPath = decrypt(userPrefs.get(LIBEFP_SERVER_KEY, "check"), secretKey);
    }

    public static void setLibefpUsername(String value){

        try {
            encrypLibEFPUser = encrypt(value, secretKey);
            userPrefs.put(LIBEFP_USERNAME_KEY, encrypLibEFPUser);

//        userPrefs.put(LIBEFP_USERNAME_KEY, value);
            libefpUsername = decrypt(userPrefs.get(LIBEFP_USERNAME_KEY, "check"), secretKey);

        }

        catch(Exception e) {
            System.out.println("Problem accessing LibEFP username and/or with its encryption");
            e.printStackTrace();
        }
    }

    public static void setLibefpPassword(String value){

        try {
            encrypLibEFPPass = encrypt(value, secretKey);
            userPrefs.put(LIBEFP_PASSWORD_KEY, encrypLibEFPPass);

            libefpPassword = decrypt(userPrefs.get(LIBEFP_PASSWORD_KEY, "check"), secretKey);
        }
        catch(Exception e) {
            System.out.println("Problem accessing LibEFP password and/or with its encryption");
            e.printStackTrace();
        }
    }

    public static void setLibefpOutputPath(String value){
        userPrefs.put(LIBEFP_OUTPUT_KEY, value);
        libefpOutputPath = userPrefs.get(LIBEFP_OUTPUT_KEY, "check");
    }
}
