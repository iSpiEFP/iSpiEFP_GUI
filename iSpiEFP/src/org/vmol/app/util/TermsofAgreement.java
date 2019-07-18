package org.vmol.app.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class TermsofAgreement {

    private Alert alert;

    private final String msg = "Welcome to iSpiEFP\n\n"
            + "I acknowledge that this is a Pre-Alpha Release which means my jobs and data can be damaged or lost.\n"
            + "My credentials will be safe however.\n"
            + "This application will install a directory: '/iSpiClient' on a remote machine and\n"
            + "a working directory on the local machine in the app's current directory: '/iSpiWorkspace for storing data'\n\n"
            + "This product is new and will have lots of bugs. There are no loading bars so please wait\n"
            + "a few seconds while submitting jobs, searching for parameters, and configuring servers.\n"
            + "If I encounter a freeze or serious issue I will shake the app or restart it.\n"
            + "If I encounter an issue I will report it.\n";

    public TermsofAgreement() {

    }

    public void show() {
        alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Terms of Agreement");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        Optional<ButtonType> result = alert.showAndWait(); //Terms of Agreement
    }

    public void close() {
        alert.close();
    }
}
