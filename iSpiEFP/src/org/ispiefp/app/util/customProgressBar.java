package org.ispiefp.app.util;

import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Example Code + Explanation in order for the progress bar to show up:
 */
/*
  // 1. The code that needs the progress bar needs to go in the following segment within the call function
  // Task task = new Task() {
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
  new customProgressBar(task);

  // 3. make a new thread to start the progress bar
  // new Thread(task).start();

  // 4. VERY IMPORTANT. If you have code that needs to be executed AFTER the task finishes.
  // Unfortunately, It has to be within this block. Otherwise, the task will run concurrently.
  // For example, if you task was to load 100 molecules, and after it loaded, you need to display a screen.
  // Displaying the screen needs to be within the setOnSucceeded lambda block.
  task.setOnSucceeded(event -> {
 
  });
 */

public class customProgressBar {

    ProgressBar progressBar;
    Task task;
    Stage stage;

    public customProgressBar(Task task) {
        progressBar = new ProgressBar();
        this.task = task;
        init();
    }

    private void init() {
        stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setHeight(30);
        stage.setWidth(600);

        progressBar.setProgress(0.0);

        task.setOnSucceeded(event -> stage.close());
        progressBar.progressProperty().bind(task.progressProperty());
        task.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() == 1) stage.close();
        });

        Scene scene = new Scene(progressBar);
        stage.setScene(scene);
        stage.show();

    }

    public void destroyStage() {
        stage.close();
    }
}
