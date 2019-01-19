package org.vmol.app.visualizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.jmol.api.JmolSelectionListener;
import org.jmol.java.BS;
import org.jmol.util.Logger;
import org.jmol.viewer.Viewer;
import org.openscience.jmol.app.jmolpanel.console.AppConsole;
import org.vmol.app.Main;
import org.vmol.app.Main.JmolPanel;
import org.vmol.app.database.DatabaseController;
import org.vmol.app.database.DatabaseController2;
import org.vmol.app.util.PDBParser;

import javafx.application.Platform;
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
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class JmolVisualizer {
	
	private static JmolPanel jmolPanel;
	private static Viewer jmolViewer;
	private static LinkedList<Integer> adj[];
	private static List<ArrayList<Integer>> fragment_list;
	
	
	public JmolVisualizer(JmolPanel jmolPanel){
		this.jmolPanel = jmolPanel;
		this.jmolViewer = jmolPanel.viewer;
	}

	//check if file is valid, then execute file
	public void show(File file) {
		//check if file is valid
		String strError = null;
		if(file != null){
			if ((strError = jmolViewer.openFile(file.getAbsolutePath())) != null) {	
				Logger.error("Error while loading XYZ file. " + strError);
			} else {
				//visualize file, no errors opening it
				visualize();
			}
		}
	}
	
	//start thread for SwingNode to run jmol, main function for visualizer
	public void visualize() {   		
    	//get buttons from left pane and assign definitions
   		ObservableList<Node> buttonList = getButtonList();
     	ToggleButton button_halo_on = (ToggleButton) buttonList.get(0);
    	ToggleButton button_fragmentation = (ToggleButton) buttonList.get(1);
   		//ToggleButton button_pick_by_bonds = (ToggleButton) buttonList.get(2);
   		//ToggleButton button_pick_by_atoms = (ToggleButton) buttonList.get(3);
   		Button button_back = (Button) buttonList.get(2);
     	ToggleButton button_play_pause = (ToggleButton) buttonList.get(3);
     	Button button_show_console = (Button) buttonList.get(4);
     	Button button_submit = (Button) buttonList.get(5);
     		
     	//init icons
     	Image play = new Image("file:icons8-play-filled-30.png");
    	Image pause = new Image("file:icons8-pause-30.png");
     		
   
   		System.out.println("running visualizer...");
    			
   		//initialize jmol with default settings
        initJmol();
        	    
        //handle ctrl-z
        InputMap inputMap = jmolPanel.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "ctrl-z Action");
        ActionMap actionMap = jmolPanel.getActionMap();
        actionMap.put("ctrl-z Action", new AbstractAction() {
        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent arg0) {
        		undo();
        	}
	    });	    	    
        // Create the Event-Handlers for the Buttons
        button_halo_on.setOnAction(new EventHandler <ActionEvent>()
        {
        	public void handle(ActionEvent event)
        	{
        		if (button_halo_on.isSelected()) {
        			jmolViewer.clearSelection();	
               		jmolViewer.runScript("selectionHalos on");
          
          	    } else {
           	    	jmolViewer.runScript("selectionHalos off");
           	    	jmolViewer.runScript("select; halos off");
           	    	jmolViewer.clearSelection();
                  	jmolPanel.repaint();
          	    }
        	}
        });
        button_fragmentation.setOnAction(new EventHandler <ActionEvent>()
        {
        	public void handle(ActionEvent event)
          	{
        		if (button_fragmentation.isSelected()) {
                  	jmolViewer.runScript("set bondpicking true");
                   	jmolViewer.runScript("set picking deletebond");
           	    } else {
           	    	jmolViewer.runScript("set bondpicking false");
                }
        	}
        });
        /*button_pick_by_bonds.setOnAction(new EventHandler <ActionEvent>()
        {
           	public void handle(ActionEvent event)
           	{
           		if (button_pick_by_bonds.isSelected()) {
                  	jmolViewer.runScript("set bondpicking true");
                  	jmolViewer.runScript("set picking deletebond");
                } else {
          	    	jmolViewer.runScript("set bondpicking false");
           	    }
            }
        });
        button_pick_by_atoms.setOnAction(new EventHandler <ActionEvent>()
       	{
        	public void handle(ActionEvent event)
                {
                        if (button_pick_by_atoms.isSelected()) {
                        	jmolViewer.runScript("set picking on");
                        	jmolViewer.runScript("set picking deleteatom");
                	    } else {
                	    	jmolViewer.runScript("set picking off");
                	    }
                }
        });*/
        button_back.setOnAction(new EventHandler <ActionEvent>()
       	{
        	public void handle(ActionEvent event)
              	{
               		undo();
               	}
       	});
        button_play_pause.setOnAction(new EventHandler <ActionEvent>()
        {
        	public void handle(ActionEvent event)
          	{
          		button_play_pause.setText("");
          		if (button_play_pause.isSelected()) {
           	        button_play_pause.setGraphic(new ImageView(pause));
           	    	jmolViewer.runScript("frame play");
           	    } else {
           	    	button_play_pause.setGraphic(new ImageView(play));
                  	jmolViewer.runScript("animation off");
           	    }
            }
        });              
        button_show_console.setOnAction(new EventHandler <ActionEvent>()
        {
          	public void handle(ActionEvent event)
          	{
         		//create window for console
           		JFrame consoleFrame = new JFrame();
           		consoleFrame.setSize(800, 400);
              	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                consoleFrame.setLocation(
                		(screenSize.width - 500) / 2,
                   		(screenSize.height) / 2);
                consoleFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        
                //create and connect panel with jmol console
                JPanel console_panel = new JPanel();
          	    console_panel.setLayout(new BorderLayout());
           	    AppConsole console = new AppConsole(jmolPanel.viewer, console_panel,
           	        "History State Clear");
           	    
           	    // Callback any scripts run in console to jmol viewer in main
                jmolPanel.viewer.setJmolCallbackListener(console);
                     
                //show console
                consoleFrame.getContentPane().add(console_panel);
                consoleFrame.setVisible(true);
                java.awt.EventQueue.invokeLater(new Runnable() {
                	@Override
                    public void run() {
                		consoleFrame.toFront();
                        consoleFrame.repaint();
                    }
                });
          	}
        });  
        button_submit.setOnAction(new EventHandler <ActionEvent>()
       	{
			public void handle(ActionEvent event)
            {
        		System.out.println("submit");
        		SplitPane splitpane = (SplitPane) Main.getMainLayout().getChildren().get(2);
        		//get items 1 and 2 from main split pane
        		ObservableList<Node> list = splitpane.getItems();
        		
        		//get item 2 from main split pane
        		SplitPane nodepane = (SplitPane) list.get(1);
        		
        		//shift divider
        		nodepane.setDividerPositions(0.6f, 0.4f);
        	    
        		//reset size of main pane
        		jmolPanel.setSize((new Dimension(600,595)));
        		jmolPanel.repaint();
        		
        		Main.showJmolViewer(false, "file:test.pdb");
        	    //jmolPanel.setPreferredSize(new Dimension(940, 595));
        		
        		//load items into list view
        		//ListView<String> aux_list = loadAuxiliaryList2();
        		TableView aux_table = loadAuxiliaryList();
        		
        		DatabaseController2 DBcontroller;
				DBcontroller = new DatabaseController2(jmolViewer, Main.auxiliaryJmolPanel.viewer, aux_table, fragment_list);
				try {
					DBcontroller.run();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		            	
            }
       	});
	} //end of visualize function 
	
	//get buttons from left pane in window
	public ObservableList<Node> getButtonList() {	
 		VBox vbox = (VBox) Main.getMainLayout().getChildren().get(0);
 		Pane buttonBar = (Pane)vbox.getChildren().get(1);
 		
		ObservableList<Node> buttonList = buttonBar.getChildren();
		
 		return buttonList;
	}
	
	public void initJmol(){
		int bond_num = jmolViewer.ms.bo.length;
		for (int i = 0; i < bond_num; i ++) {
			if (jmolViewer.ms.bo[i] != null) {
				int atom1 = jmolViewer.ms.bo[i].getAtomIndex1();
				int atom2 = jmolViewer.ms.bo[i].getAtomIndex2();
				ArrayList bond = new ArrayList();
				bond.add(atom1);
				bond.add(atom2);
				jmolPanel.original_bonds.add(bond);
				
			}
		}
		//initialize fragment list
		displayFragments(jmolPanel);
		
		//default settings
		jmolViewer.runScript("select clear");
        jmolViewer.clearSelection();
        jmolViewer.runScript("set pickingstyle SELECT DRAG");
        jmolViewer.runScript("set picking atom");
        jmolViewer.runScript("animation fps 10");
        jmolViewer.runScript("selectionHalos off");
		jmolViewer.runScript("set bondpicking true");
		jmolViewer.runScript("color halos gold");
	
	}
	
	private void undo() {
		
		if (jmolPanel.deleted_bonds!=null && jmolPanel.deleted_bonds.size() > 0) {
			ArrayList bond = jmolPanel.deleted_bonds.remove(jmolPanel.deleted_bonds.size()-1);
			String script = "connect (atomno=" + ((Integer)(bond.get(0))+1) + ") (atomno=" + ((Integer)(bond.get(1))+1) + ")";
			jmolViewer.runScript(script);
			jmolPanel.original_bonds.add(bond);
			jmolPanel.repaint();
			//reset list
			displayFragments(jmolPanel);
		}
		
	}
	
	public TableView loadAuxiliaryList() {
        //get list
        SplitPane splitpane = (SplitPane) Main.getMainLayout().getChildren().get(2);
        //splitpane.getItems().add(swingNode);
        //get items 1 and 2 from main split pane
        ObservableList<Node> list = splitpane.getItems();
        
        //get item 2 from main split pane
        SplitPane nodepane = (SplitPane) list.get(1);
        //add swingnode to left split pane
        ObservableList<Node> sublist = nodepane.getItems();
        
        
        SplitPane vertSplit = (SplitPane) sublist.get(1);
        ObservableList<Node> vertlist = vertSplit.getItems();
        
        //ListView<String> listView = (ListView) vertlist.get(1);
        
        TableView table = (TableView) vertlist.get(1);
        //load up list
        //ObservableList<String> data = FXCollections.observableArrayList();

        //ListView<String> listView = new ListView<String>(data);
        //listView.setPrefSize(200, 250);
        
        return table;

        /*
        data.addAll("lysine_0.xyz","lysine_1.xyz","lysine_2.xyz");

        listView.setItems(data);
        
        //set listener to items
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // Your action here
                System.out.println("Selected item: " + newValue);
                Main.auxiliaryJmolPanel.viewer.openFile("file:"+newValue);
            }
        });
        return null;*/
    }
	
	public ListView<String> loadAuxiliaryList2() {
		//get list
		SplitPane splitpane = (SplitPane) Main.getMainLayout().getChildren().get(2);
		//splitpane.getItems().add(swingNode);
		//get items 1 and 2 from main split pane
		ObservableList<Node> list = splitpane.getItems();
		
		//get item 2 from main split pane
		SplitPane nodepane = (SplitPane) list.get(1);
		//add swingnode to left split pane
		ObservableList<Node> sublist = nodepane.getItems();
		
		
		SplitPane vertSplit = (SplitPane) sublist.get(1);
		ObservableList<Node> vertlist = vertSplit.getItems();
		ListView<String> listView = (ListView) vertlist.get(1);
		
		//load up list
		//ObservableList<String> data = FXCollections.observableArrayList();

	    //ListView<String> listView = new ListView<String>(data);
	    listView.setPrefSize(200, 250);
	    
	    return listView;

	    /*
	    data.addAll("lysine_0.xyz","lysine_1.xyz","lysine_2.xyz");

	    listView.setItems(data);
	    
	    //set listener to items
	    listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
        	@Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            // Your action here
	            System.out.println("Selected item: " + newValue);
	            Main.auxiliaryJmolPanel.viewer.openFile("file:"+newValue);
	        }
	    });
	    return null;*/
	}
	
	//find deleted bonds, called every deletion of a bound in paint under jmolPanel in Main
	public static ArrayList find_deleted_bonds(JmolPanel jmolPanel) {
		ArrayList<ArrayList> tmp = new ArrayList<ArrayList>();
		int bond_num  = jmolPanel.viewer.ms.bo.length;
		
		
		for (int i = 0; i < bond_num; i ++) {
			if (jmolPanel.viewer.ms.bo[i] != null) {
				int atom1 = jmolPanel.viewer.ms.bo[i].getAtomIndex1();
				int atom2 = jmolPanel.viewer.ms.bo[i].getAtomIndex2();
				ArrayList bond = new ArrayList();
				bond.add(atom1);
				bond.add(atom2);
				
				tmp.add(bond);
			}
		}
		if (jmolPanel.original_bonds == null) {
			return null;
		}
		if (tmp.size() == jmolPanel.original_bonds.size()) {
			return null;
		}
		for (int i = 0; i < jmolPanel.original_bonds.size(); i ++) {
			int index = tmp.indexOf(jmolPanel.original_bonds.get(i));
			if (index == -1) {
					//deleted_atom.add(original_bonds.get(i).get(0));
					//deleted_atom.add(original_bonds.get(i).get(1));
				return jmolPanel.original_bonds.remove(i);
					//found = true;
			}
			
		}
		return null;
	}
	
	private static void loadFragmentList(){
		//get list
		SplitPane splitpane = (SplitPane) Main.getMainLayout().getChildren().get(2);
		//splitpane.getItems().add(swingNode);
		//get items 1 and 2 from main split pane
		ObservableList<Node> list = splitpane.getItems();
		ListView<String> listView = (ListView) list.get(0);
				
		//load up fragment list
		//ArrayList<Integer>
	
		ObservableList<String> data = FXCollections.observableArrayList();
	
		int fragmentCounter = 1;
		for (ArrayList<Integer> frag : fragment_list) {
		    if(frag.size() > 0){
		    	System.out.println("Dumping frag contents");
		    	for(int piece : frag){
		    		System.out.println(piece);
		    	}
		    	data.add("Fragment " + fragmentCounter++);
		    }
		}
		listView.setItems(data);
			    
		//set listener to items
		listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
	    	@Override
	    	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	    		// Your action here
	            String[] arrOfStr = newValue.split(" "); 
	            System.out.println("split0: " + arrOfStr[0] + " split1: " + arrOfStr[1]);
	            int index = Integer.parseInt(arrOfStr[1]);
	    		System.out.println("Selected item: " + index);
	    		int i = 1;
	    		
	    		for (ArrayList<Integer> frag : fragment_list) {
	    		    if(i == index){
	    		    	//highlight this fragment
	    		    	//jmolViewer.runScript("selectionHalos on");
	    		    	//jmolViewer.runScript("halos on");
	    		    	for(int piece : frag){
		    		    	jmolViewer.runScript("select atomno="+(piece+1)+"; halos on; color halos gold;");
		    		    	
	    		    	}
	    		    	//jmolViewer.runScript("select atomno="+1+"; color lime; select atomno="+2+"; color lime;");
	    		    	
	    		    } else {
	    		    	for(int piece : frag){
		    		    	jmolViewer.runScript("select atomno="+(piece+1)+"; halos off;");
		    		    	
	    		    	}
	    		    }
	    		    
	    		    i++;
	    		}
	    		jmolPanel.repaint();
	    		
	    	}
	    });	
	}
	
	private static	LinkedList<Integer> [] buildJmolAdjacencyList() {
		int size = jmolViewer.ms.at.length;
		 // define the size of array as  
       // number of vertices 
       LinkedList<Integer> adjListArray[] = new LinkedList[size]; 
         
       // Create a new list for each vertex 
       // such that adjacent nodes can be stored 
       for(int i = 0; i < size ; i++){ 
           adjListArray[i] = new LinkedList<Integer>(); 
       } 

		for(int i = 0; i < jmolViewer.ms.bo.length; i++)
       { 
           //int n = i.next(); 
           //if (!visited[n]) 
            //   DFSUtil(n, visited);
       	if(jmolViewer.ms.bo[i] != null){
       		System.out.println("jmolViewer.ms.bo[i]: " + jmolViewer.ms.bo[i]);
       		int atom1 = jmolViewer.ms.bo[i].getAtomIndex1();
				int atom2 = jmolViewer.ms.bo[i].getAtomIndex2();
               adjListArray[atom1].add(atom2);
               adjListArray[atom2].add(atom1); 
       	} 
       } 
		
		//print graph adjlist
		for(int i = 0; i < size; i++){
			System.out.println("Adjacency list of vertex "+ (i+1)); 
           System.out.print("head"); 
           for(Integer pCrawl: adjListArray[i]){ 
               System.out.print(" -> "+(pCrawl+1)); 
           } 
           System.out.println("\n"); 
		}
		return adjListArray;
	}
	
	private static ArrayList<Integer> tmp_frag_list;
	
	// A function used by DFS 
    static 
	void DFSUtil(int v,boolean visited[]) 
    { 
        // Mark the current node as visited and print it 
        visited[v] = true; 
        System.out.print(v+" "); 
       // ArrayList<Integer> sub_frag_list = new ArrayList<Integer>();
    	//fragment_list.add(sub_frag_list);
        // Recur for all the vertices adjacent to this vertex 
        Iterator<Integer> i = adj[v].listIterator(); 
        while (i.hasNext()) 
        {      	
            int n = i.next(); 
            if (!visited[n]) {
            	
            	tmp_frag_list.add(n);
                DFSUtil(n,visited);
            }
        } 
        
    } 
  
    static // The function to do DFS traversal. It uses recursive DFSUtil() 
    void DFS() 
    { 
    	//ArrayList<ArrayList> fragment_list = new ArrayList<ArrayList>();
    	fragment_list.clear();
        // Mark all the vertices as not visited(set as 
        // false by default in java) 
        boolean visited[] = new boolean[adj.length]; 
        
        // Call the recursive helper function to print DFS traversal 
        // starting from all vertices one by one 
        for (int i=0; i< adj.length; ++i) {
        		
            if (visited[i] == false){
            	tmp_frag_list = new ArrayList<Integer>();
            	tmp_frag_list.add(i);
            	fragment_list.add(tmp_frag_list);
            	System.out.println("framgnet");
                DFSUtil(i, visited); 
            }
        }
    } 
    
    public static void displayFragments(JmolPanel jmolPanel){
    	jmolPanel.viewer.runScript("selectionHalos off");
	    jmolPanel.viewer.clearSelection();
     	jmolPanel.repaint();
	
     	adj = JmolVisualizer.buildJmolAdjacencyList();
     	fragment_list = new ArrayList<ArrayList<Integer>>();
     	DFS();

     	loadFragmentList();
    }

}
