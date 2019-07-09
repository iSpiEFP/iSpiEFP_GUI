package org.vmol.app.visualizer;

import java.awt.Dimension;

import org.jmol.viewer.Viewer;

public class MainJmolViewer extends JmolViewer {
    /**
     * Generate generic serial ID 
     */
    private static final long serialVersionUID = 1L;

    /**
     * MainJmolViewer inherits: viewer, jmolPanel
     * Viewer viewer : jmol viewer object
     * JmolPanel jmolPanel : jmol panel that holds viewer
     */
    public MainJmolViewer() {
        super();
        setSize();
    }
    
    protected void setSize() {
        jmolPanel.setPreferredSize(new Dimension(69, 69));
    }
}
