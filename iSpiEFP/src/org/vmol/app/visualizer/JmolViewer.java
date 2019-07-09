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

public abstract class JmolViewer extends JmolPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Viewer viewer;
    
    protected JmolPanel jmolPanel;
    
    public JmolViewer() {
        JmolPanel panel = new JmolPanel();
        this.viewer = panel.viewer;
        this.jmolPanel = panel;
        
        showJmolViewer();
    }
    
    private void showJmolViewer() {  
        //init jmolPanel
            final SwingNode swingNode = new SwingNode();

            Platform.runLater(new Runnable() {
                private Component jmolPanel;

                @Override
                public void run() {
                    //jmolPanel.setPreferredSize(new Dimension(940, 595));
                    // main panel -- Jmol panel on top
                    JPanel panel = new JPanel();
                    panel.setLayout(new BorderLayout());
                    panel.add("North", this.jmolPanel);


                    panel.setFocusable(true);
                    swingNode.setContent(panel);
                }
            });
    }
    
    protected abstract void setSize();
}
