package org.vmol.app.database;

import java.awt.Checkbox;
import java.awt.Container;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.jmol.viewer.Viewer;
import org.vmol.app.Main;
import org.vmol.app.MainViewController;
import org.vmol.app.database.DatabaseController.JmolPanel;
import org.vmol.app.qchem.QChemInputController;
import org.vmol.app.util.Atom;
import org.vmol.app.util.PDBParser;
import org.vmol.app.visualizer.JmolVisualizer;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class DatabaseController2 {
	
	private Viewer jmolViewer;
	private Viewer auxiliaryJmolViewer;
	@SuppressWarnings("rawtypes")
    private TableView auxiliary_list;
	private List<ArrayList<Integer>> fragment_list;
	
	private int prev_selection_index = 0;
    private List<ObservableList<DatabaseRecord>> userData;
    private ArrayList<String> final_selections;
	
	public DatabaseController2(Viewer jmolViewer, Viewer auxiliaryJmolViewer, TableView auxiliary_list, List<ArrayList<Integer>> fragment_list) {
		this.jmolViewer = jmolViewer;
		this.auxiliaryJmolViewer = auxiliaryJmolViewer;
		this.auxiliary_list = auxiliary_list;
		this.fragment_list = fragment_list;
	}
	
	public void run() throws IOException {
	    @SuppressWarnings("rawtypes")
        ArrayList<ArrayList> groups = getGroups(this.fragment_list);
	    
		//query database
		ArrayList<String> dbResponse = queryDatabase(groups);
	
		String workingDirectory = System.getProperty("user.dir");
		DatabaseFileManager databaseFileManager = new DatabaseFileManager(workingDirectory);
        
		ArrayList<ArrayList<String []>> group_files = databaseFileManager.processDBresponse(dbResponse);
        ArrayList<ArrayList<String []>> group_filenames = databaseFileManager.writeFiles(group_files);
        
        if(group_filenames.size() > 0){
            //read files into array of strings
            ArrayList<String> filenames = new ArrayList<String>();
            int groupNumber = 1;
            
            for (ArrayList<String []> group : group_filenames){ 
                if(group.size() > 0) {
                    for(String [] pair_name : group) {
                        String xyz_filename = pair_name[0];
                        String efp_filename = pair_name[1];
                        
                        filenames.add(xyz_filename);
                    }
                } else {
                    //this particular fragment did not have any matches from the database
                    sendGamessForm("There are 0 matches for fragment:"+Integer.toString(groupNumber)+" in the Database, do you want to calculate them by Gamess?");
                }
                groupNumber++;
            }
            runAuxiliaryList(group_filenames);
          
            Button button_libefp = getLibefpSubmitButton();
            button_libefp.setOnAction(new EventHandler <ActionEvent>()
            {
                public void handle(ActionEvent event)
                {
                    //handle action
                    sendQChemForm(); //send and arm qchem input form

                }
            });            
        } else {
            //There is zero matched fragments
            //refer to gamess
            sendGamessForm("There are 0 matches for any of the fragments in the Database, do you want to calculate them by Gamess?");
        }
	}
	
	private Button getLibefpSubmitButton() {
        ObservableList<Node> buttonList = new JmolVisualizer().getButtonList();
	    Button button_libefp = (Button) buttonList.get(6);
        button_libefp.setDisable(false);
	    return button_libefp;
	}
	
	//query remote database from AWS server, and return response
    @SuppressWarnings("unchecked")
    private ArrayList<String> queryDatabase(ArrayList<ArrayList> groups) throws IOException {
        ArrayList<String> response = new ArrayList<String>();
	    ArrayList<Atom> pdb;
		pdb = PDBParser.get_atoms(new File(MainViewController.getLastOpenedFile()));
		
		String serverName = "ec2-3-16-11-177.us-east-2.compute.amazonaws.com";
		int port = 8080;
	
		System.out.println("Atoms count: " + groups);
		
		for (int x = 0; x < groups.size(); x ++) {
            try {
                String query = "Query";
                for (int j = 0; j < groups.get(x).size(); j ++) {
                    Atom current_atom = (Atom) pdb.get((Integer) groups.get(x).get(j));
                    if (current_atom.type.matches(".*\\d+.*")) { // atom symbol has digits, treat as charged atom
                        String symbol = current_atom.type;
                        String sign = symbol.substring(symbol.length() - 1);
                        String digits = symbol.replaceAll("\\D+", "");
                        String real_symbol = symbol.substring(0, symbol.length() - 2 - digits.length());
                        query += "$END$" + real_symbol + "  " + current_atom.x + "  " + current_atom.y + "  " + current_atom.z;
                        
                    } else {
                        query += "$END$" + current_atom.type + "  " + current_atom.x + "  " + current_atom.y + "  " + current_atom.z;
                    }
                }
                query+="$ENDALL$";
    				
    	        Socket client = new Socket(serverName, port);
    	        OutputStream outToServer = client.getOutputStream();
    	        //DataOutputStream out = new DataOutputStream(outToServer);
    	        
    	        System.out.println(query);
    	        outToServer.write(query.getBytes("UTF-8"));
    	       
    	        InputStream inFromServer = client.getInputStream();
    	        DataInputStream in = new DataInputStream(inFromServer);
    	        StringBuilder sb = new StringBuilder();
    	        int i;
    	        char c;
    	        boolean start = false;
    	        while (( i = in.read())!= -1) {
    	        	c = (char)i;
    	        	if (c == '#')
    	        		start = true;
    	        	if (start == true)
    	        		sb.append(c);
    	        }
    	        
    	        String reply = sb.toString();
    	        System.out.println("Database Response:" + reply);
    	        response.add(reply);
    	        
    	        client.close();
    	    } catch (IOException e) {
    	        e.printStackTrace();
    	    } 
		}
		return response;
	}
	
	//converts Addison's frag list to Hanjings Groups
	@SuppressWarnings({ "rawtypes", "unchecked" })
    private ArrayList<ArrayList> getGroups(List<ArrayList<Integer>> fragment_list) {
	    ArrayList<ArrayList> groups = new ArrayList<ArrayList>();
        for (ArrayList<Integer> frag : fragment_list) {
            if(frag.size() > 0){
                ArrayList curr_group = new ArrayList();
                for(int piece : frag){
                    curr_group.add(piece);
                }
                Collections.sort(curr_group);
                groups.add(curr_group);
            }
        }
	    return groups;
	}
	
	private void runAuxiliaryList(ArrayList<ArrayList<String []>> group_filenames) {
	    List<ObservableList<DatabaseRecord>> data = loadAuxListData(group_filenames);
	    this.userData = data;
	    
        ListView<String> listView = getFragmentListButtons();
                
        //set listener to items
        //TRIGGER LIST LOAD WITH FRAGMENT CLICK
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String[] arrOfStr = newValue.split(" "); 
                System.out.println("split0: " + arrOfStr[0] + " split1: " + arrOfStr[1]);
                int index = Integer.parseInt(arrOfStr[1]) - 1;
                System.out.println("Selected itemITEM IN DB CONTROLLER: " + index);
                
                runTable(data, index);
            }
        });
	}
	
	@SuppressWarnings("unchecked")
    private void runTable(List<ObservableList<DatabaseRecord>> data, int index) {
	    //LOAD AUXILIARY LIST
        @SuppressWarnings("rawtypes")
        TableView table = this.auxiliary_list;
	    table.setItems(data.get(index));
	    allowOnlyOneCheck(data, index);
	    
	    //LOAD AUX JMOL VIEWER ON CLICK
        table.setRowFactory(tv -> {
            TableRow<DatabaseRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (!row.isEmpty())) {
                    // SubmissionRecord rowData = row.getItem();
                    try {
                        //System.out.println("clicking:" + row.getItem().getRmsd() + row.getItem().getChoice() + row.getItem().getCheck());
                        loadAuxJmolViewer(row.getItem().getChoice().toString());
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
            return row;
        });
	}
	
	private void loadAuxJmolViewer(String filename) {
	    if(!filename.equalsIgnoreCase("NOT FOUND")){
	        String path = System.getProperty("user.dir") + "\\dbController\\xyz_files\\";
            auxiliaryJmolViewer.openFile("file:"+path+filename);
	    }
	}
	
	private void allowOnlyOneCheck(List<ObservableList<DatabaseRecord>> data, int index) {
	    this.prev_selection_index = 0;
	    ObservableList<DatabaseRecord> data_subset = data.get(index);
        for (int i = 0 ; i < data_subset.size();i++) {
            DatabaseRecord d = data_subset.get(i);
            d.checkProperty().addListener( (o, oldV, newV) -> {
                if(newV) {
                    int curr = d.getIndex();
                    if (prev_selection_index != curr) {
                        data_subset.get(prev_selection_index).checkProperty().set(false);
                    }
                    this.prev_selection_index = curr;
                }
           });
       }
	}
	
	//returns button list on main page with fragment lists
	@SuppressWarnings("unchecked")
    private ListView<String> getFragmentListButtons() {
	    SplitPane splitpane = (SplitPane) Main.getMainLayout().getChildren().get(2);
        ObservableList<Node> list = splitpane.getItems();
        @SuppressWarnings("rawtypes")
        ListView<String> listView = (ListView) list.get(0);
        return listView;
	}
	
	private List<ObservableList<DatabaseRecord>> loadAuxListData(ArrayList<ArrayList<String []>> group_filenames) {
        ArrayList<List<DatabaseRecord>> items = new ArrayList<List<DatabaseRecord>>();
        for (ArrayList<String []> group : group_filenames){ 
            ArrayList<DatabaseRecord> drs = new ArrayList<DatabaseRecord>();
            if(group.size() > 0) {
                int index = 0;
                for(String [] pair_name : group) {
                    String xyz_filename = pair_name[0];
                    if(index == 0) {
                        drs.add(new DatabaseRecord(xyz_filename,  "6.022 E23", true , index++));
                    } else {
                        drs.add(new DatabaseRecord(xyz_filename,  "6.022 E23", false , index++));
                    }
                }
            } else {
                //this particular fragment did not have any matches from the database
                drs.add(new DatabaseRecord("Not found",  "0", true , 0));
            }
            items.add(drs);
            
        }
        //LOAD AUXILIARY LIST STRUCTURES
        List<ObservableList<DatabaseRecord>> data = new ArrayList<ObservableList<DatabaseRecord>>();
        for (int i = 0; i < items.size(); i ++) {
            data.add(FXCollections.observableArrayList(new Callback<DatabaseRecord, Observable[]>() {
                @Override
                public Observable[] call(DatabaseRecord param) {
                    return new Observable[] {
                            param.checkProperty()};
                    
                }
            }));
            data.get(i).addAll(items.get(i));
        }
        return data;
	}
	
	private void sendQChemForm() {
	    List<ObservableList<DatabaseRecord>> data = this.userData;
	    //List<ArrayList<Integer>> groups = this.fragment_list;
	    @SuppressWarnings("rawtypes")
        ArrayList<ArrayList> groups = getGroups(this.fragment_list);
        
	    String coords = generateQchemInput(data, groups);
	    
	    final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/org/vmol/app/qchem/QChemInput.fxml"));
        //String coords = "fragment frag_a\n16.380  20.017  16.822\n15.898  20.749  17.636\n16.748  18.743  17.075\n\nfragment frag_b\n15.252  17.863  18.838\n14.642  18.742  18.674\n14.861  17.071  18.204\n\nfragment frag_c\n13.634  16.902  22.237\n14.110  15.961  22.470\n14.051  17.676  22.864\n";
        QChemInputController controller;
        controller = new QChemInputController(coords,null,this.final_selections);
        loader.setController(controller);
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                TabPane bp;
                try {
                    
                    bp = loader.load();
                    Scene scene = new Scene(bp,600.0,480.0);
                    Stage stage = new Stage();
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.setTitle("Libefp Input");
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
        });
	}
	
	@SuppressWarnings("unchecked")
    private String generateQchemInput(List<ObservableList<DatabaseRecord>> data,   @SuppressWarnings("rawtypes") ArrayList<ArrayList> groups) {
	    StringBuilder sb = new StringBuilder();
	    this.final_selections = new ArrayList<String>();
	    ArrayList<Atom> pdb = null;
        try {
            pdb = PDBParser.get_atoms(new File(MainViewController.getLastOpenedFile()));
            
            int group_number = 0;
            for(ObservableList<DatabaseRecord> list : data) {
                for(DatabaseRecord record : list) {
                    if(record.getCheck() == true) {
                        if(!record.getChoice().equalsIgnoreCase("NOT FOUND")) {
                            //parse filename
                            String file_name = record.getChoice().toString();
                            final_selections.add(file_name);
                            String [] filename = file_name.split("\\.");
                            if(group_number == 0) {
                                sb.append("fragment " + filename[0] + "\n");
                            } else {
                                sb.append("\nfragment " + filename[0] + "\n");
                            }
                            //apend equivalent group coordinates
                            ArrayList<Integer> fragment = groups.get(group_number);
                            for(int atom_num : fragment) {
                                Atom current_atom = (Atom) pdb.get(atom_num);
                                sb.append(current_atom.x + "  " + current_atom.y + "  " + current_atom.z+"\n");
                            }
                            
                        }
                        break;
                    }
                }
                group_number++;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    return sb.toString();
	}
	
	private void sendGamessForm(String msg) {
	    Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Gamess");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        Optional<ButtonType> result = alert.showAndWait();
	}
}
