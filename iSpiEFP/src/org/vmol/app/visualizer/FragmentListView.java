package org.vmol.app.visualizer;

import java.util.ArrayList;

import org.jmol.viewer.Viewer;
import org.vmol.app.Main;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;

public class FragmentListView {
    private ListView<String> listView;
    private JmolMainPanel jmolMainPanel;
    
    public FragmentListView(ListView<String> listView, JmolMainPanel jmolMainPanel) {
        this.listView = listView;
        this.jmolMainPanel = jmolMainPanel;
    }
    
    public void update(ArrayList<ArrayList<Integer>> fragment_list) {
        if(fragment_list == null) {
            return;
        }
        
        //load up fragment list
        ObservableList<String> data = FXCollections.observableArrayList();
        int fragmentCounter = 1;
        for (ArrayList<Integer> frag : fragment_list) {
            if (frag.size() > 0) {
                data.add("Fragment " + fragmentCounter++);
            }
        }
        listView.setItems(data);
        
        jmolMainPanel.viewer.clearSelection();
        
        //set listener to items
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String[] arrOfStr = newValue.split(" ");
                System.out.println("split0: " + arrOfStr[0] + " split1: " + arrOfStr[1]);
                int index = Integer.parseInt(arrOfStr[1]);
                System.out.println("Selected item: " + index);
                int i = 1;

                for (ArrayList<Integer> frag : fragment_list) {
                    if (i == index) {
                        for (int piece : frag) {
                            //highlight atom
                            jmolMainPanel.viewer.runScript("select atomno=" + (piece + 1) + "; halos on; color halos gold;");

                        }
                    } else {
                        for (int piece : frag) {
                            //un-highlight atom
                            jmolMainPanel.viewer.runScript("select atomno=" + (piece + 1) + "; halos off;");

                        }
                    }
                    i++;
                }   
                jmolMainPanel.repaint();
                
            }
        });
    }
    
}
