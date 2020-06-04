/**
 * Helper class for executing Python Scripts.
 * Written by Ryan DeRue 4/15/2020
 */
package org.ispiefp.app.util;

import org.apache.commons.io.FileUtils;
import org.ispiefp.app.Initializer;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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
     * @return The out of the executed process
     */
    public static String runPythonScript(String scriptName, String commandLineArgs) {
        /* Determine whether we are in a JAR or an IDE */
        String protocol = ExecutePython.class.getResource("").getProtocol();
        if (protocol.equals("jar")) {
            StringBuilder sb = new StringBuilder();
            File script = null;
            String jarLocation;
            java.nio.file.FileSystem fileSystem = null;
            try {
                try {
                    jarLocation = new File(ExecutePython.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
                    Path path = Paths.get(jarLocation);
                    URI uri = new URI("jar", path.toUri().toString(), null);

                    Map<String, String> env = new HashMap<>();
                    env.put("create", "true");

                    fileSystem = FileSystems.newFileSystem(uri, env);
                    InputStream is = ExecutePython.class.getResourceAsStream("/scripts/" + scriptName);
                    script = File.createTempFile("pythonScript", null);
                    script.deleteOnExit();
                    FileUtils.copyInputStreamToFile(is, script);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                if (!UserPreferences.pythonPathExists()) return null;
                String commandInput = String.format("%s %s %s", UserPreferences.getPythonPath(), script.getAbsolutePath(), commandLineArgs);
                try {
                    Process p = Runtime.getRuntime().exec(commandInput);   /* The path of the directory to write to */
                    BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    /* p.getInputStream() is a strange function which also returns the output stream, see API */
                    BufferedReader outReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String s1 = "";
                    String s2 = "";
                    System.out.printf("stderr for script %s:%n", scriptName);
                    while ((s1 = errReader.readLine()) != null || (s2 = outReader.readLine()) != null) {
                        //sb.append(s1);
                        System.out.println(s1);
                        sb.append(s2);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                return sb.toString();
            } finally {
                try {
                    if (fileSystem != null) fileSystem.close();
                } catch (IOException e) {
                    System.err.println("Could not close filesystem created for JAR");
                }
            }
        }
        /* IDE Case */
        else {
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
                System.out.printf("stderr for script %s:%n", scriptName);
                while ((s1 = errReader.readLine()) != null || (s2 = outReader.readLine()) != null) {
//                    sb.append(s1);
                    System.out.println(s1);
                    sb.append(s2);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return sb.toString();
        }
    }
}

