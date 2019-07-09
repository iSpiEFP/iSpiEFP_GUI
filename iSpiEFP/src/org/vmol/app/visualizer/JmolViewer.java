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

public abstract class JmolViewer extends JmolPanel2 {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    protected JmolPanel2 jmolPanel;
    
    protected final SwingNode swingNode;
    
    public JmolViewer() {
        this.swingNode = new SwingNode();
        
        showJmolViewer();
    }
    
    public JmolPanel2 getJmolPanel() {
        return this.jmolPanel;
    }
    
    public SwingNode getSwingNode() {
        return this.swingNode;
    }
    
    private void showJmolViewer() {  
        //init jmolPanel
            //final SwingNode swingNode = new SwingNode();
            JmolPanel2 panel = new JmolPanel2();
            //this.viewer = panel.viewer;
            this.jmolPanel = panel;
            
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    //jmolPanel.setPreferredSize(new Dimension(940, 595));
                    // main panel -- Jmol panel on top
                    JPanel panel = new JPanel();
                    panel.setLayout(new BorderLayout());
                    panel.add("North", jmolPanel);


                    panel.setFocusable(true);
                    swingNode.setContent(panel);
                }
            });
    }
    
    abstract public void setParentPane(Pane pane);
}
