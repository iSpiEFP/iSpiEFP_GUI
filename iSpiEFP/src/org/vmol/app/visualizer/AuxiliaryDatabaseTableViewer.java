package org.vmol.app.visualizer;

import java.util.ArrayList;
import java.util.List;

import org.jmol.viewer.Viewer;
import org.vmol.app.Main;
import org.vmol.app.database.DatabaseRecord;
import org.vmol.app.installer.LocalBundleManager;

import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

public class AuxiliaryDatabaseTableViewer {
    private TableView tableView;
    private List<ObservableList<DatabaseRecord>> userData;
    private ListView<String> fragmentListView;
    private int prev_selection_index = 0;
    private int viewerIndex; //index of selected fragment in main viewer
    private Viewer auxiliaryJmolViewer;
    private Viewer jmolViewer;
    private ArrayList<ArrayList> groups;
    private Pane bottomRightPane;
    
    public AuxiliaryDatabaseTableViewer(Pane bottomRightPane, Viewer auxiliaryJmolViewer, Viewer jmolViewer, ArrayList<ArrayList> groups) {
        this.auxiliaryJmolViewer = auxiliaryJmolViewer;
        this.jmolViewer = jmolViewer;
        this.groups = groups;
        this.bottomRightPane = bottomRightPane;
        init();
    }
    
    public TableView getTable() {
        return this.tableView;
    }
    
    public List<ObservableList<DatabaseRecord>> getUserData() {
        return this.userData;
    }
    
    /**
     * Initialize the tableView
     */
    private void init() {
        //Add table to pane
        TableView table = new TableView();
        TableColumn column1 = new TableColumn("Choice");
        TableColumn column2 = new TableColumn("RMSD");
        TableColumn column3 = new TableColumn("Select");

        //Init column 1 of table (choice)
        TableColumn<DatabaseRecord, String> index = column1;
        index.setCellValueFactory(new PropertyValueFactory<DatabaseRecord, String>("choice"));
        index.setPrefWidth(200.0);

        //Init column 2 of table (rmsd)
        TableColumn<DatabaseRecord, String> rmsd = column2;
        rmsd.setCellValueFactory(new PropertyValueFactory<DatabaseRecord, String>("rmsd"));
        rmsd.setPrefWidth(100);

        //Init column 3 of table (check)
        TableColumn<DatabaseRecord, Boolean> check = column3;
        check.setCellValueFactory(new PropertyValueFactory<DatabaseRecord, Boolean>("check"));
        check.setCellFactory(column -> new CheckBoxTableCell());
        check.setEditable(true);
        check.setPrefWidth(75);

        //add columns to table
        table.getColumns().addAll(column1, column2, column3);
        table.setEditable(true);
        
        this.tableView = table;
        bottomRightPane.getChildren().add(tableView);
    }
    
    /**
     * Runs the auxiliary List functions including loading data, loading lists, loading the viewer, and handling
     * list selections and choices
     *
     * @param group_filenames
     */
    public void runAuxiliaryList(ArrayList<ArrayList<String[]>> group_filenames) {
        try {
            //wait for files to write
            Thread.sleep(300);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<ObservableList<DatabaseRecord>> data = loadAuxListData(group_filenames);
        this.userData = data;

        fragmentListView = getFragmentListButtons();
        fragmentListView.getSelectionModel().selectFirst();

        String path = data.get(0).get(this.prev_selection_index).getChoice();
        if (path.equalsIgnoreCase("not found")) {
            runTable(data, 0);
        } else {
            runTable(data, 0);
        }

        //set listener to items
        //TRIGGER LIST LOAD WITH FRAGMENT CLICK
        fragmentListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String[] arrOfStr = newValue.split(" ");
                int index = Integer.parseInt(arrOfStr[1]) - 1;

                //Left panel fragment was selected. Load the appropriate list
                runTable(data, index);
            }
        });
    }
    
    /**
     * Load the auxiliary List with data
     *
     * @param group_filenames a list for each group containing array with xyz, rmsd, and efp file
     * @return object containing all the database files
     */
    private List<ObservableList<DatabaseRecord>> loadAuxListData(ArrayList<ArrayList<String[]>> group_filenames) {
        ArrayList<List<DatabaseRecord>> items = new ArrayList<List<DatabaseRecord>>();
        for (ArrayList<String[]> group : group_filenames) {
            ArrayList<DatabaseRecord> drs = new ArrayList<DatabaseRecord>();
            if (group.size() > 0) {
                int index = 0;
                for (String[] pair_name : group) {
                    String xyz_filename = pair_name[0];
                    String rmsd = pair_name[2];
                    if (rmsd.equals("0.0")) {
                        rmsd = "Exact Match";
                    }
                    if (index == 0) {
                        drs.add(new DatabaseRecord(xyz_filename, rmsd, true, index++));
                    } else {
                        drs.add(new DatabaseRecord(xyz_filename, rmsd, false, index++));
                    }
                }
            } else {
                //this particular fragment did not have any matches from the database
                drs.add(new DatabaseRecord("Not found", "", true, 0));
            }
            items.add(drs);

        }
        
        //LOAD AUXILIARY LIST STRUCTURES
        List<ObservableList<DatabaseRecord>> data = new ArrayList<ObservableList<DatabaseRecord>>();
        for (int i = 0; i < items.size(); i++) {
            data.add(FXCollections.observableArrayList(new Callback<DatabaseRecord, Observable[]>() {
                @Override
                public Observable[] call(DatabaseRecord param) {
                    return new Observable[]{
                            param.checkProperty()};

                }
            }));
            data.get(i).addAll(items.get(i));
        }
        return data;
    }
    
    @SuppressWarnings("unchecked")
    /**
     * Run the auxiliary List, load its data, and handle viewer loading
     */
    private void runTable(List<ObservableList<DatabaseRecord>> data, int index) {
        //LOAD AUXILIARY LIST
        @SuppressWarnings("rawtypes")
        TableView table = this.tableView;

        table.setItems(data.get(index));
        allowOnlyOneCheck(data, index);
        this.viewerIndex = index;

        //Initialize first jmol viewer
        String path = data.get(index).get(this.prev_selection_index).getChoice();

        if (path.equals("Not found")) {
            auxiliaryJmolViewer.runScript("delete;");
            //auxiliaryJmolViewer.repaint();
            loadAuxJmolViewer("Not found", index);
        } else {
            loadAuxJmolViewer(path, index);
        }

        //LOAD AUX JMOL VIEWER ON CLICK
        table.setRowFactory(tv -> {
            TableRow<DatabaseRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {

                if (event.getClickCount() == 1 && (!row.isEmpty())) {
                    try {
                        //Aux List Row was selected. Load the list with the correct data
                        loadAuxJmolViewer(row.getItem().getChoice(), index);

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
            return row;
        });
    }
    

    /**
     * Allow only one check for a list of checkboxes
     *
     * @param data
     * @param index
     */
    private void allowOnlyOneCheck(List<ObservableList<DatabaseRecord>> data, int index) {
        this.prev_selection_index = 0;
        ObservableList<DatabaseRecord> data_subset = data.get(index);
        for (int i = 0; i < data_subset.size(); i++) {
            DatabaseRecord d = data_subset.get(i);
            d.checkProperty().addListener((o, oldV, newV) -> {
                if (newV) {
                    int curr = d.getIndex();
                    if (prev_selection_index != curr) {
                        data_subset.get(prev_selection_index).checkProperty().set(false);
                    }
                    this.prev_selection_index = curr;
                }
            });
        }
    }
    
    /**
     * Load the correct file into the auxJmolViewer
     *
     * @param filename xyz file to be read
     * @param index    index of the xyz file
     */
    private void loadAuxJmolViewer(String filename, int index) {

        String path = LocalBundleManager.LIBEFP_COORDINATES;
        auxiliaryJmolViewer.setAutoBond(true);

        auxiliaryJmolViewer.openFile("file:" + path + LocalBundleManager.FILE_SEPERATOR + filename);
        @SuppressWarnings("unchecked")
        ViewerHelper viewerHelper = new ViewerHelper(jmolViewer, auxiliaryJmolViewer, this.groups.get(this.viewerIndex));
        viewerHelper.ConnectXYZBonds();

    }
    
    @SuppressWarnings("unchecked")
    /**
     * Get the buttons from the main left panel fragment list
     */
    private ListView<String> getFragmentListButtons() {
        SplitPane splitpane = (SplitPane) Main.getMainLayout().getChildren().get(2);
        ObservableList<Node> list = splitpane.getItems();
        @SuppressWarnings("rawtypes")
        ListView<String> listView = (ListView) list.get(0);
        return listView;
    }
}
