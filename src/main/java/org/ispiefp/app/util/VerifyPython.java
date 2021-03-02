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

package org.ispiefp.app.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

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
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            noDefaultPythonInterpreterError,
                            ButtonType.OK);
                    alert.showAndWait();
                }
            });
            return false;
        }
        if (scriptOutput == null) {
            String invalidPythonPath = "The path to your python interpreter specified in your settings is either an " +
                    "invalid path or is not a path to a python interpreter. You will be unable to select a fragment" +
                    " until this field is updated in your settings.";
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            invalidPythonPath,
                            ButtonType.OK);
                    alert.showAndWait();
                }
            });
            return false;
        }
        if (scriptOutput.equals("invalid python version")) {
            String invalidPythonVersionError = "Your selected python interpreter is not at least version number 3.0 " +
                    "Please select a different python interpreter.";
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            invalidPythonVersionError,
                            ButtonType.OK);
                    alert.showAndWait();
                }
            });
            return false;
        }
        System.out.printf("Script output us %s%n", scriptOutput);
        if (scriptOutput.matches(".* is not installed")) {
            String missingModule = scriptOutput.split(" ")[0];
            String noModule = String.format("iSpiEFP uses the python module %s to perform RMSD calculations. " +
                            "We cannot detect %s within the loadable modules of your selected interpreter. Please " +
                            "install numpy or choose a different interpreter which has %s",
                    missingModule, missingModule, missingModule);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            noModule,
                            ButtonType.OK);
                    alert.showAndWait();
                }
            });
            return false;
        }
        return false;
    }

    public static void raisePythonError() {

    }
}