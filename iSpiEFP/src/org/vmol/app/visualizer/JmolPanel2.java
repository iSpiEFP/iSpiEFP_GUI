package org.vmol.app.visualizer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.viewer.Viewer;

public class JmolPanel2 extends JPanel {

    /**
     * Default Serial ID 
     */
    private static final long serialVersionUID = 1L;
    
    public Viewer viewer;
   
    public JmolPanel2() {
        viewer = (Viewer) Viewer.allocateViewer(this, new SmarterJmolAdapter(),
                null, null, null, null, null);
        viewer.setAnimationFps(60);
    }
}
