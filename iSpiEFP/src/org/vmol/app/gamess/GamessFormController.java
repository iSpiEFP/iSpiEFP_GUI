package org.vmol.app.gamess;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.vmol.app.Main;
import org.vmol.app.MainViewController;
import org.vmol.app.database.DatabaseRecord;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class GamessFormController {
    private ArrayList<Integer> to_be_submitted;
    private ArrayList<Integer> unknownGroups;
    private ArrayList<ArrayList> groups;
    private Scene scene;
    private Stage newStage;
    private TableView table;
    
    public GamessFormController(ArrayList<ArrayList> groups, ArrayList<Integer> unknowngroups) {
        this.to_be_submitted = new ArrayList<Integer>();
        this.unknownGroups = unknowngroups;
        this.groups = groups;
        
    }
    
    public void run() {
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(5);
        
        Label label = new Label("The following fragments either were not found, or returned an RMSD\n value above 0.5. Please select any fragments you would like\n to have Gamess calculate.");
        
        //Add table to pane
        this.table = new TableView();
        TableColumn column1 = new TableColumn("Choice");
        TableColumn column2 = new TableColumn("Select");

        TableColumn<GamessRecord,String> index = column1;
        index.setCellValueFactory(new PropertyValueFactory<GamessRecord, String>("choice"));
        index.setPrefWidth(400.0);
        
        TableColumn<GamessRecord,Boolean> check = column2;
        check.setCellValueFactory(new PropertyValueFactory<GamessRecord, Boolean>("check"));
        check.setCellFactory(column -> new CheckBoxTableCell());
        check.setEditable(true);
        check.setPrefWidth(100);

        table.getColumns().addAll(column1,column2);
        table.setEditable(true);
        
        ObservableList<GamessRecord> data = loadData(unknownGroups).get(0);
        runTable(data);
        
        // To contain the buttons
        HBox buttonBar = new HBox();
        
        Button buttonContinue = new Button("Continue");
        buttonContinue.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("gamess");
                try {
                    newStage.close();
                    
                    for(GamessRecord record : data) {
                        if(record.getCheck()) {
                            to_be_submitted.add(record.getIndex());
                        }
                    }
                    sendRealGamessForm(groups, to_be_submitted);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        
        Button buttonSelectAll = new Button("Select All");
        buttonSelectAll.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("analyze");
                //visualize(output);
                for(GamessRecord record : data) {
                    record.setCheck(true);
                    //to_be_submitted.add(record.getIndex());
                }
            }
        });
 
        buttonBar.getChildren().addAll(buttonSelectAll, buttonContinue);
        root.getChildren().addAll(label, table, buttonBar);
        
        this.scene = new Scene(root, 520, 520);
        this.newStage = new Stage();
        
        newStage.setTitle("Fragments not found in Database");
        newStage.setScene(scene);
        newStage.showAndWait();
    }
    
    private List<ObservableList<GamessRecord>> loadData(ArrayList<Integer> groups) {
        ArrayList<GamessRecord> items = new ArrayList<GamessRecord>();
        for (Integer group : groups){ 
            items.add(new GamessRecord("Fragment: " + (group+1), group, false));
        }
        
        //LOAD LIST STRUCTURES
        List<ObservableList<GamessRecord>> data = new ArrayList<ObservableList<GamessRecord>>();
        for (int i = 0; i < items.size(); i ++) {
            data.add(FXCollections.observableArrayList(new Callback<GamessRecord, Observable[]>() {
                @Override
                public Observable[] call(GamessRecord param) {
                    return new Observable[] {
                            param.checkProperty()};
                    
                }
            }));
            data.get(0).add(items.get(i));
        }
        return data;
    }
    
    
    @SuppressWarnings("unchecked")
    private void runTable(ObservableList<GamessRecord> data) {
        //LOAD  LIST
        @SuppressWarnings("rawtypes")
        TableView table = this.table;
        
        table.setItems(data);
        
        //ADD SELECTION TO BE SUBMITTED LIST
        table.setRowFactory(tv -> {
            TableRow<GamessRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
 
                if (event.getClickCount() == 1 && (!row.isEmpty())) {
                    // SubmissionRecord rowData = row.getItem();
                    System.out.println("hit a row");
                    

                    
                }
            });
            return row;
        });
    }
    
    private void sendRealGamessForm(ArrayList<ArrayList> groups, ArrayList to_be_submitted) throws IOException {
        
        if (true) {
            //ArrayList<Atom> atom_list = PDBParser.get_atoms(new File(MainViewController.getLastOpenedFile()));
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            Date date = new Date();
      
            
            
            //String coords = "fragment frag_a\n16.380  20.017  16.822\n15.898  20.749  17.636\n16.748  18.743  17.075\n\nfragment frag_b\n15.252  17.863  18.838\n14.642  18.742  18.674\n14.861  17.071  18.204\n\nfragment frag_c\n13.634  16.902  22.237\n14.110  15.961  22.470\n14.051  17.676  22.864\n";
            StringBuilder sb = new StringBuilder();
            
            
            final FXMLLoader gamess_loader = new FXMLLoader(this.getClass().getResource("/org/vmol/app/gamess/gamessInput.fxml"));
            gamessInputController gamess_controller;
            gamess_controller = new gamessInputController(new File(MainViewController.getLastOpenedFile()), groups, to_be_submitted);
            gamess_loader.setController(gamess_controller);
            ArrayList<ArrayList> fragments = gamess_controller.get_fragments_with_h();
            
            HashMap<String, Integer> protons = new HashMap<>();
            protons.put("H", 1);
            protons.put("C", 6);
            protons.put("N", 7);
            protons.put("O", 8);
            protons.put("S", 16);
            protons.put("CL", 17);
            protons.put("H000", 1);
    
            System.out.println("hit the stage");
            
            Platform.runLater(new Runnable(){
                @Override
                public void run() {
                    BorderPane bp;
                    try {
                        bp = gamess_loader.load();
                        Scene scene = new Scene(bp,659.0,500.0);
                        Stage stage = new Stage();
                        stage.initModality(Modality.WINDOW_MODAL);
                        stage.setTitle("Gamess Input");
                        stage.setScene(scene);
                        stage.show();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                }
                });
            
        
        }
    }
}

