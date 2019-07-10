package org.vmol.app.visualizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.viewer.Viewer;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.layout.Pane;

public class JmolPanel2 extends JPanel {
    /**
     * Generate Serial UID Version
     */
    private static final long serialVersionUID = 2183410255641936515L;
    
    public Viewer viewer;
    protected Pane parentPane;
    
    protected final SwingNode swingNode = new SwingNode();
    protected Dimension dimension = new Dimension(940, 595);

    public int test = 0;
   
    public JmolPanel2(Pane pane) {
        viewer = (Viewer) Viewer.allocateViewer(this, new SmarterJmolAdapter(),
                null, null, null, null, null);
        viewer.setAnimationFps(60);
        
        parentPane = pane;
        System.out.println("id:"+pane.getId());
        parentPane.getChildren().add(swingNode);
        this.setPreferredSize(dimension);
        runJmolViewer();
    }
    
    @Override
    public void paint(Graphics g) {
        getSize(new Dimension(dimension));

        viewer.renderScreenImage(g, dimension.width, dimension.height);
        System.out.println(test);
        System.out.println("hello i am jmol panel");
        test++;
    }
    
    public JmolPanel2 getJmolPanel() {
        return this;
    }
    
    public SwingNode getSwingNode() {
        return this.swingNode;
    }
    
    public Pane getParentPane() {
        return this.parentPane;
    }
    
    public Dimension getDimension() {
        return dimension;
    }
    
    public void setDimension(int width, int height) {
        dimension = new Dimension(width, height);
        this.setPreferredSize(dimension);
        this.repaint();
    }
    
    /**
     * Set the pane for the viewer to be placed
     */
    public void setParentPane(Pane pane) {
        this.parentPane = pane;
        parentPane.getChildren().add(swingNode);
    }
    
    private void runJmolViewer() {  
        JmolPanel2 jmolPanel = this;
        
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
