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

package org.ispiefp.app.util;

import org.ispiefp.app.visualizer.JmolMainPanel;
import org.ispiefp.app.visualizer.ViewerHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JmolHandler {

    /**
     * Written by Ryan DeRue
     * Method creates a temporary xyz file for the viewerFragment whose index is passed to this function. This function
     * is primarily used for computing RMSDs against the xyz files of the library fragments. The created file will be
     * deleted when the program exits.
     *
     * @param fragmentIndex The index of the fragment in the viewer
     * @return A java.io file containing the xyz coordinates of a viewerFragment.
     * @throws IOException if the file is not able to be created.
     */
    public static File createTempXYZFileFromViewer(JmolMainPanel jmolMainPanel, int fragmentIndex) throws IOException {
        BufferedWriter bw = null;
        File xyzFile = null;
        try {
            //Create a temp xyz file
            xyzFile = File.createTempFile("fragment_" + fragmentIndex, ".xyz");
            xyzFile.deleteOnExit();
            bw = new BufferedWriter(new FileWriter(xyzFile));

            ArrayList<Integer> atoms = getGroups(jmolMainPanel.getFragmentComponents()).get(fragmentIndex);
            //Write number of atoms not including dummy atoms in XYZ file
            bw.write(String.format("%d%n%n", atoms.size()));
            System.out.println(atoms.size());
            System.out.println();
            for (int atom_num : atoms) {
                org.jmol.modelset.Atom atom = jmolMainPanel.viewer.ms.at[atom_num];
                bw.write(String.format("%s\t%.5f\t%.5f\t%.5f%n",
                        atom.getAtomName(),
                        ViewerHelper.convertAngstromToBohr(atom.x),
                        ViewerHelper.convertAngstromToBohr(atom.y),
                        ViewerHelper.convertAngstromToBohr(atom.z)
                ));
                System.out.println(String.format("%s\t%.5f\t%.5f\t%.5f",
                        atom.getAtomName(),
                        ViewerHelper.convertAngstromToBohr(atom.x),
                        ViewerHelper.convertAngstromToBohr(atom.y),
                        ViewerHelper.convertAngstromToBohr(atom.z)
                ));
            }
        } finally {
            if (bw != null) bw.close();
        }
        return xyzFile;
    }

    //converts Addison's frag list to Hanjings Groups
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ArrayList<ArrayList> getGroups(List<ArrayList<Integer>> fragment_list) {
        ArrayList<ArrayList> groups = new ArrayList<ArrayList>();
        for (ArrayList<Integer> frag : fragment_list) {
            if (frag.size() > 0) {
                ArrayList curr_group = new ArrayList();
                for (int piece : frag) {
                    curr_group.add(piece);
                }
                Collections.sort(curr_group);
                groups.add(curr_group);
            }
        }
        return groups;
    }
}
