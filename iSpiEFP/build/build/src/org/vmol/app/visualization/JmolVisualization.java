package org.vmol.app.visualization;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolSelectionListener;
import org.jmol.java.BS;
import org.jmol.viewer.Viewer;
import org.openscience.jmol.app.jmolpanel.console.AppConsole;
import org.vmol.app.MainViewController;
import org.vmol.app.database.DatabaseController;
import org.vmol.app.gamess.gamessInputController;
import org.vmol.app.gamessSubmission.gamessSubmissionHistoryController;
import org.vmol.app.localDataBase.localDataBaseController;
import org.vmol.app.qchem.QChemInputController;
import org.vmol.app.util.PDBParser;
import org.vmol.app.util.UnrecognizedAtomException;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class JmolVisualization {
	private int group = 1;
	private static final Logger logger = Logger.getLogger(JmolVisualization.class); 
	private final Stage primaryStage;
	private JFrame jmolWindow;
	private JFrame multiJmolWindow;
	private Viewer jmolViewer;
	private File currentOpenFile;
	private boolean isCreated = false;
	int currFile = 0;
	public BS selected_atoms = null;
	private boolean isPlaying = false;
	private ArrayList atoms;
	private ArrayList selected = new ArrayList();
	private ArrayList<ArrayList> groups = new ArrayList<ArrayList>();
	private int bond_num;
	private int atom_num;
	private ArrayList<ArrayList> original_bonds;
	private ArrayList<ArrayList> deleted_bonds = new ArrayList<ArrayList>();
	private int os_type; // 0 for linux, 1 for mac, 2 for windows
	private boolean selection_mode;
	
	
	private int picking_mode = 1; // 0 by atoms, 1 by bonds
	
	private static Preferences userPrefs = Preferences.userNodeForPackage(gamessSubmissionHistoryController.class);

	
//	public void selectionChanged(BS selection) {
//
//		
//		selected_atoms = (BS) selection.clone();
//
//		
//		
//		
//	}
	
	

	public JmolVisualization(Stage primaryStage, boolean selection_mode) {
		this.primaryStage = primaryStage;
		this.selection_mode = selection_mode;
		
	}
	
	public void close() {
		jmolWindow.setVisible(false);
	}
	
	private int nextFragment(int[] map) {
		for (int i = 0; i < map.length; i ++) {
			if (map[i] == -1) {
				return i;
			}
		}
		return -1;
	}
	
	
	private void dfs(int curr_atom, int group_num, int[] map, boolean[][] connections) { 
		map[curr_atom] = group_num;
		for (int i = 0; i < atom_num; i ++) {
			if (connections[curr_atom][i] == true) {
				if (map[i] == -1) {
					dfs(i, group_num, map, connections);
				}
			}
		}
	}
	
	public void show(File xyzFile) throws IOException, UnrecognizedAtomException {
        if (jmolWindow == null) {
        	if (System.getProperty("os.name").startsWith("Mac")) {
        		os_type = 1;
        	} else if (System.getProperty("os.name").startsWith("Linux")) {
        		os_type = 0;
        	} else {
        		os_type = 2;
        	}
        	atoms = PDBParser.parse(xyzFile);
        	atom_num = atoms.size();
            logger.info("Creating Jmol window.");
            jmolWindow = new JFrame("Visualization");
            jmolWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    logger.info("Jmol window closing.");
                    System.out.println("Jmol window closing");
                    jmolWindow.dispose();
                    jmolWindow = null;
                    jmolViewer = null;
                    setCurrentOpenFile(null);
                    
                }
            });
            jmolWindow.setSize(600, 600);
            
            
            
            Container contentPane = jmolWindow.getContentPane();
            JmolPanel jmolPanel = new JmolPanel(this);

            // main panel -- Jmol panel on top
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            
            
           
            // Text panel on right
            if (selection_mode == true) {
            	
            
            JTextArea txt = new JTextArea();
    	    panel.add(txt, BorderLayout.EAST);
    	    
            JToolBar control = new JToolBar();
            control.setLayout(new GridLayout(1,3));
            
            JMenuBar menuBar = new JMenuBar();
            JMenu controlMenu = new JMenu("Control");
            JMenu switch_mode = new JMenu("Selection Mode");
            JMenuItem by_atoms = new JMenuItem(new AbstractAction("by atoms") {
            	public void actionPerformed(ActionEvent e) {
            		picking_mode = 0;
            		jmolViewer.runScript("set picking off");
            		jmolViewer.runScript("selectionHalos on");
            		jmolViewer.runScript("set pickingstyle SELECT DRAG");
                    jmolViewer.runScript("set picking atom");
            	}
            });
            JMenuItem by_bonds = new JMenuItem(new AbstractAction("by bonds") {
            	public void actionPerformed(ActionEvent e) {
            		picking_mode = 1;
            		jmolViewer.runScript("selectionHalos off");
            		jmolViewer.runScript("set bondpicking true");
                    jmolViewer.runScript("set picking deletebond");
            	}
            });
            
            JMenuItem search_frag = new JMenuItem(new AbstractAction("Search Fragment Parameters") {
            	public void actionPerformed(ActionEvent e) {
            		if (picking_mode == 1) {
    	    			int [] map = new int[atom_num];
            			for (int i = 0; i < atom_num; i ++) {
            				map[i] = -1;
            			}
            			System.out.println(atom_num);
            			boolean[][] connections = new boolean[atom_num][atom_num];
            			for (int i = 0; i < atom_num; i ++) {
            				for (int j = 0; j < atom_num; j ++) {
            					connections[i][j] = false;
            				}
            			}
            			
            			for (int i = 0; i < bond_num; i ++) {
            				if (jmolViewer.ms.bo[i] != null) {
            					int atom1 = jmolViewer.ms.bo[i].getAtomIndex1();
            					int atom2 = jmolViewer.ms.bo[i].getAtomIndex2();
            					connections[atom1][atom2] = true;
            					connections[atom2][atom1] = true;
            					
            				}
            			}
            			
            			int group_no = 0;
            			while (nextFragment(map) != -1) {
            				dfs(nextFragment(map), group_no, map, connections);
            				group_no ++ ;
            			}
            			
            			for (int i = 0; i < group_no; i ++) {
            				ArrayList curr_group = new ArrayList();
            				for (int j = 0; j < atom_num; j ++) {
            					if (map[j] == i) {
            						curr_group.add(j);
            					}
            				}
            				groups.add(curr_group);
            			}
    	    		}
            		if (xyzFile.getName().equals("lysine.pdb")) {
            			final CountDownLatch latch = new CountDownLatch(1);
            			Platform.runLater(new Runnable(){
        					@Override
        					public void run() {
        						Alert alert = new Alert(AlertType.INFORMATION);
								alert.setTitle("Database Results");
								alert.setHeaderText(null);
								alert.setContentText("Parameters not found in local directory, will search remote database now..");
								alert.showAndWait();
								latch.countDown();
        					}
        					});
            			try {
							latch.await();
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} 
            			final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/org/vmol/app/database/database.fxml"));
                		DatabaseController controller;
        				controller = new DatabaseController(groups,jmolViewer,jmolWindow);
        				loader.setController(controller);
        				Platform.runLater(new Runnable(){
        					@Override
        					public void run() {
        						BorderPane bp;
        						try {
        							bp = loader.load();
        							Scene scene = new Scene(bp,659.0,500.0);
        		    	        	Stage stage = new Stage();
        		    	        	stage.initModality(Modality.WINDOW_MODAL);
        		    	        	stage.setTitle("Databse Results");
        		    	        	stage.setScene(scene);
        		    	        	stage.show();
        						} catch (IOException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						}
        						
        					}
        					});
            		} else {
            			final CountDownLatch latch = new CountDownLatch(1);
            			Platform.runLater(new Runnable(){
        					@Override
        					public void run() {
        						Alert alert = new Alert(AlertType.INFORMATION);
                    			alert.setTitle("Local Results");
                    			alert.setHeaderText(null);
                    			alert.setContentText("Parameters not found in local directory, will search remote database now..");
                    			alert.showAndWait();
                    			alert = new Alert(AlertType.INFORMATION);
                    			alert.setTitle("Database Results");
                    			alert.setHeaderText(null);
                    			alert.setContentText("Parameters not found in remote database, will submit jobs to gamess now..");
                    			alert.showAndWait();
                    			for (int i = 0; i < groups.size(); i ++) {
                    				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                					Date date = new Date();
                    				userPrefs.put(xyzFile.getName().substring(0,xyzFile.getName().length()-4) + "_" + i ,date.toString());
                    			}
                    			
                    			final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/org/vmol/app/qchem/QChemInput.fxml"));
                				//String coords = "fragment frag_a\n16.380  20.017  16.822\n15.898  20.749  17.636\n16.748  18.743  17.075\n\nfragment frag_b\n15.252  17.863  18.838\n14.642  18.742  18.674\n14.861  17.071  18.204\n\nfragment frag_c\n13.634  16.902  22.237\n14.110  15.961  22.470\n14.051  17.676  22.864\n";
                            	QChemInputController controller;
                				controller = new QChemInputController("");
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
        					});
            			try {
							latch.await();
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
            			
            			
            		}
            		
            		
            		
            	}
            });
            
            JMenuItem confirm_selections = new JMenuItem(new AbstractAction("Confirm Selection") {
            	public void actionPerformed(ActionEvent e) {
            		if (picking_mode == 0) {
            			for (int i = 0; i < jmolViewer.ms.bo.length; i++) {
                			System.out.print(i);
                			System.out.print(": " + jmolViewer.ms.bo[i].getAtomIndex1());
                			System.out.println("   " + jmolViewer.ms.bo[i].getAtomIndex2());
                		}
                		
                		ArrayList curr_group = new ArrayList();
                		String str = "Group " + Integer.toString(group) + ": ";
                		group++;
                		BS selected_atoms = jmolViewer.bsA();
                		for (int i = 0; i < selected_atoms.length();i++) {
                			if (selected_atoms.get(i)) {
                				selected.add(i);
                				str += atoms.get(i) + ", ";
                				curr_group.add(i);
                			}
                			
                			
                		}
                		groups.add(curr_group);
                		str = str.substring(0,str.length()-2);
                		//str += "\r\n";
                		txt.setText(txt.getText() +  "\r\n" + str);
                		jmolViewer.runScript("select clear");
                		jmolViewer.clearSelection();
                		String cmd = "hide ({";
                		Collections.sort(selected);
                		for (int i = 0; i < selected.size();i++) {
                				cmd += Integer.toString((Integer) selected.get(i)) + " ";
                		}
                		cmd+="})";
                		System.out.println(cmd);
                		jmolViewer.runScript(cmd);
                		
                		jmolWindow.repaint();
            		} else {
            			int [] map = new int[atom_num];
            			for (int i = 0; i < atom_num; i ++) {
            				map[i] = -1;
            			}
            			
            			boolean[][] connections = new boolean[atom_num][atom_num];
            			for (int i = 0; i < atom_num; i ++) {
            				for (int j = 0; j < atom_num; j ++) {
            					connections[i][j] = false;
            				}
            			}
            			
            			for (int i = 0; i < bond_num; i ++) {
            				if (jmolViewer.ms.bo[i] != null) {
            					int atom1 = jmolViewer.ms.bo[i].getAtomIndex1();
            					int atom2 = jmolViewer.ms.bo[i].getAtomIndex2();
            					connections[atom1][atom2] = true;
            					connections[atom2][atom1] = true;
            					
            				}
            			}
            			
            			int group_no = 0;
            			while (nextFragment(map) != -1) {
            				dfs(nextFragment(map), group_no, map, connections);
            				group_no ++ ;
            			}
            			
            			for (int i = 0; i < group_no; i ++) {
            				ArrayList curr_group = new ArrayList();
            				for (int j = 0; j < atom_num; j ++) {
            					if (map[j] == i) {
            						curr_group.add(j);
            					}
            				}
            				groups.add(curr_group);
            			}
            		}
            	}
            });
            
            JMenuItem back = new JMenuItem(new AbstractAction("Back") {
            	public void actionPerformed(ActionEvent e) {
            		if (deleted_bonds!=null && deleted_bonds.size() > 0) {
            			ArrayList bond = deleted_bonds.remove(deleted_bonds.size()-1);
            			String script = "connect (atomno=" + ((Integer)(bond.get(0))+1) + ") (atomno=" + ((Integer)(bond.get(1))+1) + ")";
            			jmolViewer.runScript(script);
            			original_bonds.add(bond);
            			jmolWindow.repaint();
            		}
            		
            	}
            });
            KeyStroke keyStrokeToOpen;
            if (os_type == 1) {
            	keyStrokeToOpen = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
            } else {
            	keyStrokeToOpen = KeyStroke.getKeyStroke(KeyEvent.VK_Z,KeyEvent.CTRL_DOWN_MASK);
            }
            back.setAccelerator(keyStrokeToOpen);
             
            
            switch_mode.add(by_atoms);
            switch_mode.add(by_bonds);
            
            menuBar.add(switch_mode);
            //menuBar.add(confirm_selections);
            //menuBar.add(back);

            control.setFloatable(false);
            panel.add(jmolPanel);
            
            // main panel -- console panel on bottom
    	    JPanel panel2 = new JPanel();
    	    panel2.setLayout(new BorderLayout());
    	    panel2.setPreferredSize(new Dimension(800, 300));
    	    AppConsole console = new AppConsole(jmolPanel.viewer, panel2,
    	        "History State Clear");

    	    JMenuItem show_console = new JMenuItem(new AbstractAction("Show/Hide console") {
            	public void actionPerformed(ActionEvent e) {
            		if (panel2.isVisible()) {
            			panel2.setVisible(false);
            		} else {
            			panel2.setVisible(true);
            		}
            	}
            });
    	    controlMenu.add(back);
    	    controlMenu.add(search_frag);
    	    //controlMenu.add(confirm_selections);
    	    
    	    
    	    JMenuItem play_pause = new JMenuItem(new AbstractAction("Play/Pause") {
    	    	public void actionPerformed(ActionEvent e) {
    	    		if (isPlaying == false) {
    	    			isPlaying = true;
    	    			jmolViewer.runScript("frame play");
    	    		} else {
    	    			isPlaying = false;
    	    			jmolViewer.runScript("animation off");
    	    		}
    	    	}
    	    });
    	    controlMenu.add(play_pause);
    	    controlMenu.add(show_console);
    	    JMenuItem fragmentation_done = new JMenuItem(new AbstractAction("Done") {
    	    	public void actionPerformed(ActionEvent e) {
    	    		
    	    	}
    	    });
    	    
    	    JMenuItem submit_gamess = new JMenuItem(new AbstractAction("Gamess Submission") {
    	    	public void actionPerformed(ActionEvent e) {
    	    		if (picking_mode == 1) {
    	    			int [] map = new int[atom_num];
            			for (int i = 0; i < atom_num; i ++) {
            				map[i] = -1;
            			}
            			System.out.println(atom_num);
            			boolean[][] connections = new boolean[atom_num][atom_num];
            			for (int i = 0; i < atom_num; i ++) {
            				for (int j = 0; j < atom_num; j ++) {
            					connections[i][j] = false;
            				}
            			}
            			
            			for (int i = 0; i < bond_num; i ++) {
            				if (jmolViewer.ms.bo[i] != null) {
            					int atom1 = jmolViewer.ms.bo[i].getAtomIndex1();
            					int atom2 = jmolViewer.ms.bo[i].getAtomIndex2();
            					connections[atom1][atom2] = true;
            					connections[atom2][atom1] = true;
            					
            				}
            			}
            			
            			int group_no = 0;
            			while (nextFragment(map) != -1) {
            				dfs(nextFragment(map), group_no, map, connections);
            				group_no ++ ;
            			}
            			
            			for (int i = 0; i < group_no; i ++) {
            				ArrayList curr_group = new ArrayList();
            				for (int j = 0; j < atom_num; j ++) {
            					if (map[j] == i) {
            						curr_group.add(j);
            					}
            				}
            				groups.add(curr_group);
            			}
    	    		}
                	final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("../gamess/gamessInput.fxml"));
                	gamessInputController controller;
    				controller = new gamessInputController(xyzFile,groups);
    				loader.setController(controller);
    				Platform.runLater(new Runnable(){
    					@Override
    					public void run() {
    						BorderPane bp;
    						try {
    							bp = loader.load();
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
    	    });
    	    
    	    controlMenu.add(submit_gamess);
    	    
    	    menuBar.add(controlMenu);
    	    
			panel.add(menuBar,BorderLayout.NORTH);

    	    // Can use a different JmolStatusListener or JmolCallbackListener interface
    	    // if you want to, but AppConsole itself should take care of any console-related callbacks
    	    jmolPanel.viewer.setJmolCallbackListener(console);
    	    
    	    //JmolSelectionListener selectionListener = new JmolSelectionListener();
    	    //jmolPanel.viewer.addSelectionListener(this);
    	    panel.add("South", panel2);
    	    panel2.setVisible(false);
        } else {
        	JMenu controlMenu = new JMenu("Fragmentation");
        	panel.add(jmolPanel);
        	JMenuBar control = new JMenuBar();
            control.setLayout(new GridLayout(1,3));
            
            JMenuItem manual = new JMenuItem(new AbstractAction("Manual") {
            	public void actionPerformed(ActionEvent e) {
            		
            			Stage currStage = (Stage) primaryStage.getScene().getWindow();
            			JmolVisualization jv = new JmolVisualization(currStage, true);
            			MainViewController.getJmolVisualization().close();
            			MainViewController.setJmolVisualization(jv);
            			try {
							jv.show(new File(MainViewController.getLastOpenedFile()));
						} catch (IOException | UnrecognizedAtomException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
            		
            	}
            });
            
            JMenuItem auto = new JMenuItem(new AbstractAction("Automatic") {
            	public void actionPerformed(ActionEvent e) {
            		if (xyzFile.getName().equals("npt0000.pdb")) {
            			Stage currStage = (Stage) primaryStage.getScene().getWindow();
            			JmolVisualization jv = new JmolVisualization(currStage, false);
            			MainViewController.getJmolVisualization().close();
            			MainViewController.setJmolVisualization(jv);
            			try {
            				String homeDir = System.getProperty("user.home");
							jv.show(new File(homeDir + "/Desktop/benzene_na_water_box/benzene_na_water_simulation/npt0000_pbc_applied.xyz"));
						} catch (IOException | UnrecognizedAtomException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
            			
            			final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/org/vmol/app/localDataBase/localDatabase.fxml"));
            			localDataBaseController controller;
        				controller = new localDataBaseController();
        				loader.setController(controller);
        				Platform.runLater(new Runnable(){
        					@Override
        					public void run() {
        						BorderPane bp;
        						try {
        							bp = loader.load();
        							Scene scene = new Scene(bp,659.0,500.0);
        		    	        	Stage stage = new Stage();
        		    	        	stage.initModality(Modality.WINDOW_MODAL);
        		    	        	stage.setTitle("Local Results");
        		    	        	stage.setScene(scene);
        		    	        	stage.show();
        						} catch (IOException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						}
        						
        					}
        					});
            		} else {
            			final CountDownLatch latch = new CountDownLatch(1);
            			Platform.runLater(new Runnable(){
        					@Override
        					public void run() {
        						Alert alert = new Alert(AlertType.INFORMATION);
								alert.setTitle("Fragmentation error");
								alert.setHeaderText(null);
								alert.setContentText("System is currently not supported by the fragmentation script, please use manual fragmentation..");
								alert.showAndWait();
								latch.countDown();
        					}
        					});
            			try {
							latch.await();
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} 
            		}
            	}
            });
            
            JMenuItem no = new JMenuItem(new AbstractAction("No fragmentation") {
            	public void actionPerformed(ActionEvent e) {
            		if (xyzFile.getName().equals("npt0000.pdb")) {
            			
            		}
            	}
            });
            
           
            
            
            controlMenu.add(manual);
            controlMenu.add(auto);
            controlMenu.add(no);
            
            control.add(controlMenu);
            panel.add("North",control);
            
        }
            contentPane.add(panel);
            jmolViewer = jmolPanel.viewer;

            alignWindowPosition(jmolWindow);
            jmolWindow.setVisible(true);
            isCreated = true;

        } else {
            logger.debug("Bringing existing Jmol window to front.");
        }
        openFile(xyzFile.getAbsoluteFile());
        jmolWindow.toFront();
        
    }
	
	public void showMultipleFiles(List<File> files) {
		
		if (multiJmolWindow == null) {
			multiJmolWindow = new JFrame();
	        
	        JPanel control = new JPanel();
	        control.add(new JButton(new AbstractAction("\u22b2Prev") {

	            @Override
	            public void actionPerformed(ActionEvent e) {
	                currFile--;
	                if (currFile < 0) currFile = files.size() - 1;
	                openCurrentFile(files);
	            }
	        }));
	        control.add(new JButton(new AbstractAction("Next\u22b3") {

	            @Override
	            public void actionPerformed(ActionEvent e) {
	                currFile = (currFile+1)%files.size();
	                openCurrentFile(files);
	            }
	        }));
	        multiJmolWindow.add(control, BorderLayout.SOUTH);
	        multiJmolWindow.pack();
	        multiJmolWindow.setLocationRelativeTo(null);
	        
            multiJmolWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("Jmol window closing");
                    multiJmolWindow = null;
                    jmolViewer = null;
                }
            });
            multiJmolWindow.setSize(600, 600);

            Container contentPane = multiJmolWindow.getContentPane();
            JmolPanel jmolPanel = new JmolPanel(this);

            // main panel -- Jmol panel on top
            JPanel panel = new JPanel();
            panel.setLayout(new CardLayout());
            panel.add(jmolPanel);

            // main panel -- console panel on bottom -- Not necessary for multiJmolWindow
//    	    JPanel panel2 = new JPanel();
//    	    panel2.setLayout(new BorderLayout());
//    	    panel2.setPreferredSize(new Dimension(800, 300));
//    	    AppConsole console = new AppConsole(jmolPanel.viewer, panel2,
//    	        "History State Clear");
    	    
    	    // Can use a different JmolStatusListener or JmolCallbackListener interface
    	    // if you want to, but AppConsole itself should take care of any console-related callbacks
//    	    jmolPanel.viewer.setJmolCallbackListener(console);
//    	    panel.add("South", panel2);
            
            contentPane.add(panel);
            jmolViewer = jmolPanel.viewer;

            alignWindowPosition(multiJmolWindow);
            multiJmolWindow.setVisible(true);
            
        } else {
            logger.debug("Bringing existing Jmol window to front.");
        }
        openCurrentFile(files);
        multiJmolWindow.toFront();
	}

    private void openCurrentFile(List<File> files) {
    	String strError = jmolViewer.openFile(files.get(currFile).getAbsolutePath());
        if (strError == null) {
            jmolViewer.runScript("select clear");
            //jmolViewer.clearSelection();
            jmolWindow.repaint();
            
        } else {
            System.out.println("Error while loading XYZ file. " + strError);
        }
	}

	private void alignWindowPosition(JFrame window) {
    	System.out.println(primaryStage.getX());
    	System.out.println(primaryStage.getY());
    	
        double x = primaryStage.getX() - 100;
        double y = primaryStage.getY() - 30;

        logger.info(String.format("Positioning jmol window at position x=%f y=%f", x, y));

        window.setLocation((int) x, (int) y);
    }

    public void openFile(File file) {
        if (file != null) {
            boolean isDifferentFile = getCurrentOpenFile() == null
                    || !FilenameUtils.equalsNormalized(getCurrentOpenFile().getAbsolutePath(), file.getAbsolutePath());
            if (jmolViewer != null && isDifferentFile) {
                String strError = jmolViewer.openFile(file.getAbsolutePath());
                if (strError == null) {
                    jmolViewer.runScript("select clear");
                    //jmolViewer.clearSelection();
                    jmolViewer.runScript("set pickingstyle SELECT DRAG");
                    jmolViewer.runScript("set picking atom");
                    jmolViewer.runScript("selectionHalos on");
                    jmolViewer.runScript("animation fps 10");
                    jmolViewer.runScript("selectionHalos off");
            		jmolViewer.runScript("set bondpicking true");
                    jmolViewer.runScript("set picking deletebond");
                    bond_num = jmolViewer.ms.bo.length;
                    
                    original_bonds = new ArrayList<ArrayList>();
                    
                    for (int i = 0; i < bond_num; i ++) {
        				if (jmolViewer.ms.bo[i] != null) {
        					int atom1 = jmolViewer.ms.bo[i].getAtomIndex1();
        					int atom2 = jmolViewer.ms.bo[i].getAtomIndex2();
        					ArrayList bond = new ArrayList();
        					bond.add(atom1);
        					bond.add(atom2);
        					original_bonds.add(bond);
        					
        				}
        			}
                	//System.out.println("bond numbers: " + bond_num);
                    //jmolViewer.evalString(strScript);
                    setCurrentOpenFile(file);
                } else {
                    logger.error("Error while loading XYZ file. " + strError);
                }
            }
        }
    }
    
    public File getCurrentOpenFile() {
		return currentOpenFile;
	}

	public void setCurrentOpenFile(File currentOpenFile) {
		this.currentOpenFile = currentOpenFile;
	}
	
	private ArrayList find_deleted_bonds() {
		ArrayList<ArrayList> tmp = new ArrayList<ArrayList>();
		
		for (int i = 0; i < bond_num; i ++) {
			if (jmolViewer.ms.bo[i] != null) {
				int atom1 = jmolViewer.ms.bo[i].getAtomIndex1();
				int atom2 = jmolViewer.ms.bo[i].getAtomIndex2();
				ArrayList bond = new ArrayList();
				bond.add(atom1);
				bond.add(atom2);
				
				tmp.add(bond);
			}
		}
		if (original_bonds == null) {
			return null;
		}
		if (tmp.size() == original_bonds.size()) {
			return null;
		}
	
		
		
		for (int i = 0; i < original_bonds.size(); i ++) {
			
				
			int index = tmp.indexOf(original_bonds.get(i));
			if (index == -1) {
					//deleted_atom.add(original_bonds.get(i).get(0));
					//deleted_atom.add(original_bonds.get(i).get(1));
				return original_bonds.remove(i);
					//found = true;
			}
					
				
				
			
		}
		return null;
		
	}

	static class JmolPanel extends JPanel {

        /**
		 * 
		 */
		private static final long serialVersionUID = -3661941083797644242L;
		static Viewer viewer;

        private final Dimension currentSize = new Dimension();
        
        private JmolVisualization jv;

        JmolPanel(JmolVisualization jv) {
        	this.jv = jv;
            viewer = (Viewer) Viewer.allocateViewer(this, new SmarterJmolAdapter(),
                    null, null, null, null, null);
            viewer.addSelectionListener(new JmolSelectionListener() {
            	public void selectionChanged(BS selection) {
            		//System.out.println("things changed");
            	}
            });
            viewer.setAnimationFps(60);
        }

        @Override
        public void paint(Graphics g) {
            getSize(currentSize);
            viewer.renderScreenImage(g, currentSize.width, currentSize.height);
            ArrayList bond = jv.find_deleted_bonds();
            if (bond != null) {
            	
            	System.out.println("bond between " + bond.get(0) + "  " + bond.get(1));
            	jv.deleted_bonds.add(bond);
            }
            
        }
    }
}

