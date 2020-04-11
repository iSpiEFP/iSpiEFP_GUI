package org.ispiefp.app.metaDataSelector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.ispiefp.app.Main;
import org.ispiefp.app.MetaData.MetaData;
import org.ispiefp.app.util.CheckInternetConnection;
import org.ispiefp.app.visualizer.JmolMainPanel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetaDataSelectorController{
    private List<String> fragments;
    private List<String> fragmentFromFiles;
    private ObservableList<MetaData> fragmentObservableList = FXCollections.observableArrayList();
    private MetaData selectedFragment;

    @FXML
    private Parent root;

    @FXML
    private TableView<MetaData> fragmentList;

    @FXML
    private TableColumn fragmentName;

    @FXML
    private TableColumn fragmentFile;

    @FXML
    private TableColumn electrostaticsIndicator;

    @FXML
    private TableColumn XRIndicator;

    @FXML
    private TableColumn polarizationIndicator;

    @FXML
    private TableColumn dispersionIndicator;

    @FXML
    private TableColumn basisSetIndicator;

    @FXML
    private TextField fragmentSearchField;

    @FXML
    private Button selectButton;

    // the preview panel for FXML
    @FXML
    private Pane previewPane;

    // the preview jmol panel
    private JmolMainPanel jmolPreviewPanel;

    public MetaDataSelectorController(){
        fragments = new ArrayList<>();
        fragmentFromFiles = new ArrayList<>();
        selectedFragment = null;
        for (MetaData md : Main.fragmentTree.getMetaDataIterator()) {
            // If there is no internet connection and the efp file for the fragment is not local, skip it.
            if (!CheckInternetConnection.checkInternetConnection()) {
                File checkIfLocal = new File(md.getFromFile());
                if (!checkIfLocal.exists()) continue;
            }
            fragments.add(md.getFragmentName());
            fragmentFromFiles.add(md.getFromFile());
            fragmentObservableList.add(md);
        }

    }

    /**
     * A lot of the code for filtering below is taken from a tutorial which I found at this link:
     * https://code.makery.ch/blog/javafx-8-tableview-sorting-filtering/
     *
     * Other things may break this code, but changes which I know will break it are the following
     * 1. There must be a getFragmentName method in MetaData.java
     * 2. There must be a getFromFile method in MetaData.java
     * These functions are used in an internal wrapped in the call to the PropertyValueFactory
     * constructor.
     */
    @FXML
    public void initialize(){
        //Create table columns
        fragmentName.setCellValueFactory(new PropertyValueFactory<MetaData, String>("fragmentName"));
        fragmentFile.setCellValueFactory(new PropertyValueFactory<MetaData, String>("fromFile"));
        basisSetIndicator.setCellValueFactory(new PropertyValueFactory<MetaData, String>("basisSet"));
        electrostaticsIndicator.setCellValueFactory(new PropertyValueFactory<MetaData, Boolean>("electrostatics"));
        XRIndicator.setCellValueFactory(new PropertyValueFactory<MetaData, Boolean>("exchangeRepulsion"));
        polarizationIndicator.setCellValueFactory(new PropertyValueFactory<MetaData, Boolean>("polarization"));
        dispersionIndicator.setCellValueFactory(new PropertyValueFactory<MetaData, Boolean>("dispersion"));

        //Handle the display of non-string columns (just background colors should display)
        electrostaticsIndicator.setCellFactory(col -> new TableCell<MetaData, Boolean>(){
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText("");
                if (empty) this.setBackground(new Background(
                        new BackgroundFill(
                                Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                else if (item)
                    this.setBackground(new Background(
                            new BackgroundFill(
                                    Color.GREEN, CornerRadii.EMPTY, new Insets(1,1,1,1))));
                else
                    this.setBackground(new Background(
                            new BackgroundFill(
                                    Color.RED, CornerRadii.EMPTY, new Insets(1,1,1,1))));
            }
        });


        XRIndicator.setCellFactory(col -> new TableCell<MetaData, Boolean>(){
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText("");
                if (empty) this.setBackground(new Background(
                        new BackgroundFill(
                                Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                else if (item)
                    this.setBackground(new Background(
                            new BackgroundFill(
                                    Color.GREEN, CornerRadii.EMPTY, new Insets(1,1,1,1))));
                else
                    this.setBackground(new Background(
                            new BackgroundFill(
                                    Color.RED, CornerRadii.EMPTY, new Insets(1,1,1,1))));
            }
        });


        polarizationIndicator.setCellFactory(col -> new TableCell<MetaData, Boolean>(){
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText("");
                if (empty) this.setBackground(new Background(
                        new BackgroundFill(
                                Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                else if (item)
                    this.setBackground(new Background(
                            new BackgroundFill(
                                    Color.GREEN, CornerRadii.EMPTY, new Insets(1,1,1,1))));
                else
                    this.setBackground(new Background(
                            new BackgroundFill(
                                    Color.RED, CornerRadii.EMPTY, new Insets(1,1,1,1))));
            }
        });

        dispersionIndicator.setCellFactory(col -> new TableCell<MetaData, Boolean>(){
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText("");
                if (empty) this.setBackground(new Background(
                        new BackgroundFill(
                                Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                else if (item)
                    this.setBackground(new Background(
                            new BackgroundFill(
                                    Color.GREEN, CornerRadii.EMPTY, new Insets(1,1,1,1))));
                else
                    this.setBackground(new Background(
                            new BackgroundFill(
                                    Color.RED, CornerRadii.EMPTY, new Insets(1,1,1,1))));
            }
        });


        //Wrap the observables in a FilteredList
        FilteredList<MetaData> filteredData = new FilteredList<>(fragmentObservableList, p -> true);

        //Set the filter predicate (Don't have to use lambdas if you don't want to (Requires Java 8)
        fragmentSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(metaData ->{
                if (newValue == null || newValue.isEmpty()){
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (metaData.getFragmentName().toLowerCase().contains(lowerCaseFilter)) return true;
                else if (metaData.getFromFile().toLowerCase().contains(lowerCaseFilter)) return true;
                else return false;
            });
        });
        SortedList<MetaData> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(fragmentList.comparatorProperty());
        fragmentList.setItems(sortedData);

        // Call previewSelectedFragment to populate the preview window on the right when row selected changed
        fragmentList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldSelection, newSelection) -> {
            if (newSelection != null) previewSelectedFragment(fragmentList.getSelectionModel().getSelectedItem());
        });

    }

    public void handleSelection(){
        MetaData selectedFragment = Main.fragmentTree.getMetaData(
                fragmentFromFiles.get(fragmentList.getSelectionModel().getSelectedIndex()));
        Main.fragmentTree.setSelectedFragment(selectedFragment);
        selectedFragment.setEfpFile();
        Stage stage = (Stage) selectButton.getScene().getWindow();
        stage.close();
    }

    /**
     * The function will show a preview of the selected fragment with the window on the right of the table view
     * @param selectedItem the item selected to be displayed
     */
    private void previewSelectedFragment(MetaData selectedItem) {
        System.out.println("Previewing: " + selectedItem.getFragmentName());
        File xyzFile;
        try {
            xyzFile = selectedItem.createTempXYZ();
            jmolPreviewPanel = new JmolMainPanel(previewPane, new ListView<>());
            jmolPreviewPanel.openFile(xyzFile);
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("Selected fragment NULL");
        } catch (IOException e) {
            System.out.println("Can not create XYZ file");
        }
    }
}
