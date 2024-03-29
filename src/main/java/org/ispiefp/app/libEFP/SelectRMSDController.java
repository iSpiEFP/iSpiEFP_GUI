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

package org.ispiefp.app.libEFP;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.ispiefp.app.MetaData.MetaData;
import org.ispiefp.app.visualizer.JmolMainPanel;
import org.ispiefp.app.visualizer.JmolPanel;
import org.jmol.modelset.Atom;
import org.jmol.viewer.Viewer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Class written by Ryan DeRue
 * Terminology:
 *  1. Viewer Fragment - Refers to the fragments in the user's system which may or may not have already been assigned
 *     parameters. These are not library fragments, they are what appear in the main jmol panel of the program.
 *  2. Library Fragment - Refers to the fragments contained within the parameter repo for iSpiEFP. These fragments
 *     are defined by metaDatas and contain the parameters the user is going to select.
 * Class is responsible for allowing the user to select which parameters they want to use for a given viewer fragment.
 */
public class SelectRMSDController {

    /* The table from which the user will be selecting the parameters they want to use for a given viewer fragment. *
     *  Has 3 columns:                                                                                               *
     *   1. The name of the fragment as defined by the metaData                                                      *
     *   2. The file name which contains the parameters. Can be a local file or a remote file                        *
     *   3. The RMSD computed between this library fragment and the current viewer fragment                          */

    @FXML
    private TableView<MetaData> libraryFragments;

    @FXML
    private TableColumn fragmentName;

    @FXML
    private TableColumn fragmentFile;

    @FXML
    private TableColumn RMSD;

    /* The box which will contain a preview of the fragment for which the user is selecting parameters */
    @FXML
    private VBox previewBox;

    /* The pane which will be updated with the previewPanel below */
    @FXML
    private Pane previewPane;

    @FXML
    private Button selectButton;

    /* The panel which is updated with the preview of the fragment for which the user is selecting parameters */
    private JmolPanel previewPanel;

    /* Index into validIndices of current viewer fragment for which the user is selecting parameters */
    private int currentViewerFragmentIndex;

    /* Array of indices for viewerFragments that have more than one RMSD to select from */
    private ArrayList<Integer> validIndices;

    /* Maps the viewerFragment index of a fragment to a map whose keys are the metadata of a library fragment   *
     * that matches on chemical formula and whose values are the computed RMSD of this fragment against         *
     * the viewerFragment as a String.                                                                          */
    private Map<Integer, Map<MetaData, String>> viewerFragmentMap;

    /* Represents the keyset of the Inner map in the viewerFragmentMap for the index of                         *
     * currentViewerFragmentIndex                                                                               */
    private ArrayList<MetaData> currentMetaDatas;

    /* Will contain all of the metadatas selected by the user mapped by the viewerFragment index */
    private Map<Integer, MetaData> selectedMetaDatas;

    private ObservableList<MetaData> fragmentObservableList = FXCollections.observableArrayList();

    /* Jmol uses a strange internal representation for fragments in the viewer where the index of the outer     *
       array list represents the fragments and the indices of the inner array list are the atoms                */
    private ArrayList<ArrayList<Integer>> viewerFragments;

    /* This Jmol viewer is the one from the main iSpiEFP viewer and is in no way related to the preview pane.   */
    private Viewer mainJmolViewer;

    private Stage currentStage;

    private boolean alwaysLowest;

    public SelectRMSDController(){
        super();
        currentViewerFragmentIndex = 0;
        selectedMetaDatas = new HashMap<>();
        currentMetaDatas = new ArrayList<>();
        //offerNextFragmentSelection();
    }

    public void initialize() {
        /* Set up table columns */
        if (validIndices != null && currentViewerFragmentIndex == validIndices.size()){
            currentStage.close();
            return;
        }
        fragmentName.setCellValueFactory(new PropertyValueFactory<MetaData, String>("fragmentName"));
        fragmentFile.setCellValueFactory(new PropertyValueFactory<MetaData, String>("fromFile"));
        RMSD.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MetaData, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures param) {
                MetaData md = (MetaData) param.getValue();
                return new ObservableValue() {
                    @Override
                    public void addListener(ChangeListener listener) {

                    }

                    @Override
                    public void removeListener(ChangeListener listener) {

                    }

                    @Override
                    public String getValue() {
                        return viewerFragmentMap.get(validIndices.get(currentViewerFragmentIndex)).get(md);
                    }

                    @Override
                    public void addListener(InvalidationListener listener) {

                    }

                    @Override
                    public void removeListener(InvalidationListener listener) {

                    }
                };
            }
        });

        /* Create a new JMolPanel with which we can display the current viewer fragment */
        previewPanel = new JmolMainPanel(previewPane, new ListView<>());

        selectButton.setOnAction(event -> handleSelection());

//        offerNextFragmentSelection();

    }

    public void handleSelection(){
        System.out.printf("Viewer Fragment Index is %d%n", currentViewerFragmentIndex);
        selectedMetaDatas.put(validIndices.get(currentViewerFragmentIndex),
                libraryFragments.getSelectionModel().getSelectedItem());
        libraryFragments.getItems().clear();
        fragmentObservableList.clear();
        currentMetaDatas.clear();
        currentViewerFragmentIndex++;
        offerNextFragmentSelection();
    }

    /**
     * Updates the view to handle the user's selection for the next viewerFragment
     */
    public void offerNextFragmentSelection(){
        if(currentViewerFragmentIndex == validIndices.size()){
            currentStage.close();
            return;
        }
        int viewerFragIdx = validIndices.get(currentViewerFragmentIndex);
        currentMetaDatas.addAll(viewerFragmentMap.get(viewerFragIdx).keySet());
        previewSelectedFragment(validIndices.get(viewerFragIdx));
        fragmentObservableList.addAll(currentMetaDatas);
        libraryFragments.setItems(fragmentObservableList);
    }

    private void previewSelectedFragment(int index) {
        File xyzFile;
        try {
            xyzFile = createTempXYZFileFromViewer(index);
            previewPanel.removeAll();
            previewPanel.openFile(xyzFile);
            if (!xyzFile.delete()) {
                System.err.println("Was unable to delete the created xyzFile");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("Selected fragment NULL");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Can not create XYZ file");
        }
    }

    @FXML
    private void selectLowestRMSDs(){
        double minimumValue = Double.MAX_VALUE;
        MetaData minmumMetaData = null;
        while (currentViewerFragmentIndex < validIndices.size()){
            Map<MetaData, String> currentRMSDs = viewerFragmentMap.get(validIndices.get(currentViewerFragmentIndex));
            for (MetaData md : currentRMSDs.keySet()){
                double rmsd = Double.parseDouble(currentRMSDs.get(md));
                if (rmsd < minimumValue){
                    minimumValue = rmsd;
                    minmumMetaData = md;
                }
            }
            selectedMetaDatas.put(validIndices.get(validIndices.get(currentViewerFragmentIndex)), minmumMetaData);
            currentViewerFragmentIndex++;
        }
        currentStage.close();
    }

    private File createTempXYZFileFromViewer(int fragmentIndex) throws IOException {
        BufferedWriter bw = null;
        File xyzFile = null;
        try {
            //Create a temp xyz file
            xyzFile = File.createTempFile("fragment_" + fragmentIndex, ".xyz");
            xyzFile.deleteOnExit();
            bw = new BufferedWriter(new FileWriter(xyzFile));

            ArrayList<Integer> atoms = getGroups(viewerFragments).get(fragmentIndex);
            //Write number of atoms not including dummy atoms in XYZ file
            bw.write(String.format("%d%n%n", atoms.size()));
//            System.out.println(atoms.size());
//            System.out.println();
            for (int i = 0; i < atoms.size(); i++) {
                Atom atom = mainJmolViewer.ms.getAtom(i);
                String fileLine = String.format("%s %.5f %.5f %.5f%n",
                        atom.getAtomName().replaceAll("[0-9]", ""), atom.x, atom.y, atom.z);
                bw.write(fileLine);
//                System.out.println(fileLine);
            }
        } finally {
            if (bw != null) bw.close();
        }
        return xyzFile;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ArrayList<ArrayList> getGroups(List<ArrayList<Integer>> fragment_list) {
        ArrayList<ArrayList> groups = new ArrayList<ArrayList>();
        for (ArrayList<Integer> frag : fragment_list) {
            if (frag.size() > 0) {
                ArrayList curr_group = new ArrayList();
                for (int piece : frag) {
                    curr_group.add(piece);
                }
                Collections.sort(curr_group);
                groups.add(curr_group);
            }
        }
        return groups;
    }

    public void setStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    public Map<Integer, MetaData> getSelectedMetaDatas(){
        return selectedMetaDatas;
    }

    public void setValidIndices(ArrayList<Integer> validIndices) {
        this.validIndices = validIndices;
    }

    public void setViewerFragmentMap(Map<Integer, Map<MetaData, String>> viewerFragmentMap) {
        this.viewerFragmentMap = viewerFragmentMap;
    }

    public void setViewerFragments(ArrayList<ArrayList<Integer>> viewerFragments) {
        this.viewerFragments = viewerFragments;
    }

    public void setMainJmolViewer(Viewer mainJmolViewer) {
        this.mainJmolViewer = mainJmolViewer;
    }

}
