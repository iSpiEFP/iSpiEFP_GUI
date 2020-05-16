/**
 * Helper class for executing Python Scripts.
 * Written by Ryan DeRue 4/15/2020
 */
package org.ispiefp.app.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class ExecutePython {

    /**
     * This method returns as a String the output and error of the python script which is passed as an argument when
     * executed with the command line arguments that are passed as the second argument.
     *
     * This method makes a few assumptions.
     *  1) The script is located in the resources/scripts directory as all scripts should be
     *  2) The user of iSpiEFP has a properly configured python path.
     *
     *  If either of the above two assumptions fail, or execution of the process fails, the method returns null so
     *  ensure that when you use this method you do appropriate error checking.
     * @param scriptName Name of the script located in the resources/scripts directory
     * @param commandLineArgs Everything that would follow after python <script name> on the command line
     * @return The out and err of the executed process
     */
    public static String runPythonScript(String scriptName, String commandLineArgs) {
        StringBuilder sb = new StringBuilder();
        String scriptPath = null;
        try {
            URL resource = ExecutePython.class.getResource("/scripts/" + scriptName);
            File file = Paths.get(resource.toURI()).toFile();
            scriptPath = file.getAbsolutePath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        if (!UserPreferences.pythonPathExists()) return null;
        String commandInput = String.format("%s %s %s", UserPreferences.getPythonPath(), scriptPath, commandLineArgs);
        try {
            Process p = Runtime.getRuntime().exec(commandInput);   /* The path of the directory to write to */
            BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            /* p.getInputStream() is a strange function which also returns the output stream, see API */
            BufferedReader outReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s1 = "";
            String s2 = "";
            while ((s1 = errReader.readLine()) != null || (s2 = outReader.readLine()) != null) {
                sb.append(s1);
                sb.append(s2);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return sb.toString();
    }
}

