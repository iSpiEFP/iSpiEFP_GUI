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

import com.sun.javafx.robot.FXRobot;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.ispiefp.app.Initializer;
import org.ispiefp.app.Main;
import org.ispiefp.app.util.CheckInternetConnection;
import org.ispiefp.app.util.VerifyPython;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainViewTests extends ApplicationTest {
    FXRobot robot;
    boolean hasInternetConnection;

    @Override
    public void start(Stage stage) throws Exception {
        Parent mainNode = FXMLLoader.load(Main.class.getResource("/views/MainView.fxml"));
        stage.setScene(new Scene(mainNode));
        stage.show();
        stage.toFront();
    }

    @Before
    public void setUp() throws Exception {
        new Initializer().init();
        hasInternetConnection = CheckInternetConnection.checkInternetConnection();
        if (!hasInternetConnection) System.err.println("There is no internet connection. " +
                "Only tests which don't require an internet connection will be ran.");

    }


    @After
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    @Test
    public void testServerSetup() {
        clickOn("#fileButton");
        clickOn("#settingsButton");
    }

    @Test
    public void testPythonSetup() {
        /* Open Settings */
        clickOn("#fileButton");
        clickOn("#settingsButton");

        /* Save faulty Python Path */
        window("Settings");
        clickOn("#pythonPathField");
        write(".exe");
        clickOn("#pathsSave");
        Assert.assertFalse(VerifyPython.isValidPython());
        Stage settingsWindow = getTopModalStage();
        Assert.assertNotNull(settingsWindow);
        sleep(1000);
        clickOn("OK");
        Platform.runLater(() -> {
            settingsWindow.close();
        });

        /* Open Settings */
        clickOn("#fileButton");
        clickOn("#settingsButton");

        /* Save true Python Path */
        window("Settings");
        clickOn("#pythonPathField");
        write("/usr/local/bin/python3");
        clickOn("#pathsSave");
        Assert.assertTrue(VerifyPython.isValidPython());
    }

    /* Taken from here: https://stackoverflow.com/questions/48565782/testfx-how-to-test-validation-dialogs-with-no-ids
     */
    private javafx.stage.Stage getTopModalStage() {
        // Get a list of windows but ordered from top[0] to bottom[n] ones.
        // It is needed to get the first found modal window.
        final List<Window> allWindows = new ArrayList<Window>(robotContext().getWindowFinder().listWindows());
        Collections.reverse(allWindows);

        return (javafx.stage.Stage) allWindows
                .stream()
                .filter(window -> window instanceof javafx.stage.Stage)
                .filter(window -> ((javafx.stage.Stage) window).getModality() == Modality.APPLICATION_MODAL)
                .findFirst()
                .orElse(null);
    }
}
