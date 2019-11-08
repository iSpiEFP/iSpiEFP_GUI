package org.ispiefp.app.visualizer;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.util.Logger;
import org.jmol.viewer.Viewer;
import org.ispiefp.app.fileparser.FileParserController;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * JmolPanel holds the Jmol Viewer Object for the Jmol.jar, wraps it to work
 * with JavaFX, and runs the pane. 
 * 
 * Example Usage: Given a Pane pane, which is a desired pane for the Jmol Viewer to be,
 * simply call: JmolPanel jmolPanel = new JmolPanel(pane);
 * 
 * To use Jmol options use the "viewer" class member. 
 * Example Usage: For accessing the Jmol modelSet, "jmolPanel.viewer.ms"
 * 
 * @author addison
 *
 */
public class JmolPanel extends JPanel {
    /**
     * Generate Serial Version UID Version
     */
    private static final long serialVersionUID = 2183410255641936515L;
    
    public Viewer viewer;       //jmol Viewer object
    protected Pane parentPane;  //pane which contains jmol viewer object
    
    protected final MySwingNode swingNode = new MySwingNode();      //java swing node wrapper for javaFX

    protected double width;
    protected double height;

    /**
     * Allocate a Jmol Viewer Object, 
     * place it in the pane location, 
     * set the default size to the pane dimensions,
     * and run the Jmol Viewer Object on a thread.
     * 
     * @param pane : container for Jmol Viewer Object
     */
    public JmolPanel(Pane pane) {
        //allocate a Jmol Viewer
        viewer = (Viewer) Viewer.allocateViewer(this, new SmarterJmolAdapter(),
                null, null, null, null, null);
        viewer.setAnimationFps(60);

        //place 
        this.parentPane = pane;
        this.width = pane.getWidth();
        this.height = pane.getHeight();

        //set JPanel initial size
        this.setPreferredSize(new Dimension((int)width, (int)height));

        //set SwingNode initial size
        this.swingNode.resize(width, height);
        pane.getChildren().add(swingNode);

        //add height listener to change size during height change
        pane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldPaneHeight, Number newPaneHeight) {
                //height changed
                height = newPaneHeight.doubleValue();
                updateJmolSize(width, height);
            }
        });
        //add width listener to change size during width change
        pane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldPaneWidth, Number newPaneWidth) {
                //width changed
                width = newPaneWidth.doubleValue();
                updateJmolSize(width, height);
            }
        });
        
        //run on thread
        runJmolViewer();
    }

    private void updateJPanelSize() {
        this.setPreferredSize(new Dimension((int) width, (int) height));

    }

    private void updateSwingNodeSize() {
        this.swingNode.resize(width, height);
        this.repaint();

    }

    private void updateJmolSize(double width, double height) {
        this.setPreferredSize(new Dimension((int) width, (int) height));
        this.swingNode.resize(width, height);
        this.repaint();
    }
    
    /**
     * @return this JmolPanel Object
     */
    public JmolPanel getJmolPanel() {
        return this;
    }
    
    /**
     * @return this swingNode
     */
    public SwingNode getSwingNode() {
        return this.swingNode;
    }
    
    /**
     * @return the Parent Pane holding the Jmol Viewer
     */
    public Pane getParentPane() {
        return this.parentPane;
    }
    
    /**
     * Set the pane for the Jmol viewer to be placed
     */
    public void setParentPane(Pane pane) {
        this.parentPane = pane;
        parentPane.getChildren().add(swingNode);
    }
    
    /**
     * A Safer way of opening a file than "viewer.openFile()"
     * @throws IOException 
     */
    public boolean openFile(File file) throws IOException {
        if(file == null) {
            System.err.println("Jmol Viewer IO error: reading a null file.");
            return false;
        }
        
        String fileName = file.getName();
        String strError = new String();
        if (fileName.contains("xyz") || fileName.contains("pdb")) {
            if ((strError = viewer.openFile(file.getAbsolutePath())) != null) {
                Logger.error("Error while loading XYZ file. " + strError);
                return false;
            }
            return true;
        } else {
            openFileParserWindow(file);
        }
        return false;
    }

    /**
     * Open the file parser window if a file fails
     *
     * @param file
     * @throws IOException
     */
    protected void openFileParserWindow(File file) throws IOException {
        System.out.println("OPEN FILEPARSER CALLED");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/FileParser.fxml"));
//        loader.setLocation(getClass().getResource("/Users/shaadhussain/Desktop/NewiSpiEFP/iSpiEFP_GUI/iSpiEFP/resources/views/FileParser.fxml"));
        Parent fileParser = loader.load();
        Parent root = new StackPane();

        Stage stage = new Stage();
        stage.setTitle(file.getName());
        stage.initModality(Modality.WINDOW_MODAL);
        System.out.println("Root" + root);
        System.out.println("Scene" + root.getScene());
        stage.setScene(new Scene(fileParser));
        stage.initOwner(root.getScene().getWindow());
//        stage.setScene(new Scene(fileParser));

        // Set the file into the controller
        FileParserController controller = loader.getController();
        controller.setFile(file);

        stage.showAndWait();
    }
    
    /**
     * Run the Jmol Viewer Object on a separate thread
     */
    private void runJmolViewer() {  
        JmolPanel jmolPanel = this;
        
        Platform.runLater(new Runnable() {
            @Override
            public void run(){
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                panel.add("North", jmolPanel);
                panel.setFocusable(true);
                swingNode.setContent(panel);
            }
        });
    }
    
    /**
     * Override JPanel Paint to also render Jmol Viewer Object
     * 
     * @param g : Graphics Param from JPanel
     */
    @Override
    public void paint(Graphics g) {
        //render Jmol Viewer with these dimensions
        viewer.renderScreenImage(g, (int)width, (int)height);
    }
}
