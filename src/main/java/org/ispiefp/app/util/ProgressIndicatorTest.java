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

import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgressIndicatorTest {

    ProgressIndicator pit;

    public ProgressIndicatorTest() {

    }


    public void fire() {
        Stage primaryStage = new Stage();
        StackPane root = new StackPane();
        ProgressIndicator pi = new ProgressIndicator();

        pi.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        pi.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
        root.setStyle("-fx-background-color: transparent;");
        root.getChildren().add(pi);
        root.toFront();
        Scene scene = new Scene(root, 400, 400);
        pi.getScene().getRoot().setStyle("-fx-background-color: transparent");
        scene.getStylesheets().add(ProgressIndicatorTest.class.getResource("progress.css").toExternalForm());
        scene.setFill(null);

        primaryStage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);


        //scene.getStylesheets().add("progress.css");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.toFront();
    }

    public void kill() {
        pit.setProgress(0.0);
        pit = null;

    }


}