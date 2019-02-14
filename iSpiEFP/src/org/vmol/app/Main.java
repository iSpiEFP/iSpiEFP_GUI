package org.vmol.app;
	
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JPanel;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolSelectionListener;
import org.jmol.api.JmolViewer;
import org.jmol.java.BS;
import org.jmol.util.Logger;
import org.jmol.viewer.Viewer;
import org.openscience.jmol.app.jmolpanel.console.AppConsole;
import org.vmol.app.installer.BundleManager;
import org.vmol.app.visualizer.JmolVisualizer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Main extends Application {
	
	private static Stage primaryStage;
	private static BorderPane mainLayout;
	public static int globalnum;
	public static JmolPanel jmolPanel;
	public static JmolPanel auxiliaryJmolPanel;
	
	//iSpiEFP Server Credentials
	/*
	 * Here are the server credentials to the iSpiEFP server
	 * Development Server: Server where developing takes place for development
	 * Test Server: Server constantly running for current iSpiEFP(jar file release) for real users
	 */
	
	public static final String iSpiEFP_SERVER = "ec2-18-220-105-41.us-east-2.compute.amazonaws.com"; //This is the Development Server
	//public static final String iSpiEFP_SERVER = "ec2-3-16-11-177.us-east-2.compute.amazonaws.com"; //This is the Test Server
	public static final int iSpiEFP_PORT = 8080;
	        
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		Main.setPrimaryStage(primaryStage);
		Main.getPrimaryStage().setTitle("iSpiEFP");
		showMainView();
		showJmolViewer(true, null);
		
		//optional alert for jar test files
		Alert alert = new Alert(AlertType.WARNING);
		String msg = "Welcome to iSpiEFP\n\n"
		         + "I acknowledge that this is a Pre-Alpha Release which means my jobs and data can be damaged or lost.\n"
	             + "My credentials will be safe however.\n"
	             + "This application will install a directory: '/iSpiClient' on a remote machine and\n"
                 + "a working directory on the local machine in the app's current directory: '/iSpiWorkspace for storing data'\n\n"
	             + "This product is new and will have lots of bugs. There are no loading bars so please wait\n"
	             + "a few seconds while submitting jobs, searching for parameters, and configuring servers.\n"
	             + "If I encounter a freeze or serious issue I will shake the app or restart it.\n"
	             + "If I encounter an issue I will report it.\n";
        alert.setTitle("Terms of Agreement");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        Optional<ButtonType> result = alert.showAndWait();
		
	}

	private void showMainView() throws IOException {
		setMainLayout(FXMLLoader.load(getClass().getResource("MainView.fxml")));
	
		getMainLayout().setScaleShape(true);
		getPrimaryStage().setScene(new Scene(getMainLayout()));
		getPrimaryStage().setOnCloseRequest(new EventHandler<javafx.stage.WindowEvent>() {
	          public void handle(javafx.stage.WindowEvent we) {
	              System.out.println("Stage is closing");
	              System.exit(0);
	          }
	      });
		getPrimaryStage().setResizable(false);
		//primaryStage.initModality(Modality.WINDOW_MODAL);
		getPrimaryStage().setAlwaysOnTop(false);
		getPrimaryStage().setHeight(700);
		getPrimaryStage().setWidth(1200);
		
		//add iSpiEFP Spider icon
	    String url = Main.class.getResource("/images/iSpiEFP_Logo.png").toString();
		getPrimaryStage().getIcons().add(new Image(url));
		
		//load buttons
        initializeButtonIcons();
		
		//Manage Working Directory
		BundleManager bundleManager = new BundleManager("LOCAL");
		bundleManager.manageLocal();
		
		//launch stage
		getPrimaryStage().show();
	}
	
	private void initializeButtonIcons(){
	    Image halo = new Image(Main.class.getResource("/images/halo.png").toString());
 		Image scissors = new Image(Main.class.getResource("/images/scissors.png").toString());
 		Image play = new Image(Main.class.getResource("/images/play.png").toString());
 		Image terminal = new Image(Main.class.getResource("/images/terminal.png").toString());
 		
 		//get button list
 		//SplitPane splitpane = (SplitPane) Main.getMainLayout().getChildren().get(2);
 		VBox vbox = (VBox) Main.getMainLayout().getChildren().get(0);
 		Pane buttonBar = (Pane)vbox.getChildren().get(1);
 		
		ObservableList<Node> buttonList = buttonBar.getChildren();
		
 		ToggleButton button_halo_on = (ToggleButton) buttonList.get(0);
 		ToggleButton button_fragment = (ToggleButton) buttonList.get(1);
 		ToggleButton button_play_pause = (ToggleButton) buttonList.get(3);
 		Button button_show_console = (Button) buttonList.get(4);
 		Button button_submit = (Button) buttonList.get(5);
        Button button_libefp = (Button) buttonList.get(6);
        button_submit.setDisable(true);
        button_libefp.setDisable(true);
 		
 		//set graphics
 		button_halo_on.setText("");	
		button_halo_on.setGraphic(new ImageView(halo));
		button_fragment.setText("");	
		button_fragment.setGraphic(new ImageView(scissors));
 		button_play_pause.setText("");	
		button_play_pause.setGraphic(new ImageView(play));
		button_show_console.setText("");	
		button_show_console.setGraphic(new ImageView(terminal));
		
	}
	
	public static void showJmolViewer(boolean mainPanel, String filename) {
		final SwingNode swingNode = new SwingNode();
		
		createAndSetSwingContent(swingNode, filename, mainPanel);

		System.out.println(getMainLayout().getChildren().get(2).getClass());
		//Pane pane = (Pane) mainLayout.getChildren().get(2);
		SplitPane splitpane = (SplitPane) getMainLayout().getChildren().get(2);
		//splitpane.getItems().add(swingNode);
		//get items 1 and 2 from main split pane
		ObservableList<Node> list = splitpane.getItems();
		
		//get item 2 from main split pane
		SplitPane nodepane = (SplitPane) list.get(1);
		//add swingnode to left split pane
		ObservableList<Node> sublist = nodepane.getItems();
		
		if(mainPanel){
			splitpane.setDividerPositions(0.2f, 0.3f);
			nodepane.setDividerPositions(1, 0);
		
			Pane pane = (Pane) sublist.get(0);
			pane.getChildren().add(swingNode);
		} else {
			nodepane.setDividerPositions(0.6f, 0.4f);
			SplitPane vertSplit = (SplitPane) sublist.get(1);
			ObservableList<Node> vertlist = vertSplit.getItems();
			Pane pane = (Pane) vertlist.get(0);
			pane.getChildren().add(swingNode);
		}
		
		//updateSwingContent(swingNode, "file:test.pdb");

	}
	
    private static void createAndSetSwingContent(final SwingNode swingNode, String fileName, boolean mainPanel) {
    	//init jmolPanel
    	if(mainPanel){
        	jmolPanel = new JmolPanel();
        	
        	Platform.runLater(new Runnable() {
                @Override
                public void run() {
            	    
            	    jmolPanel.setPreferredSize(new Dimension(940, 595));
            	    //jmolPanel.setPreferredSize(new Dimension(100, 100));
    
            	    // main panel -- Jmol panel on top
    
            	    JPanel panel = new JPanel();
            	    panel.setLayout(new BorderLayout());
            	    panel.add("North", jmolPanel);
            	    
            	    
            	    getMainLayout().setVisible(true);
            	    //frame.setVisible(true);
            	    String strError = null;
            	    if (fileName != null && !fileName.isEmpty())
            	    	strError = jmolPanel.viewer.openFile(fileName);
            	    //jmolPanel.viewer.openStringInline(strXyzHOH);
    //        	    jmolPanel.viewer.openFileAsync("benzene.xyz");
            	    if (strError != null)
            	      Logger.error(strError);
    
            	    panel.setFocusable(true);
                    swingNode.setContent(panel);  
                    
                }
            });
    	} else {
    		auxiliaryJmolPanel = new JmolPanel();
        	
        	Platform.runLater(new Runnable() {
                @Override
                public void run() {
            	    
            	    auxiliaryJmolPanel.setPreferredSize(new Dimension(400, 300));
            	    //jmolPanel.setPreferredSize(new Dimension(100, 100));

            	    // main panel -- Jmol panel on top

            	    JPanel panel = new JPanel();
            	    panel.setLayout(new BorderLayout());
            	    panel.add("North", auxiliaryJmolPanel);
            	    
            	    
            	    getMainLayout().setVisible(true);
            	    //frame.setVisible(true);
            	    String strError = null;
            	    if (fileName != null && !fileName.isEmpty())
            	    	strError = auxiliaryJmolPanel.viewer.openFile(fileName);
            	    //jmolPanel.viewer.openStringInline(strXyzHOH);
//            	    jmolPanel.viewer.openFileAsync("benzene.xyz");
            	    if (strError != null)
            	      Logger.error(strError);

            	    panel.setFocusable(true);
                    swingNode.setContent(panel);  
                    
                }
            });	
    	}
    }
	  public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void setPrimaryStage(Stage primaryStage) {
		Main.primaryStage = primaryStage;
	}

	public static BorderPane getMainLayout() {
		return mainLayout;
	}

	public static JmolPanel getJmolViewer() {
		return Main.jmolPanel;
	}
	
	public static void setMainLayout(BorderPane mainLayout) {
		Main.mainLayout = mainLayout;
	}

	public static class JmolPanel extends JPanel {

	    /**
		 * 
		 */
		
		private static final long serialVersionUID = 1L;

		public Viewer viewer;
		public ArrayList<ArrayList> original_bonds = new ArrayList<ArrayList>();
		public ArrayList<ArrayList> deleted_bonds = new ArrayList<ArrayList>();

		//JmolViewer viewer;
	    
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
          ArrayList bond = JmolVisualizer.find_deleted_bonds(jmolPanel);
          if (bond != null) {
          	
          	System.out.println("bond between " + bond.get(0) + "  " + bond.get(1));
          	deleted_bonds.add(bond);
          	System.out.println("woah");
          	JmolVisualizer.displayFragments(jmolPanel);
          }
	    }
	  }
 

}
