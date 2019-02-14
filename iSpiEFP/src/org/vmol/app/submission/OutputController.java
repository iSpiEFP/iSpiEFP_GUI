package org.vmol.app.submission;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolSelectionListener;
import org.jmol.java.BS;
import org.jmol.viewer.Viewer;
import org.openscience.jmol.app.jmolpanel.JmolPanel;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class OutputController {
    
    private Viewer jmolViewer;
    private JFrame jmolWindow;
    private String type;

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
    
    public void initialize(String output, String type) {
        this.type = type;
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(5);
 
        Label label = new Label("Libefp Output:");
        TextArea textArea = new TextArea();
        textArea.setScaleShape(true);
        textArea.setText(output);
        textArea.setPrefHeight(700);
        
        // To contain the buttons
        HBox buttonBar = new HBox();
 
        // Button to Append text
        Button buttonDownload = new Button("Download");
        buttonDownload.setDisable(true);
        buttonDownload.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("download");
                download();
            }
        });
 
        Button buttonVisualize = new Button("Visualize");
        buttonVisualize.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("visualize this stuff");
                visualize(output);
            }
        });
        
        Button buttonAnalyze = new Button("Analyze");
        buttonAnalyze.setDisable(true);
        buttonAnalyze.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("analyze");
                //visualize(output);
            }
        });
 
        buttonBar.getChildren().addAll(buttonDownload, buttonVisualize, buttonAnalyze);
        root.getChildren().addAll(label, textArea, buttonBar);
        Scene scene = new Scene(root, 520, 520);
        Stage newStage = new Stage();
        
        newStage.setTitle("JavaFX Libefp Output");
        newStage.setScene(scene);
        newStage.show();
        
        System.out.println("output controller ready");
    }
    
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
    
    private void download() {
        System.out.println("downloading molecule");
    }
    
    private String getXYZfromEFP(String output){
        BufferedReader br = new BufferedReader(new StringReader(output));
        StringBuilder sb = new StringBuilder();
        String line;
        int lineCount = 0;
        try {
            while ((line = br.readLine()) != null) {
                String triggerLine = new String();
                if(this.type.equals("LIBEFP")){
                    triggerLine = "    GEOMETRY (ANGSTROMS)";
                } else if(this.type.equals("GAMESS")) {
                    triggerLine = " COORDINATES (BOHR)";
                }
                if (line.startsWith(triggerLine)) {
                    line = br.readLine();
                    if(triggerLine.startsWith("    GEOMETRY (ANGSTROMS)")){
                        line = br.readLine();
                    }
                    //line = br.readLine();
                    while (line != null && !line.isEmpty() && !line.startsWith(" STOP")) {
                        String[] tokens = line.split("\\s+");
                        if(tokens[0].charAt(0) == 'A' && !tokens[0].endsWith("H000")) {
                            char atom_symbol = (tokens[0].charAt(tokens[0].length()-1));
                            sb.append(atom_symbol);
                            sb.append("  " + tokens[1] + "  " + tokens[2] + "  " + tokens[3] + "\n");
                            lineCount++;
                        }
                       
                        System.out.println(line);
                        line = br.readLine();
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sb.insert(0, Integer.toString(lineCount)+"\n\n");
        
        System.out.println("line ocunt:"+Integer.toString(lineCount));
        System.out.println("parsed xyz");
        System.out.println(sb.toString());
        return sb.toString();
    }
}
