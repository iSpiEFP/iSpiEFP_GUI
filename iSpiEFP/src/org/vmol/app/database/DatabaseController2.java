package org.vmol.app.database;

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

import org.jmol.viewer.Viewer;
import org.vmol.app.Main;
import org.vmol.app.MainViewController;
import org.vmol.app.qchem.QChemInputController;
import org.vmol.app.util.Atom;
import org.vmol.app.util.PDBParser;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
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
	//private ListView<String> auxiliary_list;
	private TableView auxiliary_list;
	private List<ArrayList<Integer>> fragment_list;

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
            loadAuxiliaryList(filenames); //load files from DB into viewer list
            sendQChemForm(); //send and arm qchem input form
            
        } else {
            //There is zero matched fragments
            //refer to gamess
            sendGamessForm("There are 0 matches for any of the fragments in the Database, do you want to calculate them by Gamess?");
        }
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
	
	private void loadAuxiliaryList(ArrayList<String> filenames) {
	    ObservableList<String> data = FXCollections.observableArrayList();
        //ListView<String> listView = this.auxiliary_list;
        TableView table = this.auxiliary_list;
        TableColumn column1 = (TableColumn) table.getColumns().get(0);
        column1.setText("Choice");
        TableColumn column2 = (TableColumn) table.getColumns().get(1);
        column2.setText("RMSD");
        TableColumn column3 = (TableColumn) table.getColumns().get(2);
        column3.setText("Select");
        
        TableColumn<DatabaseRecord,String> index = column1;
        index.setCellValueFactory(new PropertyValueFactory<DatabaseRecord, String>("choice"));
        //table.getColumns().add(index);
        index.setPrefWidth(100.0);
        
        TableColumn<DatabaseRecord,String> rmsd = column2;
        rmsd.setCellValueFactory(new PropertyValueFactory<DatabaseRecord, String>("rmsd"));
        //choices.getColumns().add(rmsd);
        rmsd.setPrefWidth(100.0);
        
        TableColumn<DatabaseRecord,Boolean> check = column3;
        check.setCellValueFactory(new PropertyValueFactory<DatabaseRecord, Boolean>("check"));
        check.setCellFactory(column -> new CheckBoxTableCell());
        check.setEditable(true);
        check.setPrefWidth(100);
        
        ArrayList<DatabaseRecord> drs = new ArrayList<DatabaseRecord>();
        drs.add(new DatabaseRecord("Not found",  "0", false , -1));
        drs.add(new DatabaseRecord("Nooaincoian",  "0", false , -1));
        drs.add(new DatabaseRecord("Noapm cncd",  "0", false , -1));
        drs.add(new DatabaseRecord("Not focce",  "69", false , -1));
        drs.add(new DatabaseRecord("Not fouc",  "0", false , -1));

        ArrayList<List<DatabaseRecord>> items = new ArrayList<List<DatabaseRecord>>();
        items.add(drs);

        List<ObservableList<DatabaseRecord>> data2 = new ArrayList<ObservableList<DatabaseRecord>>();
        System.out.println("items has " + items.size());
        for (int i = 0; i < items.size(); i ++) {
            data2.add(FXCollections.observableArrayList(new Callback<DatabaseRecord, Observable[]>() {
                @Override
                public Observable[] call(DatabaseRecord param) {
                    return new Observable[] {
                            param.checkProperty()};
                    
                }
            }));
            data2.get(i).addAll(items.get(i));
            
        }
        table.setItems(data2.get(0));
        //choices.getColumns().add(check);
        //choices.setItems(data.get(0));
        
        //column3.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().getIsDefault()));
        //column3.setCellFactory(tc -> new CheckBoxTableCell<>());
        
        
	    String[] names = new String[filenames.size()];
        names = filenames.toArray(names);
        data.addAll(names);
        
        /*listView.setItems(data);
        
        //set listener to items
        auxiliaryJmolViewer.runScript("set autobond on");
        
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // Your action here
                System.out.println("Selected item: " + newValue);
                String path = System.getProperty("user.dir") + "\\dbController\\xyz_files\\";

                auxiliaryJmolViewer.openFile("file:"+path+newValue);
            }
        });*/
	}
	
	private void sendQChemForm() {
	    final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/org/vmol/app/qchem/QChemInput.fxml"));
        String coords = "fragment frag_a\n16.380  20.017  16.822\n15.898  20.749  17.636\n16.748  18.743  17.075\n\nfragment frag_b\n15.252  17.863  18.838\n14.642  18.742  18.674\n14.861  17.071  18.204\n\nfragment frag_c\n13.634  16.902  22.237\n14.110  15.961  22.470\n14.051  17.676  22.864\n";
        QChemInputController controller;
        controller = new QChemInputController(coords,null);
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
	
	private void sendGamessForm(String msg) {
	    Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Gamess");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        Optional<ButtonType> result = alert.showAndWait();
	}
}
