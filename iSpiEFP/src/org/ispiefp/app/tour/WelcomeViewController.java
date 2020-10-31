package org.ispiefp.app.tour;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.ispiefp.app.Main;

import javax.swing.*;
import java.io.IOException;

public class WelcomeViewController {
    private String name;

    public WelcomeViewController() throws IOException {
    }

    public WelcomeViewController(String name){
        this.name = name;
    }

    public void initialize(){
        System.out.println("ddooodfowfefew");
    }
@FXML
    public void skipButtonPushed(ActionEvent event){
       //.close();
    }
@FXML
    public void yesButtonPushed(ActionEvent event) throws IOException {
        Parent tableViewParent = FXMLLoader.load(getClass().getResource("TourWindowOne"));
        Scene tableViewScene = new Scene(tableViewParent);

        //this line gets the stage information
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

        window.close();
//    window.show();
  //  window.setScene(tableViewScene);


}
}
