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

    public static boolean isValidPython() {

        String scriptOutput = ExecutePython.runPythonScript("testPythonInterpreterVersion.py", "");
        System.out.println(scriptOutput);
        if (scriptOutput != null &&
                !scriptOutput.matches(".* is not installed") &&
                !scriptOutput.equals("invalid python version")) {
            return true;
        }
        String noDefaultPythonInterpreterError = "iSpiEFP is currently unable to find your Python interpreter" +
                " by using your system's environment variables. You will be unable to select any fragments" +
                " until you select a valid Python interpreter (Python3 3.0 or higher) from File -> Settings" +
                " -> Default Path Settings";
        if (!UserPreferences.pythonPathExists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    noDefaultPythonInterpreterError,
                    ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        if (scriptOutput == null){
            String invalidPythonPath = "The path to your python interpreter specified in your settings is either an " +
                    "invalid path or is not a path to a python interpreter. You will be unable to select a fragment" +
                    " until this field is updated in your settings.";
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    invalidPythonPath,
                    ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        if (scriptOutput.equals("invalid python version")){
            String invalidPythonVersionError = "Your selected python interpreter is not at least version number 3.0 " +
                    "Please select a different python interpreter.";
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    invalidPythonVersionError,
                    ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        System.out.printf("Script output us %s%n", scriptOutput);
        if (scriptOutput.matches(".* is not installed")) {
            String missingModule = scriptOutput.split(" ")[0];
            String noModule = String.format("iSpiEFP uses the python module %s to perform RMSD calculations. " +
                            "We cannot detect %s within the loadable modules of your selected interpreter. Please " +
                            "install numpy or choose a different interpreter which has %s",
                    missingModule, missingModule, missingModule);
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    noModule,
                    ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        return false;
    }

    public static void raisePythonError() {

    }
}