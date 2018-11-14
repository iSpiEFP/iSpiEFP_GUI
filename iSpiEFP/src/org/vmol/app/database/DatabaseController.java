package org.vmol.app.database;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.apache.commons.io.IOUtils;
import org.controlsfx.control.action.Action;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolSelectionListener;
import org.jmol.java.BS;
import org.jmol.viewer.Viewer;
import org.vmol.app.MainViewController;
import org.vmol.app.database.DatabaseRecord;
import org.vmol.app.gamess.gamessInputController;
import org.vmol.app.gamessSubmission.gamessSubmissionHistoryController;
import org.vmol.app.qchem.QChemInputController;
import org.vmol.app.submission.SubmissionHistoryController;
import org.vmol.app.util.Atom;
import org.vmol.app.util.PDBParser;
import org.vmol.app.visualization.JmolVisualization;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPOutputStream;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class DatabaseController {
	@FXML
	private Parent root;
	
	@FXML
	private ComboBox<String> group_selector;
	
	@FXML
	private TableView<DatabaseRecord> choices;
	
	private int prev_selection_index = 0;
	
	List<ObservableList<DatabaseRecord>> data;
	
	private ArrayList<ArrayList> groups;
	
	private Viewer jmolViewer;
	
	private JFrame jmolWindow;
	
	private String previous_selected_group = "";
	
	private int curr_group_index = 0;
	
	private Viewer left_viewer;
	private Viewer right_viewer;
	
	private ArrayList<ArrayList> final_selections;

	private static Preferences userPrefs = Preferences.userNodeForPackage(gamessSubmissionHistoryController.class);
	
	private boolean firstStart = true;
	
	private ArrayList<ArrayList<ArrayList>> xyzs;
	
	Map<String, Double> charges;
	
	public DatabaseController(ArrayList<ArrayList> groups, Viewer jmolViewer, JFrame jmolWindow) {
		this.groups = groups;
		this.jmolViewer = jmolViewer;
		this.jmolWindow = jmolWindow;
	}
	
	@FXML  
	public void initialize() throws IOException {
		charges = new HashMap<String,Double>();
    	charges.put("H", 1.0);
    	charges.put("H000", 1.0);
    	charges.put("C", 6.0);
    	charges.put("N", 7.0);
    	charges.put("O", 8.0);
    	charges.put("S", 16.0);
    	xyzs = new ArrayList<ArrayList<ArrayList>>();
		final_selections = new ArrayList<ArrayList>();
		for (int i = 0; i < groups.size(); i ++) {
			final_selections.add(new ArrayList());
			final_selections.get(i).add(0);
		}
	
		
		jmolViewer.runScript("select clear");
        //jmolViewer.clearSelection();
        jmolViewer.runScript("set pickingstyle SELECT DRAG");
        jmolViewer.runScript("set picking atom");
        jmolViewer.runScript("selectionHalos on");
        String selecting = "select ({";
		for (int i = 0; i < groups.get(0).size(); i++) {
			selecting += groups.get(0).get(i) + " ";
		}
		selecting += "})";
		jmolViewer.runScript(selecting);
		jmolWindow.repaint();
		
		JFrame comparison = new JFrame("Fragmentation comparasion");
		comparison.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Jmol window closing");
                jmolWindow.setVisible(false);
                
            
            }
        });
		comparison.setSize(1200, 600);
        
		if (firstStart == true) {
			firstStart = false;
			choices.setEditable(true);
			choices.setRowFactory(tv -> {
				TableRow<DatabaseRecord> row = new TableRow<>();
				row.setOnMouseClicked(event -> {
					if (event.getClickCount() == 2 && (!row.isEmpty())) {
						// SubmissionRecord rowData = row.getItem();
						
						try {
							
				            Container contentPane = comparison.getContentPane();
				            

				            // main panel -- Jmol panel on top
				            
				            
				            JmolPanel jmolPanel = new JmolPanel();
				            
				            String homeDir = System.getProperty("user.home");
				            
				            
				            
				            JmolPanel jmolPanel_right = new JmolPanel();
				            
				            JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jmolPanel, jmolPanel_right);
				            panel.setResizeWeight(0.5);
				            contentPane.add(panel);
//							if (previous_selected_group.equals("")) {
//								left_viewer = jmolPanel.viewer;
//								right_viewer = jmolPanel_right.viewer;
//								
//								
//					            BufferedWriter writer = new BufferedWriter(new FileWriter(homeDir+"/left.xyz"));
//					            ArrayList<Atom> pdb = PDBParser.get_atoms(new File(MainViewController.getLastOpenedFile()));
//					            writer.write(groups.get(0).size() + "\n");
//					            writer.write(" \n");
//					            for (int i = 0; i < groups.get(0).size(); i++) {
//					            	Atom current_atom = (Atom) pdb.get((Integer) groups.get(0).get(i));
//					            	writer.write(current_atom.type + "   " + current_atom.x + "   " + current_atom.y + "   " + current_atom.z + "\n");
//					            }
//					            writer.close();
//					            
//					            
//								left_viewer.openFile(homeDir+"/left.xyz");
//								
//					           
//					           
//					            int index = Integer.parseInt(row.getItem().getChoice()) - 1;
//					            writer = new BufferedWriter(new FileWriter(homeDir+"/right.xyz"));
//					            writer.write(xyzs.get(0).get(index).size() + "\n");
//					            writer.write(" \n");
//					            
//					            for (int i = 0; i < xyzs.get(0).get(index).size(); i ++) {
//					            	Atom current_atom = (Atom) xyzs.get(0).get(index).get(i);
//					            	writer.write(current_atom.type + "   " + current_atom.x + "   " + current_atom.y + "   " + current_atom.z + "\n");
//					            }
//					            writer.close();
//					            
//					            right_viewer.openFile(homeDir+"/right.xyz");
//					            previous_selected_group = group_selector.getValue();
//					            
//					            comparison.setVisible(true);
//					            comparison.repaint();
//							} else   {
								
				            	left_viewer = jmolPanel.viewer;
								right_viewer = jmolPanel_right.viewer;
					            BufferedWriter writer = new BufferedWriter(new FileWriter(homeDir+"/left.xyz"));
					            ArrayList<Atom> pdb = PDBParser.get_atoms(new File(MainViewController.getLastOpenedFile()));
					            writer.write(groups.get(Integer.parseInt(group_selector.getValue()) -1 ).size() + "\n");
					            
					            writer.write(" \n");
					            for (int i = 0; i < groups.get(Integer.parseInt(group_selector.getValue()) -1 ).size(); i++) {
					            	Atom current_atom = (Atom) pdb.get((Integer) groups.get(Integer.parseInt(group_selector.getValue()) -1 ).get(i));
					            	writer.write(current_atom.type + "   " + current_atom.x + "   " + current_atom.y + "   " + current_atom.z + "\n");
					            }
					            writer.close();
					            
					            
					            int index = Integer.parseInt(row.getItem().getChoice()) - 1;
					            writer = new BufferedWriter(new FileWriter(homeDir+"/right.xyz"));
					            writer.write(xyzs.get(Integer.parseInt(group_selector.getValue()) -1 ).get(index).size() + "\n");
					            writer.write(" \n");
					            
					            for (int i = 0; i < xyzs.get(Integer.parseInt(group_selector.getValue()) -1 ).get(index).size(); i ++) {
					            	Atom current_atom = (Atom) xyzs.get(Integer.parseInt(group_selector.getValue()) -1 ).get(index).get(i);
					            	writer.write(current_atom.type + "   " + current_atom.x + "   " + current_atom.y + "   " + current_atom.z + "\n");
					            }
					            writer.close();
					            
					            
					            
					            
					            left_viewer.openFile(homeDir+"/left.xyz");
					            right_viewer.openFile(homeDir+"/right.xyz");
								comparison.setVisible(true);
								comparison.repaint();
							
							System.out.println("clicking");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				return row;
			});
			
			List<String> no = new ArrayList<String>();
			for (int i = 1 ; i <= groups.size(); i ++) {
				no.add(Integer.toString(i));
			}
			group_selector.setItems(FXCollections.observableArrayList(no));
			group_selector.setValue("1");
			group_selector.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
				int group_index = newValue.charAt(0) - '1';
				String selecting2 = "select ({";
				for (int i = 0; i < groups.get(group_index).size(); i++) {
					selecting2 += groups.get(group_index).get(i) + " ";
				}
				selecting2 += "})";
				jmolViewer.runScript(selecting2);
				jmolWindow.repaint();
			});
			
			ArrayList<List<DatabaseRecord>> items = new ArrayList<List<DatabaseRecord>>();
			
			ArrayList<Atom> pdb;
			
			pdb = PDBParser.get_atoms(new File(MainViewController.getLastOpenedFile()));
			
			String serverName = "ec2-18-219-71-66.us-east-2.compute.amazonaws.com";
			int port = 8080;
			
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
		        if (reply.contains("none")) {
		        	continue;
		        }
		        reply = reply.substring(1);
		        
		        String[] current_xyzs = reply.split("\\$NEXT\\$");
		        for (i = 0; i < current_xyzs.length / 2; i ++) {
		        	String id = current_xyzs[2*i];
		        	String[] lines = current_xyzs[2*i+1].split(",");
		        	ArrayList<Atom> atoms = new ArrayList<Atom>();
		        	for (int j = 0; j < lines.length; j ++) {
		        		if (lines[j].indexOf("A") != -1) {
		        			
		        			String[] tokens = lines[j].trim().split("\\s+");
		        			String symbol = tokens[0].substring(tokens[0].indexOf("A")+3);
		        			Atom current_atom = new Atom(symbol, Integer.parseInt(id), Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]) , Double.parseDouble(tokens[3]));
		        			atoms.add(current_atom);
		        			
		        			//System.out.println(current_atom.type + "  " + current_atom.index + "  " + current_atom.x);
		        		}
		        	}
		        	molecules.add(atoms);
		        	drs.add(new DatabaseRecord(Integer.toString(index), "0" , false , Integer.parseInt(id)));
		        	index++;
		        	
		        }
		        
		        
		        if (drs.size()>=1) {
		        	xyzs.add(molecules);
		        	drs.get(0).setCheck(true);
			        items.add(drs);
		        } else {
		        	drs.add(new DatabaseRecord("Not found",  "0", false , -1));
		        	items.add(drs);
		        	xyzs.add(new ArrayList<>());
		        	final_selections.get(xyzs.size()-1).clear();
		        }
		        
		        
		        
		        //System.out.println("Server says " + in.readUTF());
		        client.close();
		     } catch (IOException e) {
		        e.printStackTrace();
		     }
			
		
		}
			
			
			
//			List<DatabaseRecord> items1 = Arrays.asList(new DatabaseRecord("1","0",true),
//					new DatabaseRecord("2","0",false),
//					new DatabaseRecord("3","0.967048526",false),
//					new DatabaseRecord("4","1.716201617",false));
//			List<DatabaseRecord> items2 = Arrays.asList(new DatabaseRecord("1","0",true),
//					new DatabaseRecord("2","0.676954958",false),
//					new DatabaseRecord("3","1.279396548",false),
//					new DatabaseRecord("4","2.463240909",false));
//			List<DatabaseRecord> items3 = Arrays.asList(new DatabaseRecord("1","0",true),
//					new DatabaseRecord("2","0.995958917",false),
//					new DatabaseRecord("3","2.3000735202074365",false),
//					new DatabaseRecord("4","3.051696274",false));
			
			
			
			data = new ArrayList<ObservableList<DatabaseRecord>>();
			System.out.println("items has " + items.size());
			for (int i = 0; i < items.size(); i ++) {
				this.data.add(FXCollections.observableArrayList(new Callback<DatabaseRecord, Observable[]>() {
					@Override
					public Observable[] call(DatabaseRecord param) {
						return new Observable[] {
								param.checkProperty()};
						
					}
				}));
				this.data.get(i).addAll(items.get(i));
				
			}
			
			ObservableList<DatabaseRecord> data_subset = data.get(0);
			for (int j = 0; j < data_subset.size(); j ++) {
				String item = Integer.toString(j);
				DatabaseRecord d = data_subset.get(j);
				d.checkProperty().addListener((obs, wasCompleted, isNowCompleted) -> {
					//data_subset.get(prev_selection_index).checkProperty().set(false);
					int curr = Integer.parseInt(d.getChoice())-1;
					if (prev_selection_index != curr) {
						data_subset.get(prev_selection_index).checkProperty().set(false);
					}
					prev_selection_index = curr;
					final_selections.get(curr_group_index).clear();
					if (isNowCompleted == true) {
						final_selections.get(curr_group_index).add(curr);
						
					}
					
					System.out.println("group" + curr_group_index + " selecting " + curr + "  " + isNowCompleted);
					//data.get(prev_selection_index).checkProperty().set(true);
				});
			}
			
//			data.addListener(new ListChangeListener <DatabaseRecord>() {
//				@Override
//				public void onChanged(ListChangeListener.Change<? extends DatabaseRecord> c) {
//					while (c.next()) {
//						if (c.wasUpdated() ) {
//							data.get(prev_selection_index).setCheck(false);
//							System.out.println(prev_selection_index);
//							prev_selection_index = c.getFrom();
//							data.get(prev_selection_index).setCheck(true);
//							//System.out.println("Cours "+items.get(c.getFrom()).getChoice()+" changed value to " +items.get(c.getFrom()).getCheck());
//						}
//					}
//				}
//			});
			
			
			
			TableColumn<DatabaseRecord,String> index = new TableColumn<DatabaseRecord,String>("Choice");
			index.setCellValueFactory(new PropertyValueFactory<DatabaseRecord, String>("choice"));
			choices.getColumns().add(index);
			index.setPrefWidth(100.0);
			
			TableColumn<DatabaseRecord,String> rmsd = new TableColumn<DatabaseRecord,String>("RMSD");
			rmsd.setCellValueFactory(new PropertyValueFactory<DatabaseRecord, String>("rmsd"));
			choices.getColumns().add(rmsd);
			rmsd.setPrefWidth(300.0);
			
			TableColumn<DatabaseRecord,Boolean> check = new TableColumn<DatabaseRecord,Boolean>("Check?");
			check.setCellValueFactory(new PropertyValueFactory<DatabaseRecord, Boolean>("check"));
			check.setCellFactory(column -> new CheckBoxTableCell());
			check.setEditable(true);
			check.setPrefWidth(255.0);
			choices.getColumns().add(check);
			choices.setItems(data.get(0));
		}
		
		
	}
	
	@FXML
	protected void switch_group() {
		int index = group_selector.getValue().charAt(0) - '1';
		System.out.println(index);
		curr_group_index = index;
		choices.setItems(data.get(index));
		prev_selection_index = (int) final_selections.get(index).get(0);
		ObservableList<DatabaseRecord> data_subset = data.get(index);
		for (int j = 0; j < data_subset.size(); j ++) {
			String item = Integer.toString(j);
			DatabaseRecord d = data_subset.get(j);
			d.checkProperty().addListener((obs, wasCompleted, isNowCompleted) -> {
				
				int curr = Integer.parseInt(d.getChoice())-1;
				if (prev_selection_index != curr) {
					data_subset.get(prev_selection_index).checkProperty().set(false);
				}
				prev_selection_index = curr;
				final_selections.get(curr_group_index).clear();
				if (isNowCompleted == true) {
					final_selections.get(curr_group_index).add(curr);
				}
				System.out.println("group" + curr_group_index + " selecting " + curr);
				//data.get(prev_selection_index).checkProperty().set(true);
			});
		}
	}
	
	@FXML
	protected void finish() throws IOException {
		for (int i = 0; i < final_selections.size(); i ++) {
			for (int j = 0; j < final_selections.get(i).size(); j++)
			System.out.println(final_selections.get(i).get(j));
		}
		ArrayList to_be_submitted = new ArrayList<>();
		boolean all = true;
		for (int i = 0; i < final_selections.size(); i ++) {
			
			if (final_selections.get(i).size() == 0) {
				to_be_submitted.add(i);
				all = false;
				System.out.println("currently selected record: none");
			} else {
				System.out.println("currently selected record: " + final_selections.get(i).get(0));
			}
			//System.out.println("currently selected record: " + final_selections.get(i).get(0));
			
		}
		
		if (all == false) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Gamess");
			alert.setHeaderText(null);
			alert.setContentText("There are groups you have not picked parameters for, do you want to calculate them by Gamess?");
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK) {
				ArrayList<Atom> atom_list = PDBParser.get_atoms(new File(MainViewController.getLastOpenedFile()));
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
				Date date = new Date();
				
//				for (int j = 0; j < to_be_submitted.size(); j ++) {
//					int num = (int) to_be_submitted.get(j);
//					userPrefs.put(MainViewController.getLastOpenedFileName()+"_"+Character.toString((char) ('A'+ num)) ,date.toString());
//				}
				jmolViewer.runScript("selectionHalos off");
				jmolWindow.repaint();
				((Stage)choices.getScene().getWindow()).close();
				
				//String coords = "fragment frag_a\n16.380  20.017  16.822\n15.898  20.749  17.636\n16.748  18.743  17.075\n\nfragment frag_b\n15.252  17.863  18.838\n14.642  18.742  18.674\n14.861  17.071  18.204\n\nfragment frag_c\n13.634  16.902  22.237\n14.110  15.961  22.470\n14.051  17.676  22.864\n";
	        	StringBuilder sb = new StringBuilder();
	        	for (int i = 0; i < to_be_submitted.size(); i ++) {
	        		sb.append("fragment frag_" + i + "\n");
	        		for (int j = 0; j < groups.get((Integer) to_be_submitted.get(i)).size(); j ++) {
	        			if (j >= 3) {
	        				break;
	        			}
	        			sb.append(atom_list.get((int) groups.get((Integer) to_be_submitted.get(i)).get(j)).x + "  " + atom_list.get((int) groups.get(i).get(j)).y + "  " + atom_list.get((int) groups.get(i).get(j)).z + "\n");
	        		}
	        		sb.append("\n");
	        	}
//	        	ArrayList job_ids = new ArrayList<>();
//	        	for (int i = 0; i < groups.size(); i ++) {
//	        		StringBuilder inp_file = new StringBuilder();
//	        		inp_file.append(" $contrl units=angs local=boys runtyp=makefp\n");
//	        		inp_file.append("       mult=1 icharg=0 coord=cart icut=11 $end\n");
//	        		inp_file.append(" $system timlim=99999   mwords=200 $end\n");
//	        		inp_file.append(" $scf soscf=.f. dirscf=.t. diis=.t. CONV=1.0d-06  $end\n");
//	        		inp_file.append(" $basis gbasis=n31 ngauss=6 ndfunc=1 $end\n");
//	        		inp_file.append(" $DAMP IFTTYP(1)=2,0 IFTFIX(1)=1,1 thrsh=500.0 $end\n");
//	        		inp_file.append(" $MAKEFP  POL=.t. DISP=.f. CHTR=.f.  EXREP=.f. $end\n");
//	        		inp_file.append(" $data\n");
//	        		inp_file.append(" frag_"+i+"\n");
//	        		inp_file.append(" C1\n");
//	        		for (int j = 0; j < groups.get(i).size(); j ++) {
//	        			Atom a = (Atom) atom_list.get((int) groups.get(i).get(j));
//	        			inp_file.append("  ");
//	        			inp_file.append(a.type);
//	        			inp_file.append("   ");
//	        			inp_file.append(String.format("%.1f", charges.get(a.type)));
//	        			inp_file.append("   ");
//	        			inp_file.append(Double.toString(a.x));
//	        			inp_file.append("   ");
//	        			inp_file.append(Double.toString(a.y));
//	        			inp_file.append("   ");
//	        			inp_file.append(Double.toString(a.z));
//	        			inp_file.append("\n");
//	        		}
//	        		inp_file.append(" $end\n $comment Atoms to be erased:  $end\n");
//	        		System.out.println(inp_file.toString());
//	        	}
	        	
	        	
	        	
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
				ArrayList total_charges = new ArrayList();
//				for (int i = 0; i < fragments.size(); i ++) {
//					int total_charge = 0;
//					for (int j = 0 ; j < fragments.get(i).size(); j++) {
//						Atom current_atom = (Atom)  fragments.get(i).get(j);
//						if (current_atom.type.matches(".*\\d+.*")) { // atom symbol has digits, treat as charged atom
//							String symbol = current_atom.type;
//							String sign = symbol.substring(symbol.length() - 1);
//							String digits = symbol.replaceAll("\\D+", "");
//							String real_symbol = symbol.substring(0, symbol.length() - 2 - digits.length());
//							if (sign.equals("-")) {
//								total_charge = total_charge + protons.get(real_symbol);
//							} else {
//								total_charge = total_charge + protons.get(real_symbol);
//							}
//						} else {
//							total_charge += protons.get(current_atom.type);
//						}
//						total_charges.add(total_charge);
//					}
//					
//				}
//				Dialog dialog = new Dialog<>();
//				dialog.setTitle("Charge Choices");
//				dialog.setHeaderText("Please input the charge for your fragments:");
//				ButtonType ok = new ButtonType("OK", ButtonData.OK_DONE);
//				dialog.getDialogPane().getButtonTypes().addAll(ok);
//				BorderPane bp = new BorderPane();
				
				
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
	        	
	        	
				
				
				
			} else {
				
			}
			
		} else {
			jmolViewer.runScript("selectionHalos off");
			jmolWindow.repaint();
			((Stage)choices.getScene().getWindow()).close();
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
		
		
		
		
		
		
	}
	
	static class JmolPanel extends JPanel {

        /**
		 * 
		 */
		private static final long serialVersionUID = -3661941083797644242L;
		Viewer viewer;

        private final Dimension currentSize = new Dimension();
        
        

        JmolPanel() {
            viewer = (Viewer) Viewer.allocateViewer(this, new SmarterJmolAdapter(),
                    null, null, null, null, null);
            
            viewer.setAnimationFps(60);
        }

        @Override
        public void paint(Graphics g) {
            getSize(currentSize);
            viewer.renderScreenImage(g, currentSize.width, currentSize.height);
            
            
        }
    }
	
	
	
}
