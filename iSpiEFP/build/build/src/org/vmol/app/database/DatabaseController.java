package org.vmol.app.database;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.controlsfx.control.action.Action;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolSelectionListener;
import org.jmol.java.BS;
import org.jmol.viewer.Viewer;
import org.vmol.app.database.DatabaseRecord;
import org.vmol.app.gamessSubmission.gamessSubmissionHistoryController;
import org.vmol.app.qchem.QChemInputController;
import org.vmol.app.submission.SubmissionHistoryController;
import org.vmol.app.visualization.JmolVisualization;

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
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
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
	public DatabaseController(ArrayList<ArrayList> groups, Viewer jmolViewer, JFrame jmolWindow) {
		this.groups = groups;
		this.jmolViewer = jmolViewer;
		this.jmolWindow = jmolWindow;
	}
	
	@FXML  
	public void initialize() {
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
							if (previous_selected_group.equals("")) {
								left_viewer = jmolPanel.viewer;
								right_viewer = jmolPanel_right.viewer;
								int choose = 0;
					            if (group_selector.getValue().equals("A")) {
					            	choose = 0;
					            } else if (group_selector.getValue().equals("B")) {
					            	choose = 1;
					            } else {
					            	choose = 2;
					            }
								left_viewer.openFile(homeDir+"/Desktop/lysine_demo/files_to_calc_rmsd/gamess_" + choose + "_a.xyz");
					           
					            String choose_right = "";
					            if (row.getItem().getChoice().equals("1")) {
					            	choose_right = "a";
					            } else if (row.getItem().getChoice().equals("2")) {
					            	choose_right = "b";
					            } else if (row.getItem().getChoice().equals("3")) {
					            	choose_right = "c";
					            } else {
					            	choose_right = "d";
					            }
					            right_viewer.openFile(homeDir+"/Desktop/lysine_demo/files_to_calc_rmsd/gamess_" + choose + "_" + choose_right + ".xyz");
					            previous_selected_group = group_selector.getValue();
					            
					            comparison.setVisible(true);
					            comparison.repaint();
							} else   {
								int choose = 0;
					            if (group_selector.getValue().equals("A")) {
					            	choose = 0;
					            } else if (group_selector.getValue().equals("B")) {
					            	choose = 1;
					            } else {
					            	choose = 2;
					            }
					            String choose_right = "";
					            if (row.getItem().getChoice().equals("1")) {
					            	choose_right = "a";
					            } else if (row.getItem().getChoice().equals("2")) {
					            	choose_right = "b";
					            } else if (row.getItem().getChoice().equals("3")) {
					            	choose_right = "c";
					            } else {
					            	choose_right = "d";
					            }
					            left_viewer.openFile(homeDir+"/Desktop/lysine_demo/files_to_calc_rmsd/gamess_" + choose + "_a.xyz");
								right_viewer.openFile(homeDir+"/Desktop/lysine_demo/files_to_calc_rmsd/gamess_" + choose + "_" + choose_right + ".xyz");
								comparison.setVisible(true);
								comparison.repaint();
							} 
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
			no.add("A");
			no.add("B");
			no.add("C");
			group_selector.setItems(FXCollections.observableArrayList(no));
			group_selector.setValue("A");
			group_selector.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
				int group_index = newValue.charAt(0) - 'A';
				String selecting2 = "select ({";
				for (int i = 0; i < groups.get(group_index).size(); i++) {
					selecting2 += groups.get(group_index).get(i) + " ";
				}
				selecting2 += "})";
				jmolViewer.runScript(selecting2);
				jmolWindow.repaint();
			});
			
			ArrayList<List<DatabaseRecord>> items = new ArrayList<List<DatabaseRecord>>();
			
			List<DatabaseRecord> items1 = Arrays.asList(new DatabaseRecord("1","0",true),
					new DatabaseRecord("2","0",false),
					new DatabaseRecord("3","0.967048526",false),
					new DatabaseRecord("4","1.716201617",false));
			List<DatabaseRecord> items2 = Arrays.asList(new DatabaseRecord("1","0",true),
					new DatabaseRecord("2","0.676954958",false),
					new DatabaseRecord("3","1.279396548",false),
					new DatabaseRecord("4","2.463240909",false));
			List<DatabaseRecord> items3 = Arrays.asList(new DatabaseRecord("1","0",true),
					new DatabaseRecord("2","0.995958917",false),
					new DatabaseRecord("3","2.3000735202074365",false),
					new DatabaseRecord("4","3.051696274",false));
			items.add(items1);
			items.add(items2);
			items.add(items3);
			
			
			data = new ArrayList<ObservableList<DatabaseRecord>>();
			
			for (int i = 0; i < groups.size(); i ++) {
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
					data_subset.get(prev_selection_index).checkProperty().set(false);
					int curr = Integer.parseInt(d.getChoice())-1;
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
		int index = group_selector.getValue().charAt(0) - 'A';
		curr_group_index = index;
		choices.setItems(data.get(index));
		prev_selection_index = (int) final_selections.get(index).get(0);
		ObservableList<DatabaseRecord> data_subset = data.get(index);
		for (int j = 0; j < data_subset.size(); j ++) {
			String item = Integer.toString(j);
			DatabaseRecord d = data_subset.get(j);
			d.checkProperty().addListener((obs, wasCompleted, isNowCompleted) -> {
				data_subset.get(prev_selection_index).checkProperty().set(false);
				int curr = Integer.parseInt(d.getChoice())-1;
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
	protected void finish() {
		ArrayList to_be_submitted = new ArrayList<>();
		for (int i = 0; i < final_selections.size(); i ++) {
			boolean all = true;
			if (final_selections.get(i).size() == 0) {
				to_be_submitted.add(i);
				all = false;
				System.out.println("currently selected record: none");
			} else {
				System.out.println("currently selected record: " + final_selections.get(i).get(0));
			}
			//System.out.println("currently selected record: " + final_selections.get(i).get(0));
			if (all == false) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Gamess");
				alert.setHeaderText(null);
				alert.setContentText("There are groups you have not picked parameters for, do you want to calculate them by Gamess?");
				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK) {
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
					Date date = new Date();
					
					for (int j = 0; j < to_be_submitted.size(); j ++) {
						int num = (int) to_be_submitted.get(j);
						userPrefs.put(Character.toString((char) ('A'+ num)) ,date.toString());
						System.out.println(date.toString());
					}
					
					
					
				} else {
					
				}
				
			} else {
				final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("../qchem/QChemInput.fxml"));
				String coords = "fragment frag_a\n16.380  20.017  16.822\n15.898  20.749  17.636\n16.748  18.743  17.075\n\nfragment frag_b\n15.252  17.863  18.838\n14.642  18.742  18.674\n14.861  17.071  18.204\n\nfragment frag_c\n13.634  16.902  22.237\n14.110  15.961  22.470\n14.051  17.676  22.864\n";
            	QChemInputController controller;
				controller = new QChemInputController(coords);
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
