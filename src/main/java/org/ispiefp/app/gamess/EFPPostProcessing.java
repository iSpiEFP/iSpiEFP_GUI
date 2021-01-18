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

package org.ispiefp.app.gamess;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class EFPPostProcessing {

    private double distance(double[] atom1, double[] atom2) {
        return Math.sqrt(Math.pow(atom1[0] - atom2[0], 2) + Math.pow(atom1[1] - atom2[1], 2) + Math.pow(atom1[2] - atom2[2], 2));
    }

    private int get_min_index(double[] val) {
        int min_index = 0;
        double min = val[0];
        for (int i = 1; i < val.length; i++) {
            if (val[i] < min) {
                min_index = i;
                min = val[i];
            }
        }
        return min_index;
    }

    private int get_ct_no(String fname) {
        try {
            int no = 0;

            File efp = new File(fname);
            FileReader fileReader = new FileReader(efp);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(" LMO CENTROIDS")) {
                    while (true) {
                        line = bufferedReader.readLine();

                        if (line.startsWith(" STOP")) {
                            break;
                        }
                        no++;
                    }
                }
            }

            fileReader.close();
            return no;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int get_ct(String fname, double[] h) {
        try {

            int ct_no = get_ct_no(fname);
            double[] dist = new double[ct_no];
            double[][] cts = new double[ct_no][3];
            File efp = new File(fname);
            FileReader fileReader = new FileReader(efp);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int index = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(" LMO CENTROIDS")) {
                    while (true) {
                        line = bufferedReader.readLine();
                        if (line.startsWith(" STOP")) {
                            break;
                        }
                        String[] tokens = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
                        cts[index][0] = Double.parseDouble(tokens[1]);
                        cts[index][1] = Double.parseDouble(tokens[2]);
                        cts[index][2] = Double.parseDouble(tokens[3]);
                        index++;


                    }

                    for (int i = 0; i < ct_no; i++) {
                        dist[i] = distance(cts[i], h);
                    }
                }
            }
            fileReader.close();

            return get_min_index(dist);


        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

    }

    private boolean exists(ArrayList a, int val) {
        for (int i = 0; i < a.size(); i++) {
            if ((Integer) a.get(i) == val) {
                return true;
            }
        }
        return false;
    }

    public void process(String fname) {
        try {
            ArrayList xyz = new ArrayList();
            ArrayList monopole_charge = new ArrayList();
            ArrayList dipole_moment = new ArrayList();
            ArrayList quadpole_moment = new ArrayList();
            ArrayList octupole_moment = new ArrayList();
            ArrayList pol = new ArrayList();
            ArrayList dynamic_pol = new ArrayList();
            ArrayList basis = new ArrayList();
            ArrayList wave = new ArrayList();
            ArrayList fock = new ArrayList();
            ArrayList lmo_coord = new ArrayList();
            ArrayList screen1 = new ArrayList();
            ArrayList screen2 = new ArrayList();


            String multiplicity = "";
            int ct_no = get_ct_no(fname);

            ArrayList h_index = new ArrayList();
            ArrayList<ArrayList> h_coord = new ArrayList<ArrayList>();
            ArrayList h_bond_index = new ArrayList();

            ArrayList ct = new ArrayList();

            File efp = new File(fname);
            FileReader fileReader = new FileReader(efp);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(" COORDINATES (BOHR)")) {  // multipole moments
                    while (true) {
                        line = bufferedReader.readLine();
                        String[] tokens = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

                        if (tokens[0].endsWith("H000")) {
                            int index = Integer.parseInt(tokens[0].substring(1, tokens[0].length() - 4));
                            h_index.add(index);
                            ArrayList coord = new ArrayList();
                            coord.add(Double.parseDouble(tokens[1]));
                            coord.add(Double.parseDouble(tokens[2]));
                            coord.add(Double.parseDouble(tokens[3]));
                            h_coord.add(coord);
                            continue;
                        }

                        if (tokens[0].startsWith("BO")) {
                            String tmp = tokens[0].substring(2);
                            boolean match = false;
                            for (int i = 0; i < h_index.size(); i++) {
                                if (tmp.startsWith((h_index.get(i)).toString())) {
                                    match = true;
                                    h_bond_index.add(tokens[0]);
                                }
                            }
                            if (match == false) {
                                xyz.add(line);
                                //System.out.println(line);
                            }
                            continue;
                        }

                        if (line.startsWith(" STOP")) {
                            break;
                        }

                        xyz.add(line);
                        //System.out.println(line);

                    }
                }

                if (line.startsWith(" MONOPOLES")) {
                    HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
                    while (true) {
                        line = bufferedReader.readLine();

                        if (line.startsWith(" STOP")) {
                            break;
                        }
                        String[] tokens = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

                        if (tokens[0].endsWith("H000")) {
                            hm.put(Integer.parseInt(tokens[0].substring(1, tokens[0].length() - 4)), Double.parseDouble(tokens[1]));
                            continue;
                        }

                        if (tokens[0].startsWith("BO")) {
                            String tmp = tokens[0].substring(2);
                            boolean match = false;
                            for (int i = 0; i < h_index.size(); i++) {
                                if (tmp.startsWith((h_index.get(i)).toString())) {
                                    match = true;
                                    int bo_no = Integer.parseInt(tmp.substring((h_index.get(i)).toString().length()));
                                    String charge_line = (String) monopole_charge.get(bo_no - 1);
                                    String[] charge_tokens = charge_line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
                                    double charge = Double.parseDouble(charge_tokens[1]);
                                    String replaced_line = charge_line.replace(charge_tokens[1], String.format("%.10f", charge + 1 + Double.parseDouble(tokens[1]) + hm.get(h_index.get(i))));
                                    monopole_charge.set(bo_no - 1, replaced_line);


                                }
                            }
                            if (match == false) {
                                monopole_charge.add(line);
                                //System.out.println(line);
                            }
                            continue;
                        }


                        monopole_charge.add(line);
                        //System.out.println(line);
                    }
                    for (int i = 0; i < monopole_charge.size(); i++) {
                        System.out.println(monopole_charge.get(i));
                    }

                }

                if (line.startsWith(" DIPOLES")) {
                    while (true) {
                        line = bufferedReader.readLine();

                        if (line.startsWith(" STOP")) {
                            break;
                        }
                        String[] tokens = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

                        if (tokens[0].endsWith("H000")) {
                            continue;
                        }

                        if (tokens[0].startsWith("BO")) {
                            String tmp = tokens[0].substring(2);
                            boolean match = false;
                            for (int i = 0; i < h_index.size(); i++) {
                                if (tmp.startsWith((h_index.get(i)).toString())) {
                                    match = true;

                                }
                            }
                            if (match == false) {
                                dipole_moment.add(line);
                                //System.out.println(line);
                            }
                            continue;
                        }


                        dipole_moment.add(line);
                        //System.out.println(line);
                    }
                }

                if (line.startsWith(" QUADRUPOLES")) {
                    while (true) {
                        line = bufferedReader.readLine();

                        if (line.startsWith(" STOP")) {
                            break;
                        }


                        String[] tokens = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

                        if (tokens.length < 6) {
                            quadpole_moment.add(line);
                            //System.out.println(line);
                            continue;
                        }

                        if (tokens[0].endsWith("H000")) {
                            bufferedReader.readLine();
                            continue;
                        }

                        if (tokens[0].startsWith("BO")) {
                            String tmp = tokens[0].substring(2);
                            boolean match = false;
                            for (int i = 0; i < h_index.size(); i++) {
                                if (tmp.startsWith((h_index.get(i)).toString())) {
                                    match = true;
                                    bufferedReader.readLine();
                                }
                            }
                            if (match == false) {
                                quadpole_moment.add(line);
                                //System.out.println(line);
                            }
                            continue;
                        }


                        quadpole_moment.add(line);
                        //System.out.println(line);
                    }
                }

                if (line.startsWith(" OCTUPOLES")) {
                    while (true) {
                        line = bufferedReader.readLine();

                        if (line.startsWith(" STOP")) {
                            break;
                        }


                        String[] tokens = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

                        if (tokens.length < 6) {
                            octupole_moment.add(line);
                            //System.out.println(line);
                            continue;
                        }

                        if (tokens[0].endsWith("H000")) {
                            bufferedReader.readLine();
                            bufferedReader.readLine();
                            continue;
                        }

                        if (tokens[0].startsWith("BO")) {
                            String tmp = tokens[0].substring(2);
                            boolean match = false;
                            for (int i = 0; i < h_index.size(); i++) {
                                if (tmp.startsWith((h_index.get(i)).toString())) {
                                    match = true;
                                    bufferedReader.readLine();
                                    bufferedReader.readLine();
                                }
                            }
                            if (match == false) {
                                octupole_moment.add(line);
                                //System.out.println(line);
                            }
                            continue;
                        }


                        octupole_moment.add(line);
                        //System.out.println(line);
                    }
                }
                if (line.startsWith(" POLARIZABLE POINTS")) {
                    for (int i = 0; i < h_coord.size(); i++) {
                        double[] h = new double[3];
                        h[0] = (Double) h_coord.get(i).get(0);
                        h[1] = (Double) h_coord.get(i).get(1);
                        h[2] = (Double) h_coord.get(i).get(2);
                        System.out.println(get_ct(fname, h));
                        ct.add(get_ct(fname, h) + 1);
                    }

                    while (true) {
                        line = bufferedReader.readLine();

                        if (line.startsWith(" STOP")) {
                            break;
                        }


                        String[] tokens = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");


                        if (tokens[0].startsWith("CT")) {
                            int index = Integer.parseInt(tokens[0].substring(2));
                            if (exists(ct, index)) {
                                bufferedReader.readLine();
                                bufferedReader.readLine();
                                bufferedReader.readLine();
                                continue;
                            } else {
                                pol.add(line.substring(tokens[0].length() + 2));
                                continue;
                            }
                        }

                        pol.add(line);      // remember to change CT# when later reassembling the efp file
                        //System.out.println(line);
                    }

                }

                if (line.startsWith(" DYNAMIC POLARIZABLE POINTS")) {
                    while (true) {
                        line = bufferedReader.readLine();

                        if (line.startsWith(" STOP")) {
                            break;
                        }


                        String[] tokens = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");


                        if (tokens[0].startsWith("CT")) {
                            int index = Integer.parseInt(tokens[1]);
                            if (exists(ct, index)) {
                                bufferedReader.readLine();
                                bufferedReader.readLine();
                                bufferedReader.readLine();
                                continue;
                            } else {
                                dynamic_pol.add(line.substring(tokens[1].length() + 6));
                                continue;
                            }
                        }

                        dynamic_pol.add(line);      // remember to change CT# when later reassembling the efp file
                        //System.out.println(line);
                    }
                }
                if (line.startsWith(" PROJECTION BASIS SET")) {
                    while (true) {
                        line = bufferedReader.readLine();
                        if (line.startsWith(" STOP")) {
                            break;
                        }
                        String[] tokens = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

                        if (tokens.length > 0 && tokens[0].endsWith("H000")) {
                            while (true) {
                                line = bufferedReader.readLine();
                                if (line.equals("  ")) {
                                    break;
                                }
                            }
                        }

                        basis.add(line);
                        //System.out.println(line);

                    }
                }

                if (line.startsWith(" MULTIPLICITY")) {
                    multiplicity = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+")[1];
                    //System.out.println(multiplicity);
                }

                if (line.startsWith(" PROJECTION WAVEFUNCTION")) {
                    wave.add(line.replace(Integer.toString(ct_no), Integer.toString(ct_no - ct.size())));
                    while (true) {
                        line = bufferedReader.readLine();
                        if (line.startsWith(" FOCK MATRIX ELEMENTS")) {
                            break;
                        }
                        String[] tokens = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
                        if (exists(ct, Integer.parseInt(tokens[0]))) {
                            continue;
                        } else {
                            wave.add(line.substring(3));

                        }

                        //wave.add(line);
                        //System.out.println(line);
                    }

                }

                if (line.startsWith(" FOCK MATRIX ELEMENTS")) {

                    double[][] old_fock = new double[ct_no][ct_no];
                    int i = 0;
                    int j = 0;
                    for (int m = 0; m < ct_no; m++) {
                        for (int n = 0; n < ct_no; n++) {
                            old_fock[m][n] = 0;
                        }
                    }


                    while (true) {
                        line = bufferedReader.readLine();
                        if (line.startsWith(" LMO CENTROIDS")) {
                            break;
                        }
                        String[] tokens = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
                        for (int m = 0; m < tokens.length; m++) {
                            try {
                                double val = Double.parseDouble(tokens[m]);
                                old_fock[i][j] = val;
                                j++;
                                if (j == ct_no) {
                                    i++;
                                    j = i;
                                }
                            } catch (NumberFormatException e) {
                                continue;
                            }
                        }
                    }

                    for (i = 0; i < ct_no; i++) {
                        for (j = 0; j < ct_no; j++) {
                            if (old_fock[i][j] == 0) {
                                old_fock[i][j] = old_fock[j][i];
                            }
                        }
                    }

                    for (i = 0; i < ct.size(); i++) {
                        for (int m = 0; m < ct_no; m++) {
                            old_fock[(Integer) ct.get(i) - 1][m] = 0;
                        }
                        for (int m = 0; m < ct_no; m++) {
                            old_fock[m][(Integer) ct.get(i) - 1] = 0;
                        }
                    }


                    double[][] new_fock = new double[ct_no - ct.size()][ct_no - ct.size()];
                    for (i = 0; i < ct_no - ct.size(); i++) {
                        for (j = 0; j < ct_no - ct.size(); j++) {
                            new_fock[i][j] = 0;
                        }
                    }


                    i = 0;
                    j = 0;


                    for (int m = 0; m < ct_no - ct.size(); m++) {
                        for (int n = m; n < ct_no - ct.size(); n++) {
                            while (old_fock[i][j] == 0) {
                                j++;
                                if (j == ct_no) {
                                    i++;
                                    j = i;
                                }
                            }

                            new_fock[m][n] = old_fock[i][j];


                            j++;
                            if (j == ct_no) {
                                i++;
                                j = i;
                            }


                        }
                    }


                    for (int m = 0; m < new_fock.length; m++) {
                        for (int n = 0; n < new_fock.length; n++) {
                            if (new_fock[m][n] != 0) {
                                fock.add(new_fock[m][n]);
                            }
                        }
                    }


                }

                if (line.startsWith(" LMO CENTROIDS")) {
                    while (true) {
                        line = bufferedReader.readLine();
                        if (line.startsWith(" STOP")) {
                            break;
                        }

                        String[] tokens = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
                        int index = Integer.parseInt(tokens[0].substring(2));
                        if (!exists(ct, index)) {
                            //System.out.println(line);
                            lmo_coord.add(line.substring(tokens[0].length() + 2));
                        }
                    }
                }

                if (line.startsWith("SCREEN2")) {
                    screen2.add(line);
                    while (true) {
                        line = bufferedReader.readLine();

                        if (line.startsWith("STOP")) {
                            break;
                        }

                        String[] tokens = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

                        if (tokens[0].endsWith("H000")) {
                            continue;
                        }

                        if (tokens[0].startsWith("BO")) {
                            String tmp = tokens[0].substring(2);
                            boolean match = false;
                            for (int i = 0; i < h_index.size(); i++) {
                                if (tmp.startsWith((h_index.get(i)).toString())) {
                                    match = true;

                                }
                            }
                            if (match == false) {
                                screen2.add(line);
                                //System.out.println(line);
                            }
                            continue;
                        }


                        screen2.add(line);
                        //System.out.println(line);
                    }
                }

                if (line.startsWith("SCREEN")) {
                    screen1.add(line);
                    while (true) {
                        line = bufferedReader.readLine();

                        if (line.startsWith("STOP")) {
                            break;
                        }

                        String[] tokens = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

                        if (tokens[0].endsWith("H000")) {
                            continue;
                        }

                        if (tokens[0].startsWith("BO")) {
                            String tmp = tokens[0].substring(2);
                            boolean match = false;
                            for (int i = 0; i < h_index.size(); i++) {
                                if (tmp.startsWith((h_index.get(i)).toString())) {
                                    match = true;

                                }
                            }
                            if (match == false) {
                                screen1.add(line);
                                //System.out.println(line);
                            }
                            continue;
                        }


                        screen1.add(line);
                        //System.out.println(line);
                    }
                }


            }

//			for (int m = 0; m < 13-ct.size(); m ++) {
//				for (int n = 0; n < 13-ct.size(); n ++) {
//					System.out.print(fock[m][n]);
//					System.out.print("   ");
//					
//				}
//				System.out.println(" ");
//			}

//			System.out.println(fock.size());
            fileReader.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter("wo_hydrogen.efp", false));
            writer.append(" $FRAGNAME\n");
            writer.append("EFP DATA FOR FRAGNAME SCFTYP=RHF     ... GENERATED WITH BASIS SET=XXX\n");
            writer.append(" COORDINATES (BOHR)\n");
            for (int i = 0; i < xyz.size(); i++) {
                writer.append((String) xyz.get(i) + "\n");
            }
            writer.append(" STOP\n");
            writer.append(" MONOPOLES\n");
            for (int i = 0; i < monopole_charge.size(); i++) {
                writer.append((String) monopole_charge.get(i) + "\n");
            }
            writer.append(" STOP\n");
            writer.append(" DIPOLES\n");
            for (int i = 0; i < dipole_moment.size(); i++) {
                writer.append((String) dipole_moment.get(i) + "\n");
            }
            writer.append(" STOP\n");
            writer.append(" QUADRUPOLES\n");
            for (int i = 0; i < quadpole_moment.size(); i++) {
                writer.append((String) quadpole_moment.get(i) + "\n");
            }
            writer.append(" STOP\n");
            writer.append(" OCTUPOLES\n");
            for (int i = 0; i < octupole_moment.size(); i++) {
                writer.append((String) octupole_moment.get(i) + "\n");
            }
            writer.append(" STOP\n");
            writer.append(" POLARIZABLE POINTS\n");
            for (int i = 0; i < pol.size(); i++) {
                if (i % 4 == 0) {
                    writer.append("CT" + (i / 4 + 1) + "  " + (String) pol.get(i) + "\n");
                } else {
                    writer.append((String) pol.get(i) + "\n");
                }

            }
            writer.append(" STOP\n");
            writer.append(" DYNAMIC POLARIZABLE POINTS\n");
            int index = 1;
            for (int i = 0; i < dynamic_pol.size(); i++) {
                if (i % 4 == 0) {
                    writer.append("CT  " + index + "  " + (String) dynamic_pol.get(i) + "\n");
                    index++;
                    if (index > ct_no - ct.size()) {
                        index = 1;
                    }
                } else {
                    writer.append((String) dynamic_pol.get(i) + "\n");
                }

            }
            writer.append(" STOP\n");
            writer.append(" PROJECTION BASIS SET\n");
            for (int i = 0; i < basis.size() - 1; i++) {
                writer.append((String) basis.get(i) + "\n");
            }
            writer.append(" STOP\n");
            writer.append(" MULTIPLICITY    " + multiplicity + "\n");
            writer.append(" STOP\n");
            writer.append((String) wave.get(0) + "\n");
            index = 0;
            for (int i = 1; i < wave.size(); i++) {
                String wave_line = (String) wave.get(i);
                //System.out.println(wave_line);
                if (wave_line.startsWith(" 1")) {
                    index++;
                }
                if (index < 10) {
                    writer.append(" " + index + " " + wave_line + "\n");
                } else {
                    writer.append(index + " " + wave_line + "\n");
                }

            }
            writer.append(" FOCK MATRIX ELEMENTS\n");
            StringBuilder sb = new StringBuilder();
            index = 0;
            for (int i = 0; i < fock.size(); i++) {
                Double no = (Double) fock.get(i);
                if (no > 0) {
                    sb.append("    " + String.format("%.10f", no));
                } else {
                    sb.append("   " + String.format("%.10f", no));
                }
                index++;
                if (index == 4) {
                    if (i == fock.size() - 1) {
                        writer.append(sb.toString() + "\n");
                        index = 0;
                        sb = new StringBuilder();
                    } else {
                        writer.append(sb.toString() + " >\n");
                        index = 0;
                        sb = new StringBuilder();
                    }

                }
            }
            if (index != 0) {
                writer.append(sb.toString() + "\n");
            }
            writer.append(" LMO CENTROIDS\n");
            index = 1;

            for (int i = 0; i < lmo_coord.size(); i++) {
                writer.append("CT" + index + "  " + (String) lmo_coord.get(i) + "\n");
                index++;
            }
            writer.append(" STOP\n");
            for (int i = 0; i < screen2.size(); i++) {
                writer.append((String) screen2.get(i) + "\n");
            }
            writer.append("STOP\n");
            for (int i = 0; i < screen1.size(); i++) {
                writer.append((String) screen1.get(i) + "\n");
            }
            writer.append("STOP\n");
            writer.append(" $END\n");

            writer.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
