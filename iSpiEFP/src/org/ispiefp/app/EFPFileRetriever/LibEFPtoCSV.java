package org.ispiefp.app.EFPFileRetriever;

import java.io.*;

public class LibEFPtoCSV {
    private final int ENERGIES = 6; // the number of different energy calculations, excluding total
    private final int TOTAL_ENERGY_WHITESPACE = 34; // the leading whitespace in a total energy file
    private final int PAIRWISE_WHITESPACE = 44; // the leading whitespace in a pairwise energy file
    private final int TOTAL_INDEX = 0;
    private final int PAIRWISE_INDEX = 1;

    private enum Type {
        REGULAR,
        OPTIMIZATION,
        MOLECULAR
    }

    /* This method takes as a parameter a file name and gets the necessary information to create a string to write to
       a CSV file. The string is in the format "geometry_number,electrostatic,polarization,dispersion,
       exchange_repulsion,point_charges,charge_penetration,total,identifier," followed by either "-1,-1,-1\n" (empty),
        "energy_change,RMS_gradient,maximum_gradient\n" (geometry optimization), or
        "kinetic_energy,invariant,temperature\n" (molecular dynamics)
     */


    public LibEFPtoCSV() {
        System.out.println("Preparing to convert files");
    }

    public String[] getCSVString(String fileName) {
        String[] csvString = new String[2];
        boolean totalEntered = false;
        boolean pairwiseEntered = false;
        File file = new File(fileName);
        int geometries = 0;
        int fragment = 1;
        Type type = Type.REGULAR;
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while (true) {
                line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }

                /* Get the energy results once we reach the key line,
                 * filtering based on pairwise or not
                 */

                if (line.contains("PAIRWISE ENERGY COMPONENTS")) {
                    if (!pairwiseEntered) {
                        csvString[PAIRWISE_INDEX] = new String();
                        csvString[PAIRWISE_INDEX] += "Ligand #,Fragment #,Electrostatic,Polarization,Dispersion,Exchange-repulsion," +
                                "Charge-penetration,Total\n";
                        pairwiseEntered = true;
                    }
                    csvString[PAIRWISE_INDEX] += "0,";
                    csvString[PAIRWISE_INDEX] += fragment;
                    fragment++;
                    csvString[PAIRWISE_INDEX] += ",";
                    for (int i = 0; i < this.ENERGIES; i++) {
                        csvString[PAIRWISE_INDEX] += getLineInformation(bufferedReader, this.PAIRWISE_WHITESPACE);
                        if (i != this.ENERGIES - 1) {
                            csvString[PAIRWISE_INDEX] += ",";
                        }
                    }
                    csvString[PAIRWISE_INDEX] += "\n";
                } else if (line.contains("LATTICE ENERGY COMPONENTS")) {
                    csvString[PAIRWISE_INDEX] += "0,-1,";
                    for (int i = 0; i < this.ENERGIES; i++) {
                        csvString[PAIRWISE_INDEX] += getLineInformation(bufferedReader, this.PAIRWISE_WHITESPACE);
                        if (i != this.ENERGIES - 1) {
                            csvString[PAIRWISE_INDEX] += ",";
                        }
                    }
                    csvString[PAIRWISE_INDEX] += "\n";
                } else if (line.contains("ENERGY COMPONENTS")) {
                    if (!totalEntered) {
                        csvString[TOTAL_INDEX] = new String();
                        totalEntered = true;
                    }

                    /* add to the first column the iteration and a comma */

                    csvString[TOTAL_INDEX] += geometries;
                    geometries++;
                    csvString[TOTAL_INDEX] += ",";
                    bufferedReader.readLine();    // read the empty line

                   /* repeat the following process to get the energy for each component (6 times)
                      Electrostatic, polarization, dispersion, exchange repulsion, point charges, and charge penetration
                    */

                    for (int i = 0; i < this.ENERGIES; i++) {
                        csvString[TOTAL_INDEX] += getLineInformation(bufferedReader, this.TOTAL_ENERGY_WHITESPACE);
                        csvString[TOTAL_INDEX] += ",";
                    }
                    bufferedReader.readLine(); // read the empty line

                    /* get the total energy */

                    csvString[TOTAL_INDEX] += getLineInformation(bufferedReader, this.TOTAL_ENERGY_WHITESPACE);

                    /* read the two blank lines */

                    bufferedReader.readLine();
                    bufferedReader.readLine();
                    line = bufferedReader.readLine();
                    if (line.contains("ENERGY CHANGE")) {
                        type = Type.OPTIMIZATION;

                        /* Get the information from the first auxiliary column, which is the line
                           we have just read. Add the comma beforehand. Skip the beginning whitespace.
                         */

                        csvString[TOTAL_INDEX] += ",";
                        for (int i = this.TOTAL_ENERGY_WHITESPACE; i < line.length(); i++) {
                            if (line.charAt(i) != ' ') {
                                csvString[TOTAL_INDEX] += line.charAt(i);
                            }
                        }
                        csvString[TOTAL_INDEX] += ",";

                        /* Get the information for the second auxiliary column as normal */

                        csvString[TOTAL_INDEX] += getLineInformation(bufferedReader, this.TOTAL_ENERGY_WHITESPACE);
                        csvString[TOTAL_INDEX] += ",";

                        /* Get the information for the last auxiliary column and add a newline */

                        csvString[TOTAL_INDEX] += getLineInformation(bufferedReader, this.TOTAL_ENERGY_WHITESPACE);
                        csvString[TOTAL_INDEX] += "\n";
                    } else if (line.contains("KINETIC ENERGY")) {
                        type = Type.MOLECULAR;

                        /* Get the information from the first auxiliary column, which is the line
                           we have just read. Add the comma beforehand. Skip the beginning whitespace.
                         */

                        csvString[TOTAL_INDEX] += ",";
                        for (int i = this.TOTAL_ENERGY_WHITESPACE; i < line.length(); i++) {
                            if (line.charAt(i) != ' ') {
                                csvString[TOTAL_INDEX] += line.charAt(i);
                            }
                        }
                        csvString[TOTAL_INDEX] += ",";

                        /* Get the information for the second auxiliary column as normal */

                        csvString[TOTAL_INDEX] += getLineInformation(bufferedReader, this.TOTAL_ENERGY_WHITESPACE);
                        csvString[TOTAL_INDEX] += ",";

                        /* Get the information for the last auxiliary column and add a newline */

                        csvString[TOTAL_INDEX] += getLineInformation(bufferedReader, this.TOTAL_ENERGY_WHITESPACE);
                        csvString[TOTAL_INDEX] += "\n";
                    } else {
                        csvString[TOTAL_INDEX] += "\n";
                    }
                }
            }
            bufferedReader.close();
            fileReader.close();
            if (type == Type.REGULAR) {
                csvString[TOTAL_INDEX] = "Geometry #,Electrostatic,Polarization,Dispersion,Exchange-repusion,Point charges," +
                        "Charge-penetration,Total\n" + csvString[TOTAL_INDEX];
            } else if (type == Type.OPTIMIZATION) {
                csvString[TOTAL_INDEX] = "Geometry #,Electrostatic,Polarization,Dispersion,Exchange-repusion,Point charges," +
                        "Charge-penetration,Total,Energy Change,RMS Gradient,Maximum Gradient\n" + csvString[TOTAL_INDEX];
            } else {
                csvString[TOTAL_INDEX] = "Geometry #,Electrostatic,Polarization,Dispersion,Exchange-repusion,Point charges," +
                        "Charge-penetration,Total,Kinetic Energy,Invariant,Temperature (K)\n" + csvString[TOTAL_INDEX];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvString;
    }

    private String getLineInformation(BufferedReader bufferedReader, int whitespace) throws IOException {
        bufferedReader.skip(whitespace);
        String line = bufferedReader.readLine();
        if (line.charAt(0) == ' ') {
            return line.substring(1);
        } else {
            return line;
        }
    }
}