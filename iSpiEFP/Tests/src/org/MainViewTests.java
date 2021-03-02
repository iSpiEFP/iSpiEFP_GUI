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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.ispiefp.app.Initializer;
import org.ispiefp.app.Main;
import org.ispiefp.app.util.CheckInternetConnection;
import org.ispiefp.app.util.TestFileParser;
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
import java.util.Optional;


public class MainViewTests extends ApplicationTest {
    FXRobot robot;
    boolean hasInternetConnection;
    String username; /* Server username */
    String hostname; /* Hostname of server */
    String sshKeyPath; /* Path to the ssh key */
    String pythonPath; /* Path to python interpreter */

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
        TestFileParser tfp = new TestFileParser();
        username = tfp.getUsername();
        hostname = tfp.getHostname();
        sshKeyPath = tfp.getSshKeyPath();
        pythonPath = tfp.getPythonPath();
    }

    @After
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    @Test
    /**
     * Creates a new instance of a server, selects authentication via ssh key file,
     * that ssh key file is unencrypted, and then ensures it can authenticate the server.
     */
    public void testServerSetupAndAuthSSHKeyNoEncryptSuccess() {
        createDummyServer("dummy");
        /* Set up a server that works */
        setUpServer(true, null);

        /* Test that the server was authenticated properly */
        Stage s = getTopModalStage();
        DialogPane dp = (DialogPane) s.getScene().getRoot();
        Assert.assertEquals(dp.getContentText(), "Was able to connect to and authenticate user");
    }

    @Test
    /**
     * Creates a new instance of a server, selects authentication via ssh key file (that does not exist),
     * that ssh key file is unencrypted, and then ensures it cannot authenticate the server.
     */
    public void testServerSetupAndAuthSSHKeyNoEncryptFailure() {
        createDummyServer("dummy");
        /* Set up a server that works */
        setUpServer("halstead.rcac.purdue.edu", "rderue", true,
                "DNE", null);

        /* Test that the server was authenticated properly */
        Stage s = getTopModalStage();
        DialogPane dp = (DialogPane) s.getScene().getRoot();
        Assert.assertEquals(dp.getContentText(),
                String.format("Was unable to connect to %s with your credentials", "halstead.rcac.purdue.edu")
        );
    }

    @Test
    public void testSaveNewServer() {
        openSettings();

        /* Get handle to the menu tree */
        Optional<TreeView<String>> tvOpt = lookup("#menuTree").tryQuery();
        Assert.assertTrue(tvOpt.isPresent());
        TreeView<String> menu = tvOpt.get();
        TreeView<String> finalMenu1 = menu;
        Platform.runLater(() -> {
            finalMenu1.getSelectionModel().select(finalMenu1.getTreeItem(1).getChildren().get(0)); //Select "Add new"
        });
        int initNumServers = menu.getTreeItem(1).getChildren().get(1).getChildren().size();

        sleep(1000);

        /* Handle entering Dialog into pop-up */
        Stage s = getTopModalStage();
        DialogPane dp = (DialogPane) s.getScene().getRoot();
        GridPane newServerPromptGrid = (GridPane) dp.getContent();
        clickOn(getNodeFromGridPane(newServerPromptGrid, 1, 0)); // Clicks on TextField
        write("alias");
        clickOn("Save Server Alias");
        sleep(1000);
        int finalNumServers = menu.getTreeItem(1).getChildren().get(1).getChildren().size();
        Assert.assertEquals(initNumServers + 1, finalNumServers);
        Platform.runLater(() -> getTopModalStage().close());
        openSettings();

        tvOpt = lookup("#menuTree").tryQuery();
        Assert.assertTrue(tvOpt.isPresent());
        menu = tvOpt.get();
        TreeView<String> finalMenu = menu;
        Platform.runLater(() -> {
            finalMenu.getSelectionModel().select(finalMenu.getTreeItem(1).getChildren().get(0)); //Select "Add new"
        });
        int recheckNumServers = menu.getTreeItem(1).getChildren().get(1).getChildren().size();
        Assert.assertNotEquals(recheckNumServers, finalNumServers);
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
        write(pythonPath);
        clickOn("#pathsSave");
        Assert.assertTrue(VerifyPython.isValidPython());
    }

    @Test
    public void testFileSettings() {
        TestFileParser tfp = new TestFileParser();
        Assert.assertTrue(tfp.fileExists());
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

    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    private void setUpServer(boolean isSSHKey, String password) {
        clickOn("#hostname");
        write(hostname);
        clickOn("#username");
        write(username);
        clickOn("#signInMethodComboBox");
        if (isSSHKey) {
            type(KeyCode.UP);
            type(KeyCode.ENTER);
            clickOn("#signInFileLocationField");
            write(sshKeyPath);
        } else {
            clickOn("#signInPasswordField");
            write(password);
            type(KeyCode.ENTER);
        }
        clickOn("#serverAuth");
        sleep(4000);
    }

    private void setUpServer(String hostname, String username, boolean isSSHKey, String sshKeyPath,
                             String password) {
        clickOn("#hostname");
        write(hostname);
        clickOn("#username");
        write(username);
        clickOn("#signInMethodComboBox");
        if (isSSHKey) {
            type(KeyCode.UP);
            type(KeyCode.ENTER);
            clickOn("#signInFileLocationField");
            write(sshKeyPath);
        } else {
            clickOn("#signInPasswordField");
            write(password);
            type(KeyCode.ENTER);
        }
        clickOn("#serverAuth");
        sleep(4000);
    }

    /*
    The functions below are convenience methods for opening different windows, but they assume that they
    are invoked from MainView.
     */

    public void openSettings() {
        /* Open Settings */
        clickOn("#fileButton");
        clickOn("#settingsButton");
    }

    public void createDummyServer(String alias) {
        openSettings();

        /* Get handle to the menu tree */
        Optional<TreeView<String>> tvOpt = lookup("#menuTree").tryQuery();
        Assert.assertTrue(tvOpt.isPresent());
        TreeView<String> menu = tvOpt.get();
        Platform.runLater(() -> {
            menu.getSelectionModel().select(menu.getTreeItem(1).getChildren().get(0)); //Select "Add new"
        });
        sleep(1000);

        /* Handle entering Dialog into pop-up */
        Stage s = getTopModalStage();
        DialogPane dp = (DialogPane) s.getScene().getRoot();
        GridPane newServerPromptGrid = (GridPane) dp.getContent();
        clickOn(getNodeFromGridPane(newServerPromptGrid, 1, 0)); // Clicks on TextField
        write(alias);
        clickOn("Save Server Alias");
        sleep(1000);
    }
}
