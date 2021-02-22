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

import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.ispiefp.app.server.ServerInfo;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Connection {
    private String keyPassword;
    private boolean isKeyBased;
    private boolean isprotectedKey;
    private ch.ethz.ssh2.Connection activeConnection;
    private ServerInfo server;
    private final Object key = new Object();

    public Connection(ServerInfo si, String keyPassword) {
        server = si;
        if (si.isSshKeyMethod()) {
            isKeyBased = true;
            if (si.isSshFileEncrypted()) isprotectedKey = true;
            if (keyPassword != null) this.keyPassword = keyPassword;
        }
    }


    public Connection(boolean isKeyBased) {
        this.isKeyBased = isKeyBased;
    }

    public Connection() {
        super();
    }

    public boolean connect() {
        try {
            if (isKeyBased) {
                activeConnection = new ch.ethz.ssh2.Connection(server.getHostname());
                /* Prompt the user for their password, but do not save it to any data structure which will be stored.
                keep it in main memory.
                */
                if (isprotectedKey && keyPassword == null) keyPassword = promptForPassword();
                activeConnection.connect();
                System.out.printf("Opening the PEM file at: %s%n", server.getSshKeyLocation());
                return activeConnection.authenticateWithPublicKey(server.getUsername(), new File(server.getSshKeyLocation()), keyPassword);
            } else {
                activeConnection = new ch.ethz.ssh2.Connection(server.getHostname());
                activeConnection.connect();
                return activeConnection.authenticateWithPassword(server.getUsername(), server.getPassword());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }

    /*
    This method is only used by the SettingsViewController to test whether or not the user is able to be
    authenticated on the server. The reason all of the variables are passed instead of obtained from a
    ServerInfo instance is because the ServerInfo for the server may not exist yet if they have not saved
    their profile.
     */
    public boolean connect(String hostname, boolean isKeyBased, boolean isprotectedKey,
                           String keyLocation, String username, String serverPassword) throws IOException {
        if (isKeyBased) {
            activeConnection = new ch.ethz.ssh2.Connection(hostname);
            /* Prompt the user for their password, but do not save it to any data structure which will be stored.
            keep it in main memory.
            */
            if (isprotectedKey && keyPassword == null) keyPassword = promptForPassword();
            activeConnection.connect();
            System.out.printf("Opening the PEM file at: %s%n", keyLocation);
            return activeConnection.authenticateWithPublicKey(username, new File(keyLocation), keyPassword);
        } else {
            activeConnection = new ch.ethz.ssh2.Connection(hostname);
            activeConnection.connect();
            return activeConnection.authenticateWithPassword(hostname, serverPassword);
        }
    }

    public String promptForPassword() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(String.format("Enter password for encrypted ssh private key file: %s", server.getSshKeyLocation()));

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Enter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(false);


        grid.add(new Label("Password:"), 0, 0);
        grid.add(password, 1, 0);


        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> password.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return password.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        if (!result.isPresent()) {
            //user hit cancel
        }
        return password.getText();
    }

    public void close(){
        activeConnection.close();
    }

    public SCPClient createSCPClient() throws IOException {
        return activeConnection.createSCPClient();
    }

    public Session openSession() throws IOException {
        return activeConnection.openSession();
    }

    public String getKeyPassword(){
        return keyPassword;
    }

    public ch.ethz.ssh2.Connection getActiveConnection() {
        return activeConnection;
    }
}
