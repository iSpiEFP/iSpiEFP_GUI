package org.ispiefp.app.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.ispiefp.app.installer.LocalBundleManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class VerifyPython {

    public static boolean isValidPython(){
//        String testPythonScriptPath = null;
//        StringBuilder sb = new StringBuilder();
//        try {
//            URL resource = VerifyPython.class.getResource("/scripts/testPythonInterpreterVersion.py");
//            File file = Paths.get(resource.toURI()).toFile();
//            testPythonScriptPath = file.getAbsolutePath();
//        } catch (URISyntaxException e){
//            e.printStackTrace();
//        }
//        System.out.println(UserPreferences.getPythonPath());
//        if (!UserPreferences.pythonPathExists()) return false;
//        String commandInput = String.format("%s %s",UserPreferences.getPythonPath(),
//                testPythonScriptPath);
//        try{
//            Process p = Runtime.getRuntime().exec(commandInput);   /* The path of the directory to write to */
//            BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//            /* p.getInputStream() is a strange function which also returns the output stream, see API */
//            BufferedReader outReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            String s1 = "";
//            String s2 = "";
//            while ((s1 = errReader.readLine()) != null || (s2 = outReader.readLine()) != null){
//                sb.append(s1);
//                sb.append(s2);
//            }
//        } catch (IOException e){
//            e.printStackTrace();
//            return false;
//        }
//        System.out.println(sb.toString());
        String scriptOutput = ExecutePython.runPythonScript("testPythonInterpreterVersion.py", "");
        System.out.println(scriptOutput);
        if (scriptOutput == null) return false;
        return scriptOutput.equals("") || scriptOutput.equals("numpy is not installed");
    }

    public static void raisePythonError(){
        String noPythonInterpreterError = "iSpiEFP is currently unable to find your Python interpreter" +
                " by using your system's environment variables. You will be unable to select any fragments" +
                " until you select a valid Python interpreter (Python3 3.0 or higher) from File -> Settings" +
                " -> Default Path Settings";
        if (!UserPreferences.pythonPathExists() || !VerifyPython.isValidPython()){
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    noPythonInterpreterError,
                    ButtonType.OK);
            alert.showAndWait();
            return;
        }
        String scriptOutput = ExecutePython.runPythonScript("testPythonInterpreterVersion.py", "");
        if (scriptOutput.equals("numpy is not installed")) {
            String noNumpy = "iSpiEFP uses the python module numpy to perform RMSD calculations. We cannot detect numpy" +
                    " within the modules of your selected interpreter. Please install numpy or choose a different interpreter" +
                    " which has numpy";
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    noNumpy,
                    ButtonType.OK);
            alert.showAndWait();
            return;
        }
    }
}