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

package org.ispiefp.app.jobSubmission;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.ispiefp.app.Main;
import org.ispiefp.app.visualizer.ViewerHelper;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.viewer.Viewer;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Random;
import java.util.Scanner;

/**
 * Job Submission Ouput Controller loads the output GUI, which allows visualization, downloads, and analyzation
 */
public class OutputController {

    private Viewer jmolViewer;
    private JFrame jmolWindow;
    private String type;
    private static String current_tab = "OUTPUT";

    private static final String OUTPUT = "OUTPUT";
    private static final String LOG = "LOG";

    public OutputController() {

    }

    static class JmolPanel extends JPanel {

        /**
         *
         */
        private static final long serialVersionUID = -3661941083797644242L;
        static Viewer viewer;

        private final Dimension currentSize = new Dimension();
        private OutputController controller;

        JmolPanel(OutputController controller) {
            this.controller = controller;
            viewer = (Viewer) Viewer.allocateViewer(this, new SmarterJmolAdapter(),
                    null, null, null, null, null);
            viewer.setAnimationFps(60);
        }

        @Override
        public void paint(Graphics g) {
            getSize(currentSize);
            viewer.renderScreenImage(g, currentSize.width, currentSize.height);
        }
    }

    public void initialize(String output, String log, String type) {
        this.type = type;
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(5);

        Label label = new Label(type + " Output:");
        TextArea outputTextArea = new TextArea();
        outputTextArea.setScaleShape(true);
        outputTextArea.setText(output);
        outputTextArea.setPrefHeight(700);

        Label label2 = new Label(type + " Log:");
        TextArea logTextArea = new TextArea();
        logTextArea.setScaleShape(true);
        logTextArea.setText(log);
        logTextArea.setPrefHeight(700);

        // To contain the buttons
        HBox buttonBar = new HBox();

        /**
         * User wants to download the selected file
         */
        Button buttonDownload = new Button("Download");
        buttonDownload.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("download");
                if (current_tab.equals(OUTPUT)) {
                    try {
                        download(output);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (current_tab.equals(LOG)) {
                    try {
                        download(log);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        /**
         * User wants to visualize the selected file
         */
        Button buttonVisualize = new Button("Visualize");
        buttonVisualize.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("visualize this stuff");
                visualize(output);
            }
        });

        /**
         * User wants to anaylze the selected file
         * TODO
         */
        Button buttonAnalyze = new Button("Analyze");
        buttonAnalyze.setDisable(true);
        buttonAnalyze.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("analyze");
                //visualize(output);
                //TODO
            }
        });

        // create a tabpane 
        TabPane tabpane = new TabPane();

        // create Tab 
        Tab outputTab = new Tab("Output");
        outputTab.setClosable(false);
        outputTab.setContent(outputTextArea);

        Tab logTab = new Tab("Log");
        logTab.setClosable(false);
        logTab.setContent(logTextArea);

        //Handle Tabs 
        EventHandler<Event> event = new EventHandler<Event>() {
            public void handle(Event e) {
                if (outputTab.isSelected()) {
                    buttonVisualize.setDisable(false);
                    current_tab = OUTPUT;

                } else if (logTab.isSelected()) {
                    buttonVisualize.setDisable(true);
                    current_tab = LOG;

                }
            }
        };
        outputTab.setOnSelectionChanged(event);
        logTab.setOnSelectionChanged(event);


        // add tab 
        tabpane.getTabs().addAll(outputTab, logTab);


        buttonBar.getChildren().addAll(buttonDownload, buttonVisualize, buttonAnalyze);
        root.getChildren().addAll(label, tabpane, buttonBar);
        Scene scene = new Scene(root, 520, 520);
        Stage newStage = new Stage();

        newStage.setTitle("JavaFX Libefp Output");
        newStage.setScene(scene);
        newStage.show();

        System.out.println("output controller ready");
    }

    /**
     * Load a jmol output viewer in a seperate window
     * @param output xyz file content to be viewed
     */
    private void visualize(String output) {
        System.out.println("visualizing molecule");
        String coords = getXYZfromEFP(output);

        JmolPanel jmolPanel = new JmolPanel(this);
        jmolViewer = jmolPanel.viewer;

        jmolWindow = new JFrame("Visualization");
        Container contentPane = jmolWindow.getContentPane();
        JPanel panel = new JPanel();
        panel.setLayout(new CardLayout());
        panel.add(jmolPanel);
        contentPane.add(panel);

        //open file coords in viewer
        jmolPanel.viewer.openStringInline(coords);

        jmolWindow.setSize(600, 600);
        jmolWindow.setVisible(true);
    }

    /**
     * Download the file into the specified location
     * @param content content to be downloaded
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void download(String content) throws UnsupportedEncodingException, FileNotFoundException, IOException {

        String filename = "";

        Random ran = new Random();
        int x = ran.nextInt(999999);
        filename += Integer.toString(x);

        if (current_tab.equals(LOG)) {
            filename += ".log";
        } else {
            filename += ".out";
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Specify a file to save");
        fileChooser.setInitialFileName(filename);


        File userSelection = fileChooser.showSaveDialog(Main.getPrimaryStage());

        if (userSelection != null) {
            File fileToSave = userSelection.getAbsoluteFile();
            System.out.println("Save as file: " + fileToSave.getAbsolutePath());

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileToSave.getAbsolutePath()), "utf-8"))) {

                Scanner scanner = new Scanner(content);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    // process the line
                    writer.write(line);
                    writer.newLine();
                }
                scanner.close();
                writer.close();
            }
        }
    }

    /**
     * Get XYZ file from EFP file by parsing it
     * @param output efp file content
     * @return xyz file content
     */
    private String getXYZfromEFP(String output) {
        BufferedReader br = new BufferedReader(new StringReader(output));
        StringBuilder sb = new StringBuilder();
        String line;
        int lineCount = 0;
        try {
            while ((line = br.readLine()) != null) {
                String triggerLine = new String();
                if (this.type.equals("LIBEFP")) {
                    triggerLine = "    GEOMETRY (ANGSTROMS)";
                } else if (this.type.equals("GAMESS")) {
                    triggerLine = " COORDINATES (BOHR)";
                }
                if (line.startsWith(triggerLine)) {
                    line = br.readLine();
                    if (triggerLine.startsWith("    GEOMETRY (ANGSTROMS)")) {
                        line = br.readLine();
                        //line = br.readLine();
                        while (line != null && !line.isEmpty() && !line.startsWith(" STOP")) {
                            String[] tokens = line.split("\\s+");
                            if (tokens[0].charAt(0) == 'A' && !tokens[0].endsWith("H000")) {
                                char atom_symbol = (tokens[0].charAt(tokens[0].length() - 1));
                                sb.append(atom_symbol);

                                sb.append("  " + tokens[1] + "  " + tokens[2] + "  " + tokens[3] + "\n");
                                lineCount++;
                            }

                            System.out.println(line);
                            line = br.readLine();
                        }
                    } else if (triggerLine.startsWith(" COORDINATES (BOHR)")) {
                        while (line != null && !line.isEmpty() && !line.startsWith(" STOP")) {
                            String[] tokens = line.split("\\s+");
                            if (tokens[0].charAt(0) == 'A' && !tokens[0].endsWith("H000")) {
                                char atom_symbol = (tokens[0].charAt(tokens[0].length() - 1));
                                sb.append(atom_symbol);
                                //convert tokens to angstroms
                                double x_coord = ViewerHelper.convertBohrToAngstrom(Double.parseDouble(tokens[1]));
                                double y_coord = ViewerHelper.convertBohrToAngstrom(Double.parseDouble(tokens[2]));
                                double z_coord = ViewerHelper.convertBohrToAngstrom(Double.parseDouble(tokens[3]));

                                sb.append("  " + x_coord + "  " + y_coord + "  " + z_coord + "\n");
                                lineCount++;
                            }

                            System.out.println(line);
                            line = br.readLine();
                        }
                    }

                }
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sb.insert(0, Integer.toString(lineCount) + "\n\n");

        System.out.println("line ocunt:" + Integer.toString(lineCount));
        System.out.println("parsed xyz");
        System.out.println(sb.toString());
        return sb.toString();
    }
}
