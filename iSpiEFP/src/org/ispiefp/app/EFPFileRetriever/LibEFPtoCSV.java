package org.ispiefp.app.EFPFileRetriever;

import java.io.*;

public class LibEFPtoCSV {
    //private final String[] files = {"opt_1.out", "pbc_1.out", "w6b2_energy.out"};
    private final int ENERGIES = 6; // the number of different energy calculations, excluding total
    private final int AUXILIARY = 3; // the auxiliary columns that may have additional information

    /* This method takes as a parameter a file name and gets the necessary information to create a string to write to
       a CSV file. The string is in the format "geometry_number,electrostatic,polarization,dispersion,
       exchange_repulsion,point_charges,charge_penetration,total,identifier," followed by either "-1,-1,-1\n" (empty),
        "energy_change,RMS_gradient,maximum_gradient\n" (geometry optimization), or
        "kinetic_energy,invariant,temperature\n" (molecular dynamics)
     */

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
                   csvString += ",";
                   bufferedReader.readLine();    // read the empty line
                   /* repeat the following process to get the energy for each component (6 times)
                      Electrostatic, polarization, dispersion, exchange repulsion, point charges, and charge penetration
                    */

                   for (int i = 0; i < this.ENERGIES; i++) {
                       line = bufferedReader.readLine();
                       String[] components = line.split(" ");
                       for (int j = 0; j < components.length; i++) {
                           try {
                               Integer.parseInt(components[j]);
                               csvString += components[j];
                               csvString += ",";
                           } catch (NumberFormatException e) {
                               //continue;
                           }
                       }
                   }
                   bufferedReader.readLine(); // read the empty line

                   /* get the total energy */

                   line = bufferedReader.readLine();
                   String[] components = line.split(" ");
                   for (int i = 0; i < components.length; i++) {
                       try {
                           Integer.parseInt(components[i]);
                           csvString += components[i];
                           csvString += ",";
                       } catch (NumberFormatException e) {
                           //e.printStackTrace();
                       }
                   }

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
                       for (int i = 0; i < AUXILIARY; i++) {
                            line = bufferedReader.readLine();
                            String[] auxiliaryColumns = line.split(" ");

                            /* Get the information from the auxiliary columns and add commas, except for the last,
                               as we need to add a newline character manually
                             */
                           for (int j = 0; j < auxiliaryColumns.length - 1; j++) {
                               try {
                                   Integer.parseInt(auxiliaryColumns[j]);
                                   csvString += auxiliaryColumns[j];
                                   csvString += ",";
                               } catch (NumberFormatException e) {
                                   //e.printStackTrace();
                               }
                           }
                           line = bufferedReader.readLine();
                           String[] finalColumn = line.split(" ");
                           for (int j = 0; j < finalColumn.length; j++) {
                               try {
                                   Integer.parseInt(finalColumn[j]);
                                   csvString += finalColumn[j];
                                   csvString += "\n";
                               } catch (NumberFormatException e) {
                                   //e.printStackTrace();
                               }
                           }
                       }
                   } else if (line.contains("KINETIC ENERGY")) {
                        csvString += "2,";
                       for (int i = 0; i < AUXILIARY; i++) {
                           line = bufferedReader.readLine();
                           String[] auxiliaryColumns = line.split(" ");

                            /* Get the information from the auxiliary columns and add commas, except for the last,
                               as we need to add a newline character manually
                             */
                           for (int j = 0; j < auxiliaryColumns.length - 1; j++) {
                               try {
                                   Integer.parseInt(auxiliaryColumns[j]);
                                   csvString += auxiliaryColumns[j];
                                   csvString += ",";
                               } catch (NumberFormatException e) {
                                   //e.printStackTrace();
                               }
                           }
                           line = bufferedReader.readLine();
                           String[] finalColumn = line.split(" ");
                           for (int j = 0; j < finalColumn.length; j++) {
                               try {
                                   Integer.parseInt(finalColumn[j]);
                                   csvString += finalColumn[j];
                                   csvString += "\n";
                               } catch (NumberFormatException e) {
                                   //e.printStackTrace();
                               }
                           }
                       }
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
}
