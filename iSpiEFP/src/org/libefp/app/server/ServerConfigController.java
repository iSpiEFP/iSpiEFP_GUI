package org.libefp.app.server;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.libefp.app.server.ServerDetails.QueueOptions;
import org.libefp.app.server.view.ServerEditViewController;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Handle Server Configurations from the user and load the javaFX server GUI
 */
public class ServerConfigController implements Initializable {

    private static final String SERVER_DETAILS_LIST = "serverDetailsList";

    private static final String EMPTY = "empty";

    @FXML
    private Parent root;

    @FXML
    private TableView<ServerDetails> serversList;

    @FXML
    private TableColumn<ServerDetails, String> cid;

    @FXML
    private TableColumn<ServerDetails, String> serverName;

    @FXML
    private TableColumn<ServerDetails, String> address;


    @FXML
    private TableColumn<ServerDetails, String> serverType;

    // As of now, make sure serverDetailsList is always updated when serversList is modified. TODO: Fix this later!!
    private static List<ServerDetails> serverDetailsList;

    private static Preferences userPrefs = Preferences.userNodeForPackage(ServerConfigController.class);

    public ServerConfigController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cid.setCellValueFactory(new Callback<CellDataFeatures<ServerDetails, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<ServerDetails, String> c) {
                return new ReadOnlyObjectWrapper<String>(serversList.getItems().indexOf(c.getValue()) + 1 + "");
            }
        });
        serverName.setCellValueFactory(new PropertyValueFactory<ServerDetails, String>("serverName"));
        address.setCellValueFactory(new PropertyValueFactory<ServerDetails, String>("address"));
        serverType.setCellValueFactory(new PropertyValueFactory<ServerDetails, String>("serverType"));
        try {
            List<ServerDetails> savedList = getServerDetailsList();
            serversList.getItems().setAll(savedList);
        } catch (ClassNotFoundException | BackingStoreException | IOException e) {
            // Probably show an alert to the user that cannot fetch server Details from the preferences
            System.out.println("Unable to fetch the server details from preferences");
            e.printStackTrace();
        }
    }

    // TODO : convert this list and store in the preferences (after serialization) and load it from the preferences too!
    public static List<ServerDetails> getServerDetailsList() throws BackingStoreException, ClassNotFoundException, IOException {
        if (serverDetailsList == null) serverDetailsList = new ArrayList<>();
        if (serverDetailsList.size() == 0) {
            //  Preferences prefs = Preferences.userNodeForPackage(this.getClass());
            ServerDetails test = new ServerDetails("test", "data.cs.purdue.edu", "SSH", 22);
            test.setQueueOptions(getDefaultQueueOptions(test));
            serverDetailsList.add(test);
        }

        String recovered = userPrefs.get(SERVER_DETAILS_LIST, EMPTY);

        if (recovered.equalsIgnoreCase(EMPTY)) return serverDetailsList;

        List<ServerDetails> reconstructed = (List<ServerDetails>) fromString(recovered);

        serverDetailsList = reconstructed;

        return serverDetailsList;
    }

    private static QueueOptions getDefaultQueueOptions(ServerDetails serverDetails) {
        QueueOptions queueOptions = serverDetails.new QueueOptions();
        queueOptions.setSubmit("/usr/pbs/bin/qsub ${JOB_NAME}.run");
        queueOptions.setQuery("/usr/pbs/bin/qstat -f ${JOB_ID}");
        queueOptions.setKill("/usr/pbs/bin/qdel ${JOB_ID}");
        queueOptions.setJobFileList("find ${JOB_DIR} -type f");
        queueOptions.setQueueInfo("/usr/pbs/bin/qstat -fQ");
        queueOptions.setSubmit("/usr/pbs/bin/qsub ${JOB_NAME}.run");
        queueOptions.setUpdateIntervalSecs(5);
        queueOptions.setRunFileTemplate(" #!/bin/bash \n #PBS -r n \n #PBS -q scholar \n #PBS -l nodes=1:ppn=${NCPUS}"
                + "	\n #PBS -l walltime=${WALLTIME} \n #QCHEM VARIABLES  \n QC=/group/lslipche/apps/qchem/QCHEM_4.2.1; export QC"
                + " \n QCAUX=/group/lslipche/apps/qchem/QCHEM_4.2.1/qcaux; export QCAUX \n QCRSH=ssh; export QCRSH \n source ${QC}/qcenv.sh"
                + " \n #END QCHEM VARIABLES \n cd ${PBS_O_WORKDIR} \n export QCSCRATCH=${RCAC_SCRATCH} \n qchem ${JOB_NAME}.inp ${JOB_NAME}.out");
        return queueOptions;
    }

    @FXML
    private void handleDeleteServer() {
        int selectedIndex = serversList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            serversList.getItems().remove(selectedIndex);
            serverDetailsList.remove(selectedIndex);
            try {
                updateServerDetailsListInPreferences();
            } catch (IOException e) {
                // Can alert the user if needed
                System.out.println("Unable to save the server details in preferences");
                e.printStackTrace();
            }
        } else {
            // Nothing selected.
            Alert alert = new Alert(AlertType.WARNING);
            alert.initOwner((Stage) root.getScene().getWindow());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Server Selected");
            alert.setContentText("Please select a server from the table.");
            alert.showAndWait();
        }
    }

    private void updateServerDetailsListInPreferences() throws IOException {
        String converted = toString((Serializable) serverDetailsList); // Implementations of list interface also implements Serializable!
        userPrefs.put(SERVER_DETAILS_LIST, converted);
    }

    private boolean showServerEditView(ServerDetails serverDetails, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("view/ServerEditView.fxml"));
        Parent serverEditView = loader.load();

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner((Stage) root.getScene().getWindow());
        stage.setScene(new Scene(serverEditView));

        // Set the serverDetails into the controller
        ServerEditViewController controller = loader.getController();
        controller.setServerDetails(serverDetails);
        stage.showAndWait();
        return controller.isOkClicked();
    }

    @FXML
    private void handleAddServer() throws IOException {
        ServerDetails serverDetails = new ServerDetails();
        serverDetails.setQueueOptions(getDefaultQueueOptions(serverDetails));
        boolean okClicked = showServerEditView(serverDetails, "Add new Server");
        if (okClicked) {
            serversList.getItems().add(serverDetails);
            serverDetailsList.add(serverDetails);
            try {
                updateServerDetailsListInPreferences();
            } catch (IOException e) {
                // Can alert the user if needed
                System.out.println("Unable to save the server details in preferences");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleEditServer() throws IOException {
        ServerDetails selectedServer = serversList.getSelectionModel().getSelectedItem();
        int selectedIndex = serversList.getSelectionModel().getSelectedIndex();
        if (selectedServer != null) {
            boolean okClicked = showServerEditView(selectedServer, "Edit Server " + selectedServer.getServerName());
            if (okClicked) {
                serversList.getItems().set(selectedIndex, selectedServer);
                serverDetailsList.set(selectedIndex, selectedServer);
                try {
                    updateServerDetailsListInPreferences();
                } catch (IOException e) {
                    // Can alert the user if needed
                    System.out.println("Unable to save the server details in preferences");
                    e.printStackTrace();
                }
            }
        } else {
            // Nothing selected.
            Alert alert = new Alert(AlertType.WARNING);
            alert.initOwner((Stage) root.getScene().getWindow());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Server Selected");
            alert.setContentText("Please select a server from the table.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) root.getScene().getWindow()).close();
    }

    /**
     * Read the object from Base64 string.
     */
    private static Object fromString(String s) throws IOException,
            ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    /**
     * Write the object to a Base64 string.
     */
    private static String toString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

}
