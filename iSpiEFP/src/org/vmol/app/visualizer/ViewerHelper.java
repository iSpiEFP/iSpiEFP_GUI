package org.vmol.app.visualizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.jmol.viewer.Viewer;
import org.vmol.app.util.Atom;

public class ViewerHelper {
    //int atom1 = jmolPanel.viewer.ms.bo[i].getAtomIndex1();
    private Viewer viewer;
    private Viewer auxViewer;
    ArrayList<Integer> atomList;
    
    public ViewerHelper(Viewer viewer, Viewer auxViewer, ArrayList<Integer> atomList) {
        this.viewer = viewer;
        this.atomList = atomList;
        this.auxViewer = auxViewer;
    }
    
    public ViewerHelper() {
        
    }
    
    //returns a chemical formula from an array size 26(alphabet) with counts of occurences
    //this uses Hill's method for generating a formula
    //First count occurences of 'C' then 'H' then the rest in sorted order
    public String getChemicalFormula(int[] symbols) {
        StringBuilder chemicalFormula = new StringBuilder();
        int c_index = 'C' - 'A';
        int h_index = 'H' - 'A';
        
        if(symbols[c_index] > 0) {
            chemicalFormula.append("C"+symbols[c_index]);
        }
        if(symbols[h_index] > 0) {
            chemicalFormula.append("H"+symbols[h_index]);
        }
        for(int i = 0; i < symbols.length; i++) {
            if(i != c_index && i != h_index) {
                if(symbols[i] > 0){
                    char symbol = ((char)('A' + i));
                    chemicalFormula.append((Character.toString(symbol))+symbols[i]);
                }
            }
        }
        return chemicalFormula.toString();
    }
    
    
    
    
    public void ConnectXYZBonds() {
        System.out.println("Dumping atoms from actually fragment");
        Collections.sort(atomList);
        int i = 0;
        auxViewer.runScript("set picking CONNECT;");
        
        for(int atomNum : atomList) {
            
            System.out.print((atomNum+1)+":");
            //System.out.println(viewer.ms.bo[atomNum].getAtomIndex1()+" bonded to "+viewer.ms.bo[atomNum].getAtomIndex2());
            System.out.print(viewer.ms.at[atomNum].getAtomNumber());
            int xyzAtomNum = translateIndex(atomList, atomNum);
            
            //set picking CONNECT
            System.out.println(xyzAtomNum);
            System.out.println("bonded too");
            org.jmol.modelset.Atom atom = viewer.ms.at[atomNum];
            for(int u = 0; u < atom.getBondCount(); u++){
                //System.out.println(atom.getBondedAtomIndex(u));
                System.out.println(viewer.ms.at[atom.getBondedAtomIndex(u)]);
                
                //.getAtomNumber();
                org.jmol.modelset.Atom bondedAtom = viewer.ms.at[atom.getBondedAtomIndex(u)];
                int bondedAtomNum = translateIndex(atomList, parseAtomNum(bondedAtom));    
                System.out.println(bondedAtomNum);
                System.out.println(atom.getBond(atom));
                
                //auxViewer.ms.at[8].
                auxViewer.runScript("connect (atomno="+xyzAtomNum+") (atomno="+bondedAtomNum+")");
                auxViewer.runScript("spin on;");

            }
            System.out.print(auxViewer.ms.at[i].getAtomNumber());
            System.out.println(xyzAtomNum);
           // for(int i = 0; i < auxViewer.ms.at.length; i++) {
               // System.out.println(auxView);
        //   }
            //System.out.print("        ");
            //System.out.print(i+":");
            //viewer.ms.at[atomNum].ge
            //System.out.print(auxViewer.ms.at[i].getAtomName());
            i++;
            
            System.out.println("");
        }
        /*
        System.out.println("Dumping atoms from new xyz fragment");
        int n = atomList.size();
        for(int i = 0; i < n; i++) {
            System.out.println(i);
            System.out.println(viewer.ms.at[i].getAtomName());
            
        }*/
        
    }
    
    private int translateIndex(ArrayList<Integer> list, int i) {
        int index = 0;
        index = list.indexOf(i)+1; 
        return index;
    }
    
    private int parseAtomNum(org.jmol.modelset.Atom bondedAtom) {        
        int num = 0;
        String strAtom = bondedAtom.toString();
        String[] strArr = strAtom.split("#");
        num = Integer.parseInt(strArr[1]);
        return num-1;
    }
    
    /*
     * Converts a HashMap into a chemical formula using Hill's method
     * this uses Hill's method for generating a formula
     * First count occurences of 'C' then 'H' then the rest in sorted order
     */
    public String getChemicalFormula2(TreeMap<String, Integer> symbolMap) {
        StringBuilder chemicalFormula = new StringBuilder();
        Integer count;
        
        //first check for appearances of Carbon
        count = symbolMap.get("C");
        if(count != null) {
            chemicalFormula.append("C"+count);
        }
        
        //next check for appearances of Hydrogen
        count = symbolMap.get("H");
        if(count != null) {
            chemicalFormula.append("H"+count);
        }
        
        //Finally dump the rest of the list one by one in a sorted fashion
        for(Map.Entry<String,Integer> entry : symbolMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            if(!key.equalsIgnoreCase("C") && !key.equalsIgnoreCase("H")) {
                chemicalFormula.append(key+value);
            }
        }
        return chemicalFormula.toString();
    }
    
    public static double convertAngstromToBohr(double angstrom) {
        return angstrom * 1.88972687777;
    }
    
    public static double convertBohrToAngstrom(double bohr) {
        return bohr * 0.529177;
    }
}
