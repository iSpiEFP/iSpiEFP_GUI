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

package org.ispiefp.app.visualizer;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.ispiefp.app.fileparser.FileParserController;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.util.Logger;
import org.jmol.viewer.Viewer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * JmolPanel holds the Jmol Viewer Object for the Jmol.jar, wraps it to work
 * with JavaFX, and runs the pane. 
 *
 * Example Usage: Given a Pane pane, which is a desired pane for the Jmol Viewer to be,
 * simply call: JmolPanel jmolPanel = new JmolPanel(pane);
 *
 * To use Jmol options use the "viewer" class member. 
 * Example Usage: For accessing the Jmol modelSet, "jmolPanel.viewer.ms"
 *
 * @author addison
 *
 */
public class JmolPanel extends JPanel {
    /**
     * Generate Serial Version UID Version
     */
    private static final long serialVersionUID = 2183410255641936515L;

    public Viewer viewer;       //jmol Viewer object
    protected Pane parentPane;  //pane which contains jmol viewer object

    protected final MySwingNode swingNode = new MySwingNode();      //java swing node wrapper for javaFX

    protected double width;
    protected double height;

    /**
     * Allocate a Jmol Viewer Object, 
     * place it in the pane location, 
     * set the default size to the pane dimensions,
     * and run the Jmol Viewer Object on a thread.
     *
     * @param pane : container for Jmol Viewer Object
     */
    public JmolPanel(Pane pane) {
        //allocate a Jmol Viewer
        viewer = (Viewer) Viewer.allocateViewer(this, new SmarterJmolAdapter(),
                null, null, null, null, null);
        viewer.setAnimationFps(60);

        //place 
        this.parentPane = pane;
        this.width = pane.getWidth();
        this.height = pane.getHeight();

        //set JPanel initial size
        this.setPreferredSize(new Dimension((int)width, (int)height));

        //set SwingNode initial size
        this.swingNode.resize(width, height);
        pane.getChildren().add(swingNode);

        //add height listener to change size during height change
        pane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldPaneHeight, Number newPaneHeight) {
                //height changed
                height = newPaneHeight.doubleValue();
                updateJmolSize(width, height);
            }
        });
        //add width listener to change size during width change
        pane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldPaneWidth, Number newPaneWidth) {
                //width changed
                width = newPaneWidth.doubleValue();
                updateJmolSize(width, height);
            }
        });

        //run on thread
        runJmolViewer();
    }

    private void updateJPanelSize() {
        this.setPreferredSize(new Dimension((int) width, (int) height));

    }

    private void updateSwingNodeSize() {
        this.swingNode.resize(width, height);
        this.repaint();

    }

    private void updateJmolSize(double width, double height) {
        this.setPreferredSize(new Dimension((int) width, (int) height));
        this.swingNode.resize(width, height);
        this.repaint();
    }

    /**
     * @return this JmolPanel Object
     */
    public JmolPanel getJmolPanel() {
        return this;
    }

    /**
     * @return this swingNode
     */
    public SwingNode getSwingNode() {
        return this.swingNode;
    }

    /**
     * @return the Parent Pane holding the Jmol Viewer
     */
    public Pane getParentPane() {
        return this.parentPane;
    }

    /**
     * Set the pane for the Jmol viewer to be placed
     */
    public void setParentPane(Pane pane) {
        this.parentPane = pane;
        parentPane.getChildren().add(swingNode);
    }

    /**
     * A Safer way of opening a file than "viewer.openFile()"
     *
     * @throws IOException
     */
    public boolean openFile(File file) throws IOException {
        if(file == null) {
            System.err.println("Jmol Viewer IO error: reading a null file.");
            return false;
        }

        String fileName = file.getName();
        String strError = new String();
        if (fileName.contains("xyz") || fileName.contains("pdb")) {
            if ((strError = viewer.openFile(file.getAbsolutePath())) != null) {
                Logger.error("Error while loading XYZ file. " + strError);
                return false;
            }
            return true;
        } else {
            openFileParserWindow(file);
        }
        return false;
    }

    /**
     * Open the file parser window if a file fails
     *
     * @param file
     * @throws IOException
     */
    protected void openFileParserWindow(File file) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/FileParser.fxml"));
        Parent fileParser = loader.load();
        Parent root = new StackPane();

        Stage stage = new Stage();
        stage.setTitle(file.getName());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(root.getScene().getWindow());
        stage.setScene(new Scene(fileParser));

        // Set the file into the controller
        FileParserController controller = loader.getController();
        controller.setFile(file);

        try {
            stage.showAndWait();
        } catch (Exception e) {
            System.err.println("ERROR WITH STAGE SHOWING AND WAITING");
        }
    }

    /**
     * Run the Jmol Viewer Object on a separate thread
     */
    private void runJmolViewer() {
        JmolPanel jmolPanel = this;

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                panel.add("North", jmolPanel);
                panel.setFocusable(true);
                swingNode.setContent(panel);
            }
        });
    }

    /**
     * Override JPanel Paint to also render Jmol Viewer Object
     *
     * @param g : Graphics Param from JPanel
     */
    @Override
    public void paint(Graphics g) {
        //render Jmol Viewer with these dimensions
        viewer.renderScreenImage(g, (int)width, (int)height);
    }
}
