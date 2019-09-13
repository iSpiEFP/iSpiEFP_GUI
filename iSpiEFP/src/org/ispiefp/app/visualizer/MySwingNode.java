package org.ispiefp.app.visualizer;

import javafx.embed.swing.SwingNode;

/**
 * The exact same thing as SwingNode except overrides the class 'isResizable' to return false.
 * This allows the swingNode to manually resize rather than be resized by default by the JPanel
 *
 * From StackOverFlow: https://stackoverflow.com/questions/20350890/how-to-resize-swing-control-which-is-inside-swingnode-in-javafx8/23642899#23642899
 *
 * Basically, the problem is that the parent of the SwingNode is trying to set its size when layout occurs,
 * based on the size of the parent. So when you resize your button, and then trigger a layout, the parent of the
 * SwingNode sets it back to its default size. This is occurring because SwingNode overrides the isResizable()
 * method to return true, giving permission to its parent objects to resize it.
 */
public class MySwingNode extends SwingNode {

    @Override
    /**
     * Will always return false
     */
    public boolean isResizable() {
        return false;
    }
}
