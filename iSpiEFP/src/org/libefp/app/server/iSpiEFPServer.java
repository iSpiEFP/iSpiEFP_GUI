package org.libefp.app.server;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * Handle iSpiEFP Server Connection and Errors
 */
public class iSpiEFPServer {

    public iSpiEFPServer() {

    }

    /**
     * Attempt Connection to the Server
     * @param serverName
     * @param port
     * @return socket on success
     */
    public Socket connect(String serverName, int port) {
        Socket client;
        try {
            client = new Socket(serverName, port);
            return client;
        } catch (UnknownHostException e) {
            //client not connected to internet
            alertClientNotConnected();
            e.printStackTrace();
        } catch (ConnectException e) {
            //iSpiEFPServer is Down
            alertServerDown();
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //unprocessed error
            e.printStackTrace();
        }
        return null;
    }

    private void alertServerDown() {
        System.out.println("Client Not Connected to Internet!!!");
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Connection Error");
        alert.setHeaderText(null);
        alert.setContentText("It Looks Like the iSpiEFP Server is Down!\nPlease Call/Text Addison Polcyn: (408)888-8161\nIf not busy I can get it up and running again\n");

        Optional<ButtonType> result = alert.showAndWait();
    }

    private void alertClientNotConnected() {
        System.out.println("Client Not Connected to Internet!!!");
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Connection Error");
        alert.setHeaderText(null);
        alert.setContentText("You are not connect to the internet!");

        Optional<ButtonType> result = alert.showAndWait();
    }
}


