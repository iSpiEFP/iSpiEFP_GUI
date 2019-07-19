package org.libefp.app.util;

import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgressIndicatorTest {

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


}