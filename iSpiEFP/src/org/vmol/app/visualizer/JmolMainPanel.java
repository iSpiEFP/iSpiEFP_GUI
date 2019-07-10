package org.vmol.app.visualizer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import org.jmol.modelset.Bond;
import org.jmol.util.Logger;
import org.jmol.viewer.Viewer;
import org.vmol.app.Main;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;

/*
 * The main panel jmol viewer for iSpiEFP.
 * This viewer object wraps jmols swing object and provides
 * some additional features such as monitoring deleted bonds.
 * 
 * It is a special purpose JmolPanel and should only be used for the Main Panel.
 */
public class JmolMainPanel extends JmolPanel2 {
    /**
     * Generated Serial UID Version
     */
    private static final long serialVersionUID = -6927133884116203529L;

    private Stack<ArrayList<ArrayList<Integer>>> fragmentListHistory = new Stack<ArrayList<ArrayList<Integer>>>();
    
    /**
     * A Modified version of a JmolPanel specifically made for the main viewer panel for iSpiEFP.
     * Initializes the JmolPanel, additionally calling some default init Jmol scripts
     * 
     * JmolMainPanel inherits: viewer, swingNode, parentPane, currentWidth, currentHeight from JmolPanel
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
        System.out.println("hello i am main panel");
        
        if(getFragmentComponents() != null) {
            //update listView
        }
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
    
    /**
     * A Safer way of opening a file than "viewer.openFile()".
     * JmolMainPanel version is identical except that valid file IO also calls initial fragment list construction. 
     * @throws IOException 
     */
    @Override
    public void openFile(File file) throws IOException {
        if(file == null) {
            System.err.println("Jmol Viewer IO error: reading a null file.");
            return;
        }
        
        String fileName = file.getName();
        String strError = new String();
        if (fileName.contains("xyz") || fileName.contains("pdb")) {
            if ((strError = viewer.openFile(file.getAbsolutePath())) != null) {
                Logger.error("Error while loading XYZ file. " + strError);
                return;
            }
            getFragmentComponents();
            
        } else {
            openFileParserWindow(file);
        }
    }
    
    /**
     * Find each fragment component, and store as a current and historical record.
     * This functions calls buildBondAdjacencyList() for utility, and then traverses the adjacency list
     * with a Depth first search to find fragment components.
     */
    private ArrayList<ArrayList<Integer>> getFragmentComponents() {
        int atomCount = viewer.ms.at.length;
        if(atomCount == 0) {
            return null;
        }
        //TODO: check if adjList Changed at all
        ArrayList<ArrayList<Integer>> bondAdjList = buildBondAdjacencyList(atomCount);
        ArrayList<ArrayList<Integer>> fragmentList = depthFirstSearch(bondAdjList, atomCount);
        fragmentListHistory.push(fragmentList);
        
        return fragmentList;
    }
    
    /**
     * Depth First Search to aide in finding molecule components
     * Input: Adjacency list of bonds
     * Output: Depth First Search Traversal List of lists
     * 
     * ArrayList<Integer> component serves as a special purpose utility variable for DFS
     */
    private ArrayList<Integer> component;
    private ArrayList<ArrayList<Integer>> depthFirstSearch(ArrayList<ArrayList<Integer>> adj, int n) {
        ArrayList<ArrayList<Integer>> fragmentList = new ArrayList<ArrayList<Integer>>();
        
        // Mark all the vertices as not visited(set as false by default in java)
        boolean[] visited = new boolean[n];

        // Call the recursive helper function to print DFS traversal
        // starting from all vertices one by one
        for (int i = 0; i < n; ++i) {
            if (visited[i] == false) {
                component = new ArrayList<Integer>();
                System.out.println("fragment:");
                depthFirstSearchUtil(i, adj, visited);
                fragmentList.add(component);
            }
            System.out.println();
        }
        return fragmentList;
    }
    
    /**
     * Utility function for DepthFirstSearch
     * @param v : vertex
     * @param adj : adjacency list of bonds
     * @param visited : list of visited vertices
     */
    private void depthFirstSearchUtil(int v, ArrayList<ArrayList<Integer>> adj, boolean[] visited) {
        // Mark the current node as visited and print it
        visited[v] = true;
        component.add(v);
        System.out.print(v+" ");

        // Recur for all the vertices adjacent to this vertex
        Iterator<Integer> i = adj.get(v).listIterator();
        while (i.hasNext()) {
            int n = i.next();
            if (!visited[n]) {
                depthFirstSearchUtil(n, adj, visited);
            }
        }
    }
    
    /**
     * Build an adjacency List from the jmol viewer bond list
     * 
     * @return ArrayList<ArrayList<Integer>> bondAdjList
     */
    private ArrayList<ArrayList<Integer>> buildBondAdjacencyList(int n) {
        ArrayList<ArrayList<Integer>> bondAdjList = new ArrayList<ArrayList<Integer>>();
        Bond[] bonds = viewer.ms.bo;

        for (int i = 0; i < n; i++) {
            bondAdjList.add(new ArrayList<Integer>());
        }
        for (int i = 0; i < bonds.length; i++) {
            int atomNo1 = bonds[i].getAtomIndex1();
            int atomNo2 = bonds[i].getAtomIndex2();
            bondAdjList.get(atomNo1).add(atomNo2);
            bondAdjList.get(atomNo2).add(atomNo1);
        }
        return bondAdjList;
    }
       
}
