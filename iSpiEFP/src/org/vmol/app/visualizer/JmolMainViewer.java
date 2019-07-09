package org.vmol.app.visualizer;

import java.awt.Dimension;

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
    
    /**
     * JmolMainViewer inherits: viewer, jmolPanel
     * Viewer viewer : jmol viewer object
     * JmolPanel jmolPanel : jmol panel that holds viewer
     */
    public JmolMainViewer() {
        super();
        
        //get default parentPane location
        SplitPane splitpane = (SplitPane) Main.getMainLayout().getChildren().get(2);
        ObservableList<Node> list = splitpane.getItems();
        SplitPane nodepane = (SplitPane) list.get(1);
        ObservableList<Node> sublist = nodepane.getItems();
        Pane pane = (Pane) sublist.get(0);
        
        //embed jmol
        pane.getChildren().add(swingNode);
        
        jmolPanel.setPreferredSize(new Dimension(69,69));
    }
    
    public void setParentPane(Pane pane) {        
        pane.getChildren().add(swingNode);
    }
    
    /**
     * Set the pane for the main viewer to be placed
     */
    
    
    protected void setSize() {
        //if (mainPanel) {
        //    splitpane.setDividerPositions(0.2f, 0.3f);
          //  nodepane.setDividerPositions(1, 0);
           /*
        //    Pane pane = (Pane) sublist.get(0);
            jmolPanel.setPreferredSize(new Dimension((int)nodepane.getWidth(), (int)nodepane.getHeight()));
            System.out.println("nodepane width:"+pane.getWidth());
            System.out.println("nodepane height:"+pane.getHeight());
          //  pane.getChildren().add(swingNode);
            */
            //jmolPanel.setPreferredSize(new Dimension(69, 69));
            
    }

}
