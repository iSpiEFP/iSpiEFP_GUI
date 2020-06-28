package org.ispiefp.app.EFPFileRetriever;

import java.io.*;

public class LibEFPtoCSV {
    //private final String[] files = {"opt_1.out", "pbc_1.out", "w6b2_energy.out"};
    private final int ENERGIES = 6; // the number of different energy calculations, excluding total
    private final int WHITESPACE = 34;
    private final int AUXILIARY = 3; // the auxiliary columns that may have additional information

    /* This method takes as a parameter a file name and gets the necessary information to create a string to write to
       a CSV file. The string is in the format "geometry_number,electrostatic,polarization,dispersion,
       exchange_repulsion,point_charges,charge_penetration,total,identifier," followed by either "-1,-1,-1\n" (empty),
        "energy_change,RMS_gradient,maximum_gradient\n" (geometry optimization), or
        "kinetic_energy,invariant,temperature\n" (molecular dynamics)
     */

    public LibEFPtoCSV() {
        System.out.println("Preparing to convert files");
    }

    public String getCSVString(String fileName) {
        String csvString = new String();
        File file = new File(fileName);
        int geometries = 0;
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while (true) {
                line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }

                /* Get the energy results once we reach the key line */

                if (line.contains("ENERGY COMPONENTS")) {

                    /* add to the first column the iteration and a comma */

                    csvString += geometries;
                    geometries++;
                    csvString += ",";
                    bufferedReader.readLine();    // read the empty line

                   /* repeat the following process to get the energy for each component (6 times)
                      Electrostatic, polarization, dispersion, exchange repulsion, point charges, and charge penetration
                    */

                    for (int i = 0; i < this.ENERGIES; i++) {
                        csvString += getLineInformation(bufferedReader);
                        csvString += ",";
                    }
                    bufferedReader.readLine(); // read the empty line

                    /* get the total energy */

                    csvString += getLineInformation(bufferedReader);
                    csvString += ",";

                    /* read the two blank lines */

                    bufferedReader.readLine();
                    bufferedReader.readLine();
                    line = bufferedReader.readLine();

                   /* Depending on the following lines, get the correct information and signify what the columns
                      correspond to:
                      0 = no additional information
                      1 = geometry optimization
                      2 = molecular dynamics
                      These values are the identifier descibed above
                    */

                    if (line.contains("ENERGY CHANGE")) {
                        csvString += "1,";

                        /* Get the information from the first auxiliary column, which is the line
                           we have just read. Skip the beginning whitespace.
                         */

                        for (int i = this.WHITESPACE; i < line.length(); i++) {
                            if (line.charAt(i) != ' ') {
                                csvString += line.charAt(i);
                            }
                        }
                        csvString += ",";

                        /* Get the information for the second auxiliary column as normal */

                        csvString += getLineInformation(bufferedReader);
                        csvString += ",";

                        /* Get the information for the last auxiliary column and add a newline */

                        csvString += getLineInformation(bufferedReader);
                        csvString += "\n";
                    } else if (line.contains("KINETIC ENERGY")) {
                        csvString += "2,";

                        /* Get the information from the first auxiliary column, which is the line
                           we have just read. Skip the beginning whitespace.
                         */

                        for (int i = this.WHITESPACE; i < line.length(); i++) {
                            if (line.charAt(i) != ' ') {
                                csvString += line.charAt(i);
                            }
                        }
                        csvString += ",";

                        /* Get the information for the second auxiliary column as normal */

                        csvString += getLineInformation(bufferedReader);
                        csvString += ",";

                        /* Get the information for the last auxiliary column and add a newline */

                        csvString += getLineInformation(bufferedReader);
                        csvString += "\n";
                    } else {
                        csvString += "0,-1,-1,-1\n";
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvString;
    }

    private String getLineInformation(BufferedReader bufferedReader) throws IOException {
        bufferedReader.skip(this.WHITESPACE);
        String line = bufferedReader.readLine();
        if (line.charAt(0) == ' ') {
            return line.substring(1);
        } else {
            return line;
        }
    }
}