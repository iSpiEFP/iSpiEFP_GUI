package org.ispiefp.app.visualizer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import org.jmol.modelset.Bond;
import org.jmol.util.Logger;

import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;

/*
 * The main panel jmol viewer for iSpiEFP.
 * This viewer object wraps jmols swing object and provides
 * some additional features such as monitoring deleted bonds.
 * 
 * It is a special purpose JmolPanel and should only be used for the Main Panel.
 */
public class JmolMainPanel extends JmolPanel {
    /**
     * Generated Serial UID Version
     */
    private static final long serialVersionUID = -6927133884116203529L;
    
    private Stack<int[]> bondHistory = new Stack<int[]>();
    
    private HashMap<int[], Boolean> bondMap;
    
    ArrayList<ArrayList<Integer>> bondAdjList;
        
    private int bondCount = 0;
    
    private FragmentListView fragmentListView;
    
    private ListView<String> listView;
    
    /**
     * A Modified version of a JmolPanel specifically made for the main viewer panel for iSpiEFP.
     * Initializes the JmolPanel, additionally calling some default init Jmol scripts
     * 
     * JmolMainPanel inherits: viewer, swingNode, parentPane, width, height from JmolPanel
     * Viewer viewer : Jmol viewer object
     * SwingNode swingNode : JavaFX wrapper object for Java Swing Objects
     * Pane parentPane : Component that holds the viewer object 
     */
    public JmolMainPanel(Pane pane, ListView<String> listView) {
        super(pane);
        
        this.listView = listView;
        
        initJmol();
    }
    
    @Override
    /**
     * JPanel Paint function overrode to accommodate the tracing of bonds, fragments and their components
     */
    public void paint(Graphics g) {
        if(bondCount > viewer.ms.bondCount) {
            //a bond was deleted
            fragmentListView.update(getFragmentComponents());

            //record deletion history
            bondHistory.push(getDeletedBond());
            
            //update current bondCount
            bondCount = viewer.ms.bondCount;
        }
        //render Jmol Viewer with these dimensions
        viewer.renderScreenImage(g, (int)width, (int)height);
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

        //initialize utility containers
        bondMap = getBondMap();
        bondAdjList = getBondAdjacencyList(viewer.ms.at.length);
        bondCount = viewer.ms.bondCount;
        
        //initialize fragmentListView
        ArrayList<ArrayList<Integer>> fragmentList = getFragmentComponents();
        fragmentListView = new FragmentListView(listView, this);
        fragmentListView.update(fragmentList);
        
        this.repaint();
    }
    
    /**
     * Undo a delete bond operation on the Main Jmol Viewer, and update the visualizer
     * as well as update the fragment selection list.
     */
    public void undoDeleteBond() {        
        if(!bondHistory.isEmpty()) {
            int[] bond = bondHistory.pop();
            int atom1 = bond[0];
            int atom2 = bond[1];
            
            //update utility map
            bondMap.put(bond, true);
            
            //reconnect deleted bond in viewer
            String script = "connect (atomno=" + atom1 + ") (atomno=" + atom2 + ")";
            viewer.runScript(script);
            
            //update bondCount
            bondCount = viewer.ms.bondCount;
            
            //update fragment list view
            ArrayList<ArrayList<Integer>> fragmentList = getFragmentComponents();
            fragmentListView.update(fragmentList);
      
            //repaint viewer
            this.repaint();
        } 
    }
    
    /**
     * A Safer way of opening a file than "viewer.openFile()".
     * JmolMainPanel version is identical except that valid file IO also calls initial fragment list construction. 
     * @throws IOException 
     */
    @Override
    public boolean openFile(File file) throws IOException {
        if(file == null) {
            System.err.println("Jmol Viewer IO error: reading a null file.");
            return false;
        }
        String fileName = file.getName();
        String strError = new String();
        if (fileName.contains("xyz") || fileName.contains("pdb")) {
            if ((strError = viewer.openFile(file.getAbsolutePath())) != null) {
                Logger.error("Error while loading XYZ file. " + strError);
                return false;
            }
            initJmol();
            
            return true;
        } else {
            openFileParserWindow(file);
        }
        return false;
    }
    
    /**
     * Find each fragment component, and store as a current and historical record.
     * This functions calls buildBondAdjacencyList() for utility, and then traverses the adjacency list
     * with a Depth first search to find fragment components.
     */
    public ArrayList<ArrayList<Integer>> getFragmentComponents() {
        int atomCount = viewer.ms.at.length;
        if(atomCount == 0) {
            return null;
        }
        bondAdjList = getBondAdjacencyList(atomCount);
        ArrayList<ArrayList<Integer>> fragmentList = depthFirstSearch(bondAdjList, atomCount);        
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
    
    /*
     * Build an adjacency List from the jmol viewer bond list
     * input: int n, number of atoms in molecule viewer
     * 
     * @return ArrayList<ArrayList<Integer>> bondAdjList 
     */
    private ArrayList<ArrayList<Integer>> getBondAdjacencyList(int n) {
        ArrayList<ArrayList<Integer>> bondAdjList = new ArrayList<ArrayList<Integer>>();
        Bond[] bonds = viewer.ms.bo;

        for (int i = 0; i < n; i++) {
            bondAdjList.add(new ArrayList<Integer>());
        }
        for (int i = 0; i < viewer.ms.bondCount; i++) {
            int atomNo1 = bonds[i].getAtomIndex1();
            int atomNo2 = bonds[i].getAtomIndex2();
            bondAdjList.get(atomNo1).add(atomNo2);
            bondAdjList.get(atomNo2).add(atomNo1);
        }
        return bondAdjList;
    }
    
    /**
     * Return an array containing the atom indexes of a bond that was just deleted.
     * Returns int[], index 0 = atomno1 and index 1 = atomno2
     */
    private int[] getDeletedBond() {
        Bond[] currBondCollection = viewer.ms.bo;
        boolean match;
                
        //iterate over map and compare with current bonds to find missing bond
        for (HashMap.Entry<int[], Boolean> entry : bondMap.entrySet()) {
            if(entry.getValue()) {
                int[] keyBond = entry.getKey();
                match = false;
                
                for(Bond bond : currBondCollection) {
                    if(bond == null) {
                        continue;
                    }
                    int atom1 = bond.getAtomIndex1()+1;
                    int atom2 = bond.getAtomIndex2()+1;
                  
                    if(atom1 == keyBond[0] && atom2 == keyBond[1]) {
                        //bond still exists
                        match = true;
                        break;
                    }
                }
                if(!match) {
                    //bond doesn't exist anymore
                    bondMap.put(keyBond, false);
                    return keyBond;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns a Bond Map copy of the viewer.ms.bo BondCollection
     * Key: int[], index 0 = atomno 1, index 1 = atomno 2
     * Value: boolean true denotes existing bond, false denotes non-existing
     */
    private HashMap<int[], Boolean> getBondMap() {
        HashMap<int[], Boolean> map = new HashMap<int[], Boolean>();
        for(Bond bond: viewer.ms.bo) {
            if(bond != null) {
                int [] newBond = new int[2];
                int atom1 = bond.getAtomIndex1()+1;
                int atom2 = bond.getAtomIndex2()+1;
                newBond[0] = atom1;
                newBond[1] = atom2;
                map.put(newBond, true);
            }
        }
        return map;
    }
}
