package org.ispiefp.app.util;

import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class EFPFileParser {

    //This stuff
    Double totalElectrostatic = 0.0;
    ArrayList<Double> efpOutValueList;
    String currStr;

    public ArrayList<Double> getEFPValues(String filePath) {

           ReversedLinesFileReader fr = null;

           try {
               fr = new ReversedLinesFileReader(new File(filePath), Charset.defaultCharset());
           }
           catch (Exception e) {
               System.out.println("Rev Reader Error");
               e.printStackTrace();
           }
           do {
               try {
                   currStr = fr.readLine();
               }
               catch (IOException e) {
                   System.out.println("File not found");
                   e.printStackTrace();
               }
               if (currStr.contains("TOTAL ENERGY")) {
                   String totalEStr = currStr.substring(currStr.lastIndexOf(' ') + 1);
                   Double totalDoub = Double.parseDouble(totalEStr);
                   efpOutValueList.add(totalDoub);
               }

               if (currStr.contains("ELECTROSTATIC ENERGY")) {
                   String totalEStr = currStr.substring(currStr.lastIndexOf(' ') + 1);
                   Double estatDoub = Double.parseDouble(totalEStr);
                   totalElectrostatic += estatDoub;
                   efpOutValueList.add(totalElectrostatic);
               }

               if (currStr.contains("EXCHANGE REPULSION ENERGY")) {
                   String exchRepStr = currStr.substring(currStr.lastIndexOf(' ') + 1);
                   Double exchRepDoub = Double.parseDouble(exchRepStr);
                   efpOutValueList.add(exchRepDoub);
               }

               if (currStr.contains("DISPERSION ENERGY")) {
                   String dispersStr = currStr.substring(currStr.lastIndexOf(' ') + 1);
                   Double dispersDoub = Double.parseDouble(dispersStr);
                   efpOutValueList.add(dispersDoub);
               }

               if (currStr.contains("POLARIZATION ENERGY")) {
                   String dispersStr = currStr.substring(currStr.lastIndexOf(' ') + 1);
                   Double dispersDoub = Double.parseDouble(dispersStr);
                   efpOutValueList.add(dispersDoub);
               }

               if (currStr.contains("CHARGE PENETRATION ENERGY")) {
                   String chargePenStr = currStr.substring(currStr.lastIndexOf(' ') + 1);
                   Double chargePenDoub = Double.parseDouble(chargePenStr);
                   totalElectrostatic += chargePenDoub;

               }


       }
           while (currStr != null) ;

        return efpOutValueList;
    }
}
