package org.vmol.app.visualizer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import org.jmol.viewer.Viewer;
import org.vmol.app.Main;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;

/*
 * The main panel jmol viewer for iSpiEFP
 * This viewer object wraps jmols swing object and provides
 * some additional features such as monitoring deleted bonds
 * 
 * Example Usage: JmolMainViewer mainJmolViewer = new JmolMainViewer();
 */
public class JmolMainViewer extends JmolViewer {
    //Generate generic serial ID 
    private static final long serialVersionUID = 1L;
    
    public int currentWidth = 69;
    public int currentHeight = 69;
    
    /**
     * JmolMainViewer inherits: viewer, jmolPanel
     * Viewer viewer : jmol viewer object
     * JmolPanel2 jmolPanel : jmol panel that holds viewer
     */
    public JmolMainViewer() {
        super();
        
        //get default parentPane location
        SplitPane splitpane = (SplitPane) Main.getMainLayout().getChildren().get(2);
        ObservableList<Node> list = splitpane.getItems();
        SplitPane nodepane = (SplitPane) list.get(1);
        ObservableList<Node> sublist = nodepane.getItems();
        Pane pane = (Pane) sublist.get(0);
        
        //embed jmol to javaFX application
        setParentPane(pane);
        
        jmolPanel.setPreferredSize(new Dimension(69,69));
    }
    
    /**
     * Set the pane for the viewer to be placed
     */
    public void setParentPane(Pane pane) {        
        pane.getChildren().add(swingNode);
    }
    
    /**
     * Override Paint method to track fragment changes
     */
    @Override
    public void paint(Graphics g) {
        //getSize(currentSize);

        viewer.renderScreenImage(g, currentWidth, currentHeight);
        /*ArrayList bond = JmolVisualizer.find_deleted_bonds(jmolPanel);
        if (bond != null) {

            System.out.println("bond between " + bond.get(0) + "  " + bond.get(1));
            deleted_bonds.add(bond);
            System.out.println("woah");
            JmolVisualizer.displayFragments(jmolPanel);
        }*/
    }

}
