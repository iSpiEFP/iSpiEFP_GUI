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
 */
public class JmolMainPanel extends JmolPanel2 {
    /**
     * Generated Serial UID Version
     */
    private static final long serialVersionUID = -6927133884116203529L;

    /**
     * A Modified version of a JmolPanel specifically made for the main viewer panel for iSpiEFP
     * 
     * JmolMainPanel inherits: viewer, swingNode, parentPane, currentWidth, currentHeight
     * Viewer viewer : Jmol viewer object
     * SwingNode swingNode : JavaFX wrapper object for Java Swing Objects
     * Pane parentPane : Component that holds the viewer object 
     */
    public JmolMainPanel(Pane pane) {
        super(pane);
        initJmol();
    }
    
    @Override
    /**
     * JPanel Paint function overrode to accommodate the tracing of bonds, fragments and their components
     */
    public void paint(Graphics g) {
        getSize(dimension);

        viewer.renderScreenImage(g, dimension.width, dimension.height);
        System.out.println(test);
        System.out.println("hello i am main panel");
        test++;
    }
    
    /**
     * Run Jmol JavaScript Commands for a default Main Panel Set-up
     */
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
