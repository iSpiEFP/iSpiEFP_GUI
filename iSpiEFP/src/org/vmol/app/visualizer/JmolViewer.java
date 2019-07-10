package org.vmol.app.visualizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.jmol.util.Logger;
import org.jmol.viewer.Viewer;
import org.vmol.app.Main.JmolPanel;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;

public class JmolViewer {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public Viewer viewer;
    protected JmolPanel2 jmolPanel;
    protected final SwingNode swingNode;
    protected Pane parentPane;
    
    protected int currentWidth;
    protected int currentHeight;
    
    public JmolViewer(Pane pane) {
        JmolPanel2 panel = new JmolPanel2(pane);
        this.jmolPanel = panel;
        this.viewer = panel.viewer;
        this.parentPane = pane;
        this.swingNode = new SwingNode();        
        this.currentWidth = (int) pane.getWidth();
        this.currentHeight = (int) pane.getHeight();
        
        currentWidth = 940;
        currentHeight = 595;
        
        parentPane.getChildren().add(swingNode);
        jmolPanel.setPreferredSize(new Dimension(currentWidth, currentHeight));

        showJmolViewer();
    }
    
    public JmolPanel2 getJmolPanel() {
        return this.jmolPanel;
    }
    
    public SwingNode getSwingNode() {
        return this.swingNode;
    }
    
    public Pane getParentPane() {
        return this.parentPane;
    }
    
    /**
     * Set the pane for the viewer to be placed
     */
    public void setParentPane(Pane pane) {        
        pane.getChildren().add(swingNode);
    }
    
    private void showJmolViewer() {  
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
}
