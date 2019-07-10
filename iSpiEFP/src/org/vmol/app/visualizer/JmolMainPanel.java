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
 * Example Usage: JmolMainPanel mainJmolViewer = new JmolMainPanel();
 */
public class JmolMainPanel extends JmolPanel2 {
    //Generate generic serial ID 
    private static final long serialVersionUID = 1L;
    
    //inherits viewer, jmolPanel, swingNode, parentPane, currentWidth, currentHeight
    
    /**
     * JmolMainPanel inherits: viewer, jmolPanel
     * Viewer viewer : jmol viewer object
     * JmolPanel2 jmolPanel : jmol panel that holds viewer
     */
    public JmolMainPanel(Pane pane) {
        super(pane);
        //jmolPanel.setPreferredSize(new Dimension(currentWidth, currentHeight));
        initJmol();
    }
    
    @Override
    public void paint(Graphics g) {
        getSize(new Dimension(currentWidth, currentHeight));

        viewer.renderScreenImage(g, currentWidth, currentHeight);
        System.out.println(test);
        System.out.println("hello i am main panel");
        test++;
    }
    
    private void initJmol() {
        //default settings
        viewer.runScript("select clear");
        viewer.clearSelection();
        viewer.runScript("set pickingstyle SELECT DRAG");
        viewer.runScript("set picking atom");
        viewer.runScript("animation fps 10");
        viewer.runScript("selectionHalos off");
        viewer.runScript("set bondpicking true");
        viewer.runScript("color halos gold");
    }
}
