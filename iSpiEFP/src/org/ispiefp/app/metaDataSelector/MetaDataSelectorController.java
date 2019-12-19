package org.ispiefp.app.metaDataSelector;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import org.ispiefp.app.Main;
import org.ispiefp.app.MetaData;

import java.util.ArrayList;
import java.util.List;

public class MetaDataSelectorController{

    @FXML
    private Parent root;

    @FXML
    private ListView fragmentList;

    List<String> fragments;
    @FXML
    public void initialize(){

        //ObservableList<String> fragments = FXCollections.observableArrayList();
        fragments = new ArrayList<>();
        for (MetaData md : Main.fragmentTree.getMetaDataIterator()) {
            System.out.println(md.getFragmentName());
            //fragmentList.getItems().add(md.getFragmentName() + " in " + md.getFromFile());
            fragments.add(md.getFragmentName());
        }
        fragmentList.setItems(FXCollections.observableArrayList(fragments));;
    }

    public void handleSelection(){
        MetaData selectedFragment = Main.fragmentTree.getMetaData(
                fragments.get(fragmentList.getSelectionModel().getSelectedIndex()));
        System.out.println("Selected: " + selectedFragment);

    }
}
