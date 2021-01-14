/*
 *     iSpiEFP is an open source workflow optimization program for chemical simulation which provides an interactive GUI and interfaces with the existing libraries GAMESS and LibEFP.
 *     Copyright (C) 2021  Lyudmila V. Slipchenko
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please direct all questions regarding iSpiEFP to Lyudmila V. Slipchenko (lslipche@purdue.edu)
 */

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
