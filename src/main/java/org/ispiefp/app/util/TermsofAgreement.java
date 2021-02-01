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

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class TermsofAgreement {

    private Alert alert;

    private final String msg = "Welcome to iSpiEFP\n\n"
            + "I acknowledge that this is a Pre-Alpha Release which means my jobs and data can be damaged or lost.\n"
            + "My credentials will be safe however.\n"
            + "This application will install a directory: \"iSpiClient\" on a remote machine and\n"
            + "a working directory (\"iSpiWorkspace for storing data\")on the local machine in the app's current directory.\n"
            + "This product is new and will have lots of bugs. There are no loading bars so please wait\n"
            + "a few seconds while submitting jobs, searching for parameters, and configuring servers.\n"
            + "If I encounter a freeze or serious issue I will shake the app or restart it.\n"
            + "If I encounter an issue I will report it.\n";

//    private final String msg = "Welcome to iSpiEFP\n\n"
//            + "I acknowledge that this is a Pre-Alpha Release which means my jobs and data can be damaged or lost.\n"
//            + "My credentials will be safe however.\n"
//            + "This application will install a directory: \"\\iSpiClient\" on a remote machine and\n"
//            + "a working directory on the local machine in the app's current directory: \"iSpiWorkspace for storing data\"'\n"
//            + "This product is new and will have lots of bugs. There are no loading bars so please wait\n"
//            + "a few seconds while submitting jobs, searching for parameters, and configuring servers.\n"
//            + "If I encounter a freeze or serious issue I will shake the app or restart it.\n"
//            + "If I encounter an issue I will report it.\n";

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
