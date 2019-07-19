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
