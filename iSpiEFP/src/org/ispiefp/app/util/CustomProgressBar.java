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

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Example Code + Explanation in order for the progress bar to show up:
 */
/*
  // 1. The code that needs the progress bar needs to go in the following segment within the call function
  Task task = new Task() {
      @Override
      protected Object call() throws Exception {
          // ADD EXECUTION CODE RIGHT HERE

          // example of how to update progress. The next line will show the progress bar at 25% complete.
          updateProgress(1, 4);

          // The next line will show the progress bar at 1% complete. I think you get the idea.
          updateProgress(1, 100);

          // make sure to return something. Otherwise, the task does not compile. Usually null should work.
          return null;
      }
  };

  // 2. call this class, pass in the task you made.
  // If you know the progress will not get to 100%, save the variable, and call destroyStage()
  new CustomProgressBar(task);
  // Optionally, you can pass in a text to be displayed under the progress bar
  new CustomProgressBar(task, "Calculation");

  // 3. make a new thread to start the progress bar
  new Thread(task).start();

  // 4. VERY IMPORTANT. If you have code that needs to be executed AFTER the task finishes.
  // Unfortunately, It has to be within this block. Otherwise, the task will run concurrently.
  // For example, if you task was to load 100 molecules, and after it loaded, you need to display a screen.
  // Displaying the screen needs to be within the setOnSucceeded lambda block.
  task.setOnSucceeded(event -> {
 
  });
 */

public class CustomProgressBar {

    ProgressBar progressBar;
    Label label;
    Task task;
    Stage stage;
    String text;

    private double xOffset = 0;
    private double yOffset = 0;

    public CustomProgressBar(Task task) {
        this(task, "");
    }

    public CustomProgressBar(Task task, String text) {
        progressBar = new ProgressBar();
        this.task = task;
        this.text = text;
        init();
    }

    private void init() {
        stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setHeight(80);
        stage.setWidth(600);
        stage.setResizable(false);

        progressBar.setProgress(0.0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(50);

        task.setOnSucceeded(event -> stage.close());
        progressBar.progressProperty().bind(task.progressProperty());
        task.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() == 1) stage.close();
        });

        label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPrefHeight(20);

        VBox vBox = new VBox(label, progressBar);
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.setMaxWidth(Double.MAX_VALUE);
        vBox.setPadding(new Insets(10, 10, 10, 10));

        // dragging
        vBox.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        vBox.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.show();

    }

    public void destroyStage() {
        stage.close();
    }
}
