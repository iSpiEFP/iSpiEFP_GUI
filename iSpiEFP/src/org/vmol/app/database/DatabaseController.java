package org.vmol.app.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jmol.viewer.Viewer;
import org.vmol.app.Main;
import org.vmol.app.gamess.GamessFormController;
import org.vmol.app.installer.LocalBundleManager;
import org.vmol.app.libEFP.libEFPInputController;
import org.vmol.app.server.iSpiEFPServer;
import org.vmol.app.visualizer.JmolVisualizer;
import org.vmol.app.visualizer.ViewerHelper;

import java.io.*;
import java.math.BigDecimal;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Database Controller controls all database interactions, and implementation with Gamess and Libefp buttons.
 * It also handles all the clicking operations for the auxiliary list
 */
public class DatabaseController {

    private Viewer jmolViewer;
    private Viewer auxiliaryJmolViewer;
    @SuppressWarnings("rawtypes")
    private TableView auxiliary_list;
    private List<ArrayList<Integer>> fragment_list;
    private ArrayList<ArrayList> groups;

    private int prev_selection_index = 0;
    private List<ObservableList<DatabaseRecord>> userData;
    private ArrayList<String> final_selections;

    private int viewerIndex; //index of selected fragment in main viewer

    private ListView listView;

    public DatabaseController(Viewer jmolViewer, Viewer auxiliaryJmolViewer, TableView auxiliary_list, List<ArrayList<Integer>> fragment_list) {
        this.jmolViewer = jmolViewer;
        this.auxiliaryJmolViewer = auxiliaryJmolViewer;
        this.auxiliary_list = auxiliary_list;
        this.fragment_list = fragment_list;
    }

    /**
     * Main function for Database controller which queries the database, runs the list, and prompts users for Gamess
     *
     * @throws IOException
     */
    public void run() throws IOException {
        @SuppressWarnings("rawtypes")
        ArrayList<ArrayList> groups = getGroups(this.fragment_list);
        this.groups = groups;

        String workingDirectory = System.getProperty("user.dir");
        DatabaseFileManager databaseFileManager = new DatabaseFileManager(workingDirectory);

        //query database for chemical formulas
        JsonFilePair[][] response = queryDatabase(groups, databaseFileManager);

        //currently supported by queryV2 which sends and recieves JSON
        ArrayList<ArrayList<String[]>> files = databaseFileManager.processDBresponse(response);
        ArrayList<ArrayList<String[]>> group_filenames = databaseFileManager.writeFiles(files);

        ArrayList<Integer> unknownGroups = new ArrayList<Integer>();

        if (group_filenames.size() > 0) {
            //read files into array of strings
            ArrayList<String> filenames = new ArrayList<String>();
            int groupNumber = 1;
            ArrayList<Integer> to_be_submitted = new ArrayList<Integer>();

            for (ArrayList<String[]> group : group_filenames) {
                if (group.size() > 0) {
                    boolean groupFound = false;
                    
                    for (String[] pair_name : group) {
                        String xyz_filename = pair_name[0];
                        String efp_filename = pair_name[1];
                        String rmsd = pair_name[2];

                        //files with rmsd 0.5 and under only
                        BigDecimal val = new BigDecimal(rmsd);
                        if (val.doubleValue() < 0.5) {
                            //unknownGroups.add(groupNumber - 1);
                            //to_be_submitted.add(groupNumber - 1);

                            //enter this value so that the same group isnt pinged again in the not found list
                            //(only one molecule of this type needs to be calculated for Gamess, the one the user loaded)
                            groupFound = true;
                        }
                        filenames.add(xyz_filename);
                    }
                    if(!groupFound) {
                        unknownGroups.add(groupNumber - 1);
                        to_be_submitted.add(groupNumber - 1);
                    }
                    /*
                    if(!groupFound){
                        boolean yes = sendGamessForm("There are 0 matches for fragment:"+Integer.toString(groupNumber)+" in the Database, do you want to calculate them by Gamess?");
                        if(yes) {
                            to_be_submitted.add(groupNumber-1);
                        }
                    }*/
                } else {
                    //this particular fragment did not have any matches from the database
                    //boolean yes = sendGamessForm("There are 0 matches for fragment:"+Integer.toString(groupNumber)+" in the Database, do you want to calculate them by Gamess?");
                    unknownGroups.add(groupNumber - 1);
                    to_be_submitted.add(groupNumber - 1);
                }
                groupNumber++;
            }
            runAuxiliaryList(group_filenames);

            if (to_be_submitted.size() > 0) {
                //there are unfound molecules, run the Games input controller form
                GamessFormController gamessFormController = new GamessFormController(groups, unknownGroups);
                gamessFormController.run();
            }

            Button button_libefp = getLibefpSubmitButton();
            button_libefp.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent event) {
                    //handle action
                    sendQChemForm(); //send and arm qchem input form

                }
            });
        } else {
            //There are 0 matched Fragments
            //build index of gorups to be submitted since there are zero matches it is simply the size of groups
            ArrayList<Integer> to_be_submitted = new ArrayList<Integer>();
            for (int i = 0; i < groups.size(); i++) {
                to_be_submitted.add(i);
                unknownGroups.add(i);
            }
            GamessFormController gamessFormController = new GamessFormController(groups, unknownGroups);
            gamessFormController.run();
        }
    }

    /**
     * Get the libefp submission button
     *
     * @return
     */
    private Button getLibefpSubmitButton() {
        ObservableList<Node> buttonList = new JmolVisualizer().getButtonList();
        Button button_libefp = (Button) buttonList.get(6);
        button_libefp.setDisable(false);
        return button_libefp;
    }

    /**
     * Query the database for the present chemical formulas
     *
     * @param groups      the molecules desired for the query
     * @param fileManager the fileManager object associated
     * @return A Json File Pair [][] which contains a list of objects with rmsd, xyz files, and efp files as parameters
     * @throws IOException
     */
    private JsonFilePair[][] queryDatabase(ArrayList<ArrayList> groups, DatabaseFileManager fileManager) throws IOException {
        //generate json query
        String jsonQuery = fileManager.generateJsonQuery(groups);

        //connect to iSpiEFP server
        String serverName = Main.iSpiEFP_SERVER;
        int port = Main.iSpiEFP_PORT;
        iSpiEFPServer iSpiServer = new iSpiEFPServer();
        Socket client = iSpiServer.connect(serverName, port);
        if (client == null) {
            return null;
        }
        //send data to server
        OutputStream outToServer = client.getOutputStream();
        outToServer.write(jsonQuery.getBytes(StandardCharsets.UTF_8));

        //Read response from server
        InputStream inFromServer = client.getInputStream();
        DataInputStream in = new DataInputStream(inFromServer);
        String str = "";
        StringBuffer buf = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        if (in != null) {
            while ((str = reader.readLine()) != null) {
                buf.append(str + "\n");
            }
        }
        String reply = buf.toString();

        //Parse Json Response
        Gson gson = new GsonBuilder().create();
        JsonFilePair[][] response = gson.fromJson(reply, JsonFilePair[][].class);

        //clean up
        reader.close();
        in.close();
        inFromServer.close();
        outToServer.close();
        client.close();

        return response;
    }

    //converts Addison's frag list to Hanjings Groups
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

    /**
     * Runs the auxiliary List functions including loading data, loading lists, loading the viewer, and handling
     * list selections and choices
     *
     * @param group_filenames
     */
    private void runAuxiliaryList(ArrayList<ArrayList<String[]>> group_filenames) {
        try {
            //wait for files to write
            Thread.sleep(300);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<ObservableList<DatabaseRecord>> data = loadAuxListData(group_filenames);
        this.userData = data;

        ListView<String> listView = getFragmentListButtons();
        listView.getSelectionModel().selectFirst();

        this.listView = listView;
        String path = data.get(0).get(this.prev_selection_index).getChoice();
        if (path.equalsIgnoreCase("not found")) {
            runTable(data, 0);
        } else {
            runTable(data, 0);
        }

        //set listener to items
        //TRIGGER LIST LOAD WITH FRAGMENT CLICK
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String[] arrOfStr = newValue.split(" ");
                int index = Integer.parseInt(arrOfStr[1]) - 1;

                //Left panel fragment was selected. Load the appropriate list
                runTable(data, index);
            }
        });
    }

    @SuppressWarnings("unchecked")
    /**
     * Run the auxiliary List, load its data, and handle viewer loading
     */
    private void runTable(List<ObservableList<DatabaseRecord>> data, int index) {
        //LOAD AUXILIARY LIST
        @SuppressWarnings("rawtypes")
        TableView table = this.auxiliary_list;

        table.setItems(data.get(index));
        allowOnlyOneCheck(data, index);
        this.viewerIndex = index;

        //Initialize first jmol viewer
        String path = data.get(index).get(this.prev_selection_index).getChoice();

        if (path.equals("Not found")) {
            Main.auxiliaryJmolPanel.viewer.runScript("delete;");
            Main.auxiliaryJmolPanel.repaint();
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

    /**
     * Send the Qchem Submission form
     */
    private void sendQChemForm() {
        List<ObservableList<DatabaseRecord>> data = this.userData;
        @SuppressWarnings("rawtypes")
        ArrayList<ArrayList> groups = getGroups(this.fragment_list);

        //Get the libefp coords for the input file
        String coords = generateQchemInput(data, groups);
        final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/org/vmol/app/qchem/QChemInput.fxml"));
        libEFPInputController controller;
        controller = new libEFPInputController(coords, null, this.final_selections);
        loader.setController(controller);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                TabPane bp;
                try {
                    bp = loader.load();
                    Scene scene = new Scene(bp, 600.0, 480.0);
                    Stage stage = new Stage();
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.setTitle("Libefp Input");
                    stage.setScene(scene);
                    stage.show();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
    }

    @SuppressWarnings("unchecked")
    /**
     * Generate the correct qchm form inputs
     * Convert selected list data to efp files and xyz coordinates in libefp format
     */
    private String generateQchemInput(List<ObservableList<DatabaseRecord>> data, @SuppressWarnings("rawtypes") ArrayList<ArrayList> groups) {
        StringBuilder sb = new StringBuilder();
        this.final_selections = new ArrayList<String>();
        int group_number = 0;
        for (ObservableList<DatabaseRecord> list : data) {
            for (DatabaseRecord record : list) {
                if (record.getCheck() == true) {
                    if (!record.getChoice().equalsIgnoreCase("NOT FOUND")) {
                        //parse filename
                        String file_name = record.getChoice();

                        String[] filename = file_name.split("\\.");
                        final_selections.add(filename[0] + ".efp");
                        if (group_number == 0) {
                            sb.append("fragment " + filename[0] + "\n");
                        } else {
                            sb.append("\nfragment " + filename[0] + "\n");
                        }
                        //apend equivalent group coordinates
                        ArrayList<Integer> fragment = groups.get(group_number);
                        int i = 0;
                        for (int atom_num : fragment) {
                            if (i == 3) {
                                break;
                            }
                            org.jmol.modelset.Atom current_atom = Main.jmolPanel.viewer.ms.at[atom_num];
                            sb.append(current_atom.x + "  " + current_atom.y + "  " + current_atom.z + "\n");
                            i++;
                        }

                    }
                    break;
                }
            }
            group_number++;
        }
        return sb.toString();
    }

}
