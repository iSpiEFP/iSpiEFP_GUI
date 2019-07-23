package org.ispiefp.app.visualizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

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
    
    protected final SwingNode2 swingNode = new SwingNode2();      //java swing node wrapper for javaFX
    protected Dimension dimension = new Dimension(940, 595);    //JmolPanel dimensions

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
        viewer = (Viewer) Viewer.allocateViewer(this, new SmarterJmolAdapter(),
                null, null, null, null, null);
        viewer.setAnimationFps(60);
        
        //place 
        this.parentPane = pane;
        pane.getChildren().add(swingNode);

        this.width = pane.getWidth();
        this.height = pane.getHeight();

        //set JPanel initial size
        this.setPreferredSize(new Dimension((int)width, (int)height));

        //set SwingNode initial size
        this.swingNode.resize(width, height);
        
        //run on thread
        runJmolViewer();
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
     * @return the dimensions of this JmolPanel
     */
    public Dimension getDimension() {
        return dimension;
    }
    
    /**
     * Set and update the dimensions of this JmolPanel
     * 
     * @param width : JmolPanel
     * @param height : JmolPanel
     */
    public void setDimension(int width, int height) {
        dimension = new Dimension(width, height);
        this.setPreferredSize(dimension);
        this.setSize(dimension);
        this.repaint();
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
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("fileparser/FileParser.fxml"));
        Parent fileParser = loader.load();
        Parent root = new StackPane();

        Stage stage = new Stage();
        stage.setTitle(file.getName());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(root.getScene().getWindow());
        stage.setScene(new Scene(fileParser));

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
        Dimension size = getSize();

        viewer.renderScreenImage(g, size.width, size.height);
    }
}
