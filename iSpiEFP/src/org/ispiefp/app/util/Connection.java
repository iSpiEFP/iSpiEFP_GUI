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

    public Connection(ServerInfo si, String keyPassword)
    {
        server = si;
        if (si.isSshKeyMethod()){
            isKeyBased = true;
            if (si.isSshFileEncrypted()) isprotectedKey = true;
            if (keyPassword != null) this.keyPassword = keyPassword;
        }
    }



    public Connection(boolean isKeyBased){
        this.isKeyBased = isKeyBased;
    }

    public boolean connect(){
        try{
            if (isKeyBased) {
                activeConnection = new ch.ethz.ssh2.Connection(server.getHostname());
                /* Prompt the user for their password, but do not save it to any data structure which will be stored.
                keep it in main memory.
                */
                if (isprotectedKey && keyPassword == null) keyPassword = promptForPassword();
                activeConnection.connect();
                System.out.printf("Opening the PEM file at: %s%n", server.getSshKeyLocation());
                return activeConnection.authenticateWithPublicKey(server.getUsername(), new File(server.getSshKeyLocation()), keyPassword);
            }
            else {
                activeConnection = new ch.ethz.ssh2.Connection(server.getHostname());
                activeConnection.connect();
                return activeConnection.authenticateWithPassword(server.getUsername(), server.getPassword());
            }
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
        return false;
    }

    public String promptForPassword(){
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Enter password to access key");

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
