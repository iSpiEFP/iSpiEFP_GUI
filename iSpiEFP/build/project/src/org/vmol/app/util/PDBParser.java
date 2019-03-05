package org.vmol.app.util;

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

    public static ArrayList get_atoms(File file) throws IOException {
        ArrayList atoms = new ArrayList();
        FileReader fileReader = new FileReader(file);
        BufferedReader br = new BufferedReader(fileReader);
        String line;
        int count = 0;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("ATOM") || line.startsWith("HETATM")) {
                String[] tokens = line.split("\\s+");
                Atom a = new Atom(tokens[tokens.length - 1], count, Double.parseDouble(tokens[5]), Double.parseDouble(tokens[6]), Double.parseDouble(tokens[7]));
                atoms.add(a);
                count++;
            }
        }
        br.close();
        return atoms;
    }

    private static double distance(Atom a, Atom b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2) + Math.pow(a.z - b.z, 2));
    }

    private boolean compare(double a, double b) {
        if (a <= b) {
            return true;
        } else {
            return false;
        }
    }

    private static int searchArray(String[] a, String to_be_matched) {
        for (int i = 0; i < a.length; i++) {
            if (a[i].equals(to_be_matched)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isConnected(Atom a, Atom b) throws UnrecognizedAtomException {
        String[] bonds = {"HH", "CC", "NN", "OO", "FF", "CLCL", "BRBR", "II", "CN", "NC", "CO", "OC", "CS", "SC", "CF", "FC", "CCL", "CLC", "CBR", "BRC", "CI", "IC", "HC", "CH", "HN", "NH", "HO", "OH", "HF", "FH", "HCL", "CLH", "HBR", "BRH", "HI", "IH", "ON", "NO"};
        double[] lengths = {0.74, 1.54, 0.45, 1.48, 1.42, 1.99, 2.28, 2.67, 1.47, 1.47, 1.43, 1.43, 1.82, 1.82, 1.35, 1.35, 1.77, 1.77, 1.94, 1.94, 2.14, 2.14, 1.09, 1.09, 1.01, 1.01, 0.96, 0.96, 0.92, 0.92, 1.27, 1.27, 1.41, 1.41, 1.61, 1.61, 1.36, 1.36};
        int index = searchArray(bonds, a.type + b.type);
        if (index == -1) {
            System.out.println(a.type + b.type);
            throw new UnrecognizedAtomException("Unknown bond pair");
        }
        double dist = distance(a, b);
        if (Math.abs(dist - lengths[index]) <= 0.05) {
            return true;
        } else {
            return false;
        }

    }

    public static ArrayList<ArrayList<Atom>> connectivity(File file) throws IOException, UnrecognizedAtomException {
        ArrayList<Atom> atoms = new ArrayList<Atom>();
        FileReader fileReader = new FileReader(file);
        BufferedReader br = new BufferedReader(fileReader);
        String line;
        int index = 0;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("ATOM") || line.startsWith("HETATM")) {
                String[] tokens = line.split("\\s+");
                Atom a = new Atom(tokens[tokens.length - 1], index, Double.parseDouble(tokens[5]), Double.parseDouble(tokens[6]), Double.parseDouble(tokens[7]));
                System.out.println(a.type + " " + a.x + " " + a.y + " " + a.z);
                atoms.add(a);
                index++;
            }
        }
        br.close();
        ArrayList<ArrayList<Atom>> connections = new ArrayList<ArrayList<Atom>>();
        for (int i = 0; i < index; i++) {
            connections.add(new ArrayList<Atom>());
        }
        for (int i = 0; i < index; i++) {
            for (int j = 0; j < index; j++) {
                if (i == j) {
                    continue;
                }
                if (isConnected(atoms.get(i), atoms.get(j))) {
                    connections.get(i).add(atoms.get(j));
                    if (atoms.get(i).type.equals("O") && atoms.get(j).type.equals("O")) {
                        if (distance(atoms.get(i), atoms.get(j)) <= 1.21) {
                            connections.get(i).add(atoms.get(j));
                        }
                    }

                    if (atoms.get(i).type.equals("C") && atoms.get(j).type.equals("C")) {
                        if (distance(atoms.get(i), atoms.get(j)) <= 1.34) {
                            connections.get(i).add(atoms.get(j));
                        }
                    }
                }

            }
        }
        return connections;

    }

}
