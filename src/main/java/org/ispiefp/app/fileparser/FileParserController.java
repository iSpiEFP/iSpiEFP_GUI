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

package org.ispiefp.app.fileparser;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * ViewController.java (UTF-8)
 * <p>
 * Aug 26, 2013
 *
 * @author tarrsalah.org
 */
public class FileParserController implements Initializable {

    @FXML
    private Parent root;

    @FXML
    private TextField textField;

    private File file;

    @FXML
    private TextArea linesTextArea;

    private List<Integer> startingIndexes;

    private String prevSearchQuery = "";

    private Integer prevShownIndex = -1;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        linesTextArea.setEditable(false);
        // System.out.println(javafx.scene.text.Font.getFamilies());
        linesTextArea.setFont(Font.font("Courier New", 14));
        startingIndexes = new ArrayList<>();
    }

    @FXML
    public void showFileLines() throws InterruptedException, ExecutionException {
        if (textField.getText() != null && !textField.getText().isEmpty()) {
            String currSearchQuery = textField.getText().toLowerCase();
            if (!currSearchQuery.equalsIgnoreCase(prevSearchQuery)) {
                calculateAllOccurences(currSearchQuery);
            }
            if (!startingIndexes.isEmpty()) {
                int currIndex = prevShownIndex != -1 ? (prevShownIndex + 1) % (startingIndexes.size()) : 0;
                int searchIndex = startingIndexes.get(currIndex);
                linesTextArea.selectRange(searchIndex, searchIndex + textField.getLength());
                prevShownIndex = currIndex;
            } else {
                System.out.println("No matching text found");
            }
            prevSearchQuery = currSearchQuery;
        } else {
            System.out.println("Missing search key");
        }
    }

    private void calculateAllOccurences(String currSearchQuery) {
        String fileContents = linesTextArea.getText().toLowerCase();
        int lastIndex = 0;
        // Reset the prev stored stuff if any
        startingIndexes.clear();
        prevShownIndex = -1;
        while (lastIndex != -1) {
            lastIndex = fileContents.indexOf(currSearchQuery, lastIndex);
            if (lastIndex != -1) {
                startingIndexes.add(lastIndex);
                lastIndex += currSearchQuery.length();
            }
        }
    }

    public List<String> read(File file) {
        List<String> lines = new ArrayList<String>();
        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            br.close();
        } catch (IOException ex) {
            System.out.println("Unable to parse the file!");
        }
        return lines;
    }

    public void setFile(File file) {
        this.file = file;
        readFileAndSetLinesTextArea();
    }

    private void readFileAndSetLinesTextArea() {
        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                linesTextArea.appendText(line + "\n");
            }
            br.close();
        } catch (IOException ex) {
            System.out.println("Unable to parse the file!");
        }
    }

    @FXML
    private void showVisualization() {
        List<StringBuilder> geometries = parseGeometries();
        List<File> files = createTemporaryFiles(geometries);
        Stage currStage = (Stage) root.getScene().getWindow();
        //JmolVisualization jmolVisualization = new JmolVisualization(currStage, false);
        //jmolVisualization.showMultipleFiles(files);
    }

    private List<File> createTemporaryFiles(List<StringBuilder> geometries) {
        List<File> tempFiles = new ArrayList<>();
        int i = 0;
        for (StringBuilder geometry : geometries) {
            try {
                //create a temp file
                File file = File.createTempFile("temp_vmol_geometry_file" + i++, ".tmp");
                file.deleteOnExit();
                //write to the file
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write(geometry.toString());
                bw.close();
                tempFiles.add(file);
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        return tempFiles;
    }

    private List<StringBuilder> parseGeometries() {
        List<StringBuilder> list = new ArrayList<>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("GEOMETRY (ANGSTROMS)")) {
                    System.out.println("Found Geometry");
                    StringBuilder sb = new StringBuilder();
                    int atomsCount = 0;
                    int emptyLinesCount = 0;
                    while (scanner.hasNextLine() && emptyLinesCount < 2) {
                        String tempLine = scanner.nextLine();
                        if (tempLine.isEmpty()) emptyLinesCount++;
                        else {
                            // Remove the first three characters from each geometry line
                            tempLine = tempLine.substring(3);
                            atomsCount++;
                        }
                        sb.append(tempLine + "\n");
                    }
                    sb.insert(0, String.valueOf(atomsCount) + "\n");
                    System.out.println("Printing the parsed geometry");
                    System.out.println(sb.toString());
                    list.add(sb);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        } finally {
            if (scanner != null) scanner.close();
        }
        return list;
    }
}