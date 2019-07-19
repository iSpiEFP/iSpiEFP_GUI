package org.ispiefp.app.visualizer;

import org.jmol.viewer.Viewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Helper Class for unique functions involving the jmol viewer
 */
public class ViewerHelper {
    ArrayList<Integer> atomList;
    private Viewer viewer;
    private Viewer auxViewer;

    public ViewerHelper(Viewer viewer, Viewer auxViewer, ArrayList<Integer> atomList) {
        this.viewer = viewer;
        this.atomList = atomList;
        this.auxViewer = auxViewer;
    }

    public ViewerHelper() {

    }

    public static double convertAngstromToBohr(double angstrom) {
        return angstrom * 1.88972687777;
    }

    public static double convertBohrToAngstrom(double bohr) {
        return bohr * 0.529177;
    }

    /**
     * Connect XYZ bonds that are seperated. This is done by modeling the connections
     * based upon a Fragment A, and putting the same bonds onto Fragment B.
     */
    public void ConnectXYZBonds() {
        System.out.println("Dumping atoms from actually fragment");
        Collections.sort(atomList);
        int i = 0;
        auxViewer.runScript("set picking CONNECT;");

        for (int atomNum : atomList) {
            int xyzAtomNum = translateIndex(atomList, atomNum);
            org.jmol.modelset.Atom atom = viewer.ms.at[atomNum];

            for (int u = 0; u < atom.getBondCount(); u++) {
                org.jmol.modelset.Atom bondedAtom = viewer.ms.at[atom.getBondedAtomIndex(u)];
                int bondedAtomNum = translateIndex(atomList, parseAtomNum(bondedAtom));

                auxViewer.runScript("connect (atomno=" + xyzAtomNum + ") (atomno=" + bondedAtomNum + ")");
                auxViewer.runScript("spin on;");
            }
            i++;
        }

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
        if (count != null) {
            chemicalFormula.append("C" + count);
        }

        //next check for appearances of Hydrogen
        count = symbolMap.get("H");
        if (count != null) {
            chemicalFormula.append("H" + count);
        }

        //Finally dump the rest of the list one by one in a sorted fashion
        for (Map.Entry<String, Integer> entry : symbolMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            if (!key.equalsIgnoreCase("C") && !key.equalsIgnoreCase("H")) {
                chemicalFormula.append(key + value);
            }
        }
        return chemicalFormula.toString();
    }

    /**
     * Helper function for tranlating the index of a jmol Atom to a real index
     *
     * @param list
     * @param i
     * @return
     */
    private int translateIndex(ArrayList<Integer> list, int i) {
        int index = 0;
        index = list.indexOf(i) + 1;
        return index;
    }

    /**
     * Helper function for parsing the number from an atom
     */
    private int parseAtomNum(org.jmol.modelset.Atom bondedAtom) {
        int num = 0;
        String strAtom = bondedAtom.toString();
        String[] strArr = strAtom.split("#");
        num = Integer.parseInt(strArr[1]);
        return num - 1;
    }
}
