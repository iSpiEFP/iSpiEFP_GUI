package org.vmol.app.visualizer;

import java.awt.Dimension;

import org.jmol.viewer.Viewer;

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
        setSize();
    }
    
    protected void setSize() {
        jmolPanel.setPreferredSize(new Dimension(69, 69));
    }
}
