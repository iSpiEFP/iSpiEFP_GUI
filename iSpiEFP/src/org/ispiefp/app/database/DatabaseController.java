package org.ispiefp.app.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.ispiefp.app.gamess.GamessFormController;
import org.ispiefp.app.server.iSpiEFPServer;
import org.ispiefp.app.visualizer.AuxiliaryDatabaseTableViewer;
import org.ispiefp.app.visualizer.JmolMainPanel;
import org.jmol.viewer.Viewer;
import org.ispiefp.app.Main;
import org.ispiefp.app.libEFP.libEFPInputController;

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
    private JmolMainPanel jmolMainPanel;
    private Viewer auxiliaryJmolViewer;
    private List<ArrayList<Integer>> fragment_list;
    private Pane bottomRightPane;
    
    private AuxiliaryDatabaseTableViewer dbTableViewer;
    
    private ArrayList<String> final_selections;

    public DatabaseController(Pane bottomRightPane, JmolMainPanel jmolMainPanel, Viewer auxiliaryJmolViewer, List<ArrayList<Integer>> fragment_list) {
        this.jmolViewer = jmolMainPanel.viewer;
        this.jmolMainPanel = jmolMainPanel;
        this.auxiliaryJmolViewer = auxiliaryJmolViewer;
        this.fragment_list = fragment_list;
        this.bottomRightPane = bottomRightPane;
    }

    /**
     * Main function for Database controller which queries the database, runs the list, and prompts users for Gamess
     *
     * @throws IOException
     */
    public void run() throws IOException {
        @SuppressWarnings("rawtypes")
        ArrayList<ArrayList> groups = getGroups(this.fragment_list);

        String workingDirectory = System.getProperty("user.dir");
        DatabaseFileManager databaseFileManager = new DatabaseFileManager(workingDirectory, jmolViewer);

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
            //run object which runs the aux jmolViewer and the tableView together to interact with the database files.
            //users use this object to view and select which files they would like to include in the calculations based on RMSD values
            dbTableViewer = new AuxiliaryDatabaseTableViewer(bottomRightPane, auxiliaryJmolViewer, jmolViewer, groups);
            dbTableViewer.runAuxiliaryList(group_filenames);

            if (to_be_submitted.size() > 0) {
                //there are not found molecules, run the Games input controller form
                GamessFormController gamessFormController = new GamessFormController(groups, unknownGroups, jmolMainPanel);
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
            GamessFormController gamessFormController = new GamessFormController(groups, unknownGroups, jmolMainPanel);
            gamessFormController.run();
        }
    }

    /**
     * Get the libefp submission button
     *
     * @return
     */
    private Button getLibefpSubmitButton() {
        BorderPane borderPane = Main.getMainLayout();
        ObservableList list = borderPane.getChildren();
        VBox vbox = (VBox) list.get(0);
        
        ObservableList list2 = vbox.getChildren();
        Pane buttonPane = (Pane) list2.get(1);
        
        ObservableList<Node> buttonList = buttonPane.getChildren();
        Button button_libefp = (Button) buttonList.get(8);
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
     * Send the Qchem Submission form
     */
    private void sendQChemForm() {
        List<ObservableList<DatabaseRecord>> data = this.dbTableViewer.getUserData();
        @SuppressWarnings("rawtypes")
        ArrayList<ArrayList> groups = getGroups(this.fragment_list);

        //Get the libefp coords for the input file
        String coords = generateQchemInput(data, groups);
        final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/org/ispiefp/app/libEFP/libEFP.fxml"));
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
                            org.jmol.modelset.Atom current_atom = jmolViewer.ms.at[atom_num];
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
