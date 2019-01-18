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
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DatabaseController2 {
	
	private Viewer jmolViewer;
	private Viewer auxiliaryJmolViewer;
	private ListView<String> auxiliary_list;
	private List<ArrayList<Integer>> fragment_list;
	private ArrayList<String> fileHistory;

	public DatabaseController2(Viewer jmolViewer, Viewer auxiliaryJmolViewer, ListView<String> auxiliary_list, List<ArrayList<Integer>> fragment_list) {
		this.jmolViewer = jmolViewer;
		this.auxiliaryJmolViewer = auxiliaryJmolViewer;
		this.auxiliary_list = auxiliary_list;
		this.fragment_list = fragment_list;
		fileHistory = new ArrayList<String>();
		fileHistory.clear();
		//int atom_num = jmolViewer.ms.at.length;
	}
	
	public void run() throws IOException {
		System.out.println("Initializing DB controller!!!!");
		ArrayList<List<DatabaseRecord>> items = new ArrayList<List<DatabaseRecord>>();
		int groups = jmolViewer.ms.at.length;
		//query database
		ArrayList<String> dbResponse = queryDatabase(groups);
		
		//process response
		//if there is no response return a sad face :( (need gamess then)
		//if there is response, package the response, put it in directory, and load it up in the auxiliary panel
		ArrayList<ArrayList<String>> response = processDBresponse(dbResponse);
		ArrayList<String> filenames = new ArrayList<String>();
		if(response.size() > 0){
		    int count = 0;
	        String path = System.getProperty("user.dir") + "\\dbController";

		    for (ArrayList<String> fileContent : response){ 
		        //write file to tmp dir
		        createTempXYZFile(fileContent, fileHistory.size()+1);
		        //String filename = path + "\\"+ Integer.toString(count) +".xyz";
	            String filename = Integer.toString(fileHistory.size()) +".xyz";

		        filenames.add(filename);
		    }
		    createDir();
		    loadAuxiliaryList(filenames);
		    
		    //load qchem input window
		    //jmolViewer.runScript("selectionHalos off");
            //jmolWindow.repaint();
            //((Stage)choices.getScene().getWindow()).close();
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
		} else {
		    Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Gamess");
            alert.setHeaderText(null);
            alert.setContentText("There are groups you have not picked parameters for, do you want to calculate them by Gamess?");
            Optional<ButtonType> result = alert.showAndWait();
		}
	}
	
	//query remote database from AWS server, and return response
	@SuppressWarnings("unchecked")
    private ArrayList<String> queryDatabase(int groups21) throws IOException {
        ArrayList<String> response = new ArrayList<String>();
	    
	    ArrayList<Atom> pdb;
		
		pdb = PDBParser.get_atoms(new File(MainViewController.getLastOpenedFile()));
		
		//legacy server name
		//String serverName = "ec2-18-219-71-66.us-east-2.compute.amazonaws.com";
		//new server name
		String serverName = "ec2-3-16-11-177.us-east-2.compute.amazonaws.com";
		int port = 8080;
	
		//ArrayList<ArrayList> groups = this.fragment_list;
		ArrayList<ArrayList> groups = new ArrayList<ArrayList>();
		
		for (ArrayList<Integer> frag : this.fragment_list) {
            if(frag.size() > 0){
                ArrayList curr_group = new ArrayList();
                //System.out.println("Dumping frag contents");
                for(int piece : frag){
                    //System.out.println(piece);
                    curr_group.add(piece);
                }
                Collections.sort(curr_group);
                groups.add(curr_group);
                //data.add("Fragment " + fragmentCounter++);
            }
        }
		
		System.out.println("Atoms count: " + groups);
		
		for (int x = 0; x < groups.size(); x ++) {
            ArrayList<DatabaseRecord> drs = new ArrayList<DatabaseRecord>();
            ArrayList<ArrayList> molecules = new ArrayList<ArrayList>();
            int index = 1;
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
    	        //System.out.println(reply);
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
	
	//INPUT: Raw response from Database
	private ArrayList<ArrayList<String>> processDBresponse(ArrayList<String> response) {
        ArrayList<ArrayList<String>> files = new ArrayList<ArrayList<String>>();

        for(String res : response){
    	    String reply = res;
    	    reply = reply.substring(1);
            
            String[] current_xyzs = reply.split("\\$NEXT\\$");
            System.out.println("Current Files:" + current_xyzs.length + current_xyzs[0]);        
    
            if(current_xyzs.length <= 1){
                //no response
            } else {
                //parse response and dump in folders for each file line
                for(int i = 1; i < current_xyzs.length; i++) {
                    ArrayList<String> file = parseDBResponse(current_xyzs[i]);
                    files.add(file);
                }
            }
        }
        return files;
	}
	
	private ArrayList<String> parseDBResponse(String rawFile) {
        ArrayList<String> result = new ArrayList<String>();
        String[] lines = rawFile.split("n', ");
        System.out.println(lines.length);
        for(String line : lines){
            System.out.println(line);
            String[] pieces = line.split("\\s+");
            String name = (pieces[0]);
            String x_coord = (pieces[1]);
            String y_coord = (pieces[2]);
            String z_coord = (pieces[3]);
            
            //fix name; dirty current fix
            //char atom_name = name.charAt(3);
            //name = Character.toString(atom_name);
            
            name = name.substring(1);
            System.out.println("name:"+name);
            if(name.charAt(0) == 'B'){
                System.out.println("bond encountered");
            } else {
                //parse name
                name = name.substring(1);
                for(int u = 0; u < name.length(); u++){
                    char ch = name.charAt(u);
                    if(ch >= 'A' && ch <= 'Z'){
                        name = Character.toString(ch);
                        break;
                    }
                }
                line = name + "      " + x_coord + "   " + y_coord + "   " + z_coord + "\n";
                result.add(line);
            }
        }
        System.out.println("Result String:" + result);
        return result;
    }
	
	private void createDir() {
	    String workingPath = System.getProperty("user.dir");
	    new File(workingPath + "/dbController").mkdirs();
	}
	
	private void removeDir() {
	    //TODO
	}
	
	private void createTempXYZFile(ArrayList<String> fileContent, int file_number) {
	    String path = System.getProperty("user.dir") + "\\dbController";
	    //unroll content
	    int line_count = fileContent.size();
	    String content = Integer.toString(line_count) + "\n\n";
	    for(String line: fileContent) {
	        content += line;
	    }
	    
	    BufferedWriter bufferedWriter = null;
        try {
            //file_number = fileHistory.size() + 1;
            String filename = path + "\\" + Integer.toString(file_number) + ".xyz";
            fileHistory.add(filename);
            File myFile = new File(filename);
        
            // check if file exist, otherwise create the file before writing
            if (!myFile.exists()) {
                myFile.createNewFile();
            }
            Writer writer = new FileWriter(myFile);
            bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try{
                if(bufferedWriter != null) bufferedWriter.close();
            } catch(Exception ex){
                 
            }
        }
	}
	
	private void loadAuxiliaryList(ArrayList<String> filenames) {
	    ObservableList<String> data = FXCollections.observableArrayList();

        ListView<String> listView = this.auxiliary_list;

        String[] names = new String[filenames.size()];
        names = filenames.toArray(names);
        //data.addAll("lysine_0.xyz","lysine_1.xyz","lysine_2.xyz");
        data.addAll(names);
        
        listView.setItems(data);
        
        //set listener to items
        auxiliaryJmolViewer.runScript("set autobond on");
        
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // Your action here
                System.out.println("Selected item: " + newValue);
                String path = System.getProperty("user.dir") + "\\dbController";
                path = path + "\\";

                auxiliaryJmolViewer.openFile("file:"+path+newValue);
            }
        });
	}
    	
}
