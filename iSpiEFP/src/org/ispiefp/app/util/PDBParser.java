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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PDBParser {
    public static ArrayList parse(File file) throws IOException {
        ArrayList atoms = new ArrayList();
        FileReader fileReader = new FileReader(file);
        BufferedReader br = new BufferedReader(fileReader);
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("ATOM") || line.startsWith("HETATM")) {
                String[] tokens = line.split("\\s+");
                atoms.add(tokens[tokens.length - 1]);
            }
        }
        br.close();
        return atoms;
    }

    //Not sure if this function is even ever used
    public static ArrayList get_atoms(File file) throws IOException {
        ArrayList atoms = new ArrayList();
        FileReader fileReader = new FileReader(file);
        BufferedReader br = new BufferedReader(fileReader);
        String line;
        int count = 0;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("ATOM") || line.startsWith("HETATM")) {
                String[] tokens = line.split("\\s+");
                String x = line.substring(31, 38).trim();
                String y = line.substring(39, 46).trim();
                String z = line.substring(47, 54).trim();
                if (tokens[tokens.length - 1].matches(".*\\d+.*")) { // atom symbol has digits, treat as charged atom
                    System.out.println("Warning: " + tokens[tokens.length - 1]);
                    String symbol = tokens[tokens.length - 1];
                    //String symbol = tokens[2];
                    String sign = symbol.substring(symbol.length() - 1);
                    String digits = symbol.replaceAll("\\D+", "");
                    String real_symbol = symbol.substring(0, symbol.length() - 1 - digits.length());
                    System.out.println("Warning: " + real_symbol);
                    Atom a = new Atom(real_symbol, count, 0, Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));
                    //Integer i = new Integer(null); //throw error when it gets here
                    atoms.add(a);
                } else {
                    Atom a = new Atom(tokens[tokens.length - 1], count, 0, Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));
                    //   Integer i = new Integer(null); //throw error when it gets here
                    atoms.add(a);
                }
                //Atom a = new Atom(tokens[tokens.length-1], count,Double.parseDouble(x),Double.parseDouble(y),Double.parseDouble(z) );
                //atoms.add(a);
                count++;
            }
        }
        br.close();
        return atoms;
    }


}
