package org.vmol.app.localDataBase;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.vmol.app.gamessSubmission.gamessSubmissionHistoryController;
import org.vmol.app.libEFP.libEFPInputController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.Preferences;

public class localDataBaseController {
    @FXML
    private Parent root;

    @FXML
    private ComboBox<String> group_selector;

    @FXML
    private TableView<localDatabaseRecord> choices;

    List<ObservableList<localDatabaseRecord>> data;

    @FXML
    public void initialize() throws IOException {
        choices.setEditable(true);
        List<String> no = new ArrayList<String>();
        no.add("5H65");
        no.add("NA");
        no.add("SOL");
        group_selector.setItems(FXCollections.observableArrayList(no));
        group_selector.setValue("5H65");


        ArrayList<List<localDatabaseRecord>> items = new ArrayList<List<localDatabaseRecord>>();

        ArrayList<String> items1_choice = new ArrayList<String>();
        items1_choice.add("1");


        List<localDatabaseRecord> items1 = Arrays.asList(new localDatabaseRecord("1", "0", "1"));

        List<localDatabaseRecord> items2 = Arrays.asList(new localDatabaseRecord("1", "0", "1"));

        String homeDir = System.getProperty("user.home");

        File rmsd_file = new File(homeDir + "/Desktop/benzene_na_water_box/water_rmsd.txt");
        FileReader fr = new FileReader(rmsd_file);
        BufferedReader bufferedReader = new BufferedReader(fr);
        String line;
        //String line = bufferedReader.readLine();
        //String[] token = line.split("\\s+");


        List<localDatabaseRecord> items3 = new ArrayList<localDatabaseRecord>();
        //Arrays.asList(new localDatabaseRecord("1",token[2],items1_choice));

        int m = 0;

        while ((line = bufferedReader.readLine()) != null) {
            m++;
            String[] token = line.split("\\s+");
            double corrected_val = Double.parseDouble(token[2]) / 10;
            items3.add(new localDatabaseRecord(Integer.toString(m), Double.toString(corrected_val), "1"));

        }


        items.add(items1);
        items.add(items2);
        items.add(items3);


        data = new ArrayList<ObservableList<localDatabaseRecord>>();
        for (int i = 0; i < 3; i++) {
            this.data.add(FXCollections.observableArrayList());
            this.data.get(i).addAll(items.get(i));

        }

        ObservableList<String> recordList = FXCollections.observableArrayList("1", "None");

        TableColumn<localDatabaseRecord, String> index = new TableColumn<localDatabaseRecord, String>("Index");
        index.setCellValueFactory(new PropertyValueFactory<localDatabaseRecord, String>("choice"));
        choices.getColumns().add(index);
        index.setPrefWidth(100.0);

        TableColumn<localDatabaseRecord, String> rmsd = new TableColumn<localDatabaseRecord, String>("RMSD");
        rmsd.setCellValueFactory(new PropertyValueFactory<localDatabaseRecord, String>("rmsd"));
        choices.getColumns().add(rmsd);
        rmsd.setPrefWidth(300.0);

        TableColumn<localDatabaseRecord, String> check = new TableColumn<>("Parameters");
        check.setCellValueFactory(cellData -> cellData.getValue().getParameters());
        check.setCellFactory(ComboBoxTableCell.<localDatabaseRecord, String>forTableColumn(recordList));
        check.setPrefWidth(255.0);
        check.setOnEditCommit((TableColumn.CellEditEvent<localDatabaseRecord, String> e) -> {
            String newValue = e.getNewValue();
            System.out.println(newValue);
            for (int i = 0; i < e.getTableView().getItems().size(); i++) {
                localDatabaseRecord re = (localDatabaseRecord) e.getTableView().getItems().get(i);
                re.setParameters(newValue);
            }
            choices.refresh();
        });
//			check.setOnEditCommit(
//				    new EventHandler<CellEditEvent<localDatabaseRecord, String>>() {
//				        @Override
//				        public void handle(CellEditEvent<localDatabaseRecord, String> t) {
//				            ((localDatabaseRecord) t.getTableView().getItems().get(t.getTablePosition().getRow())).setParameters(t.getNewValue());
//				        };
//				    }
//				);
//			check.setCellFactory(t -> new ComboBoxTableCell(recordList) {
//				@Override
//				public void startEdit() {
//					localDatabaseRecord currentRow = (localDatabaseRecord) getTableRow().getItem();
//					getItems().setAll(currentRow.getParameters());
//					super.startEdit();
//				}
//			});
        check.setEditable(true);
        choices.getColumns().add(check);
        choices.setItems(data.get(0));
    }


    @FXML
    public void switch_group() {
        int index = 0;
        if (group_selector.getValue().equals("5H65")) {
            index = 0;
        } else if (group_selector.getValue().equals("NA")) {
            index = 1;
        } else {
            index = 2;
        }
        choices.setItems(data.get(index));
    }

    @FXML
    public void finish() throws IOException {
        boolean flag = true;
        ArrayList<String> to_be_submitted = new ArrayList<String>();
        Preferences userPrefs = Preferences.userNodeForPackage(gamessSubmissionHistoryController.class);
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.get(i).size(); j++) {
                localDatabaseRecord re = data.get(i).get(j);
                if (re.getParameters().get().equals("None")) {
                    flag = false;
                    if (i == 0) {
                        to_be_submitted.add("5H65_" + re.getChoice());
                    } else if (i == 1) {
                        to_be_submitted.add("NA_" + re.getChoice());
                    } else if (i == 2) {
                        to_be_submitted.add("SOL" + re.getChoice());
                    }
                }
            }

        }
        if (flag == false) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Gamess");
            alert.setHeaderText(null);
            alert.setContentText("There are groups you have not picked parameters for, do you want to calculate them by Gamess?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                Date date = new Date();

                for (int j = 0; j < to_be_submitted.size(); j++) {

                    userPrefs.put(to_be_submitted.get(j), date.toString());
                    System.out.println(date.toString());
                }

                ((Stage) choices.getScene().getWindow()).close();
                String homeDir = System.getProperty("user.home");
                File rmsd_file = new File(homeDir + "/Desktop/benzene_na_water_box/inp_tail");
                FileReader fr = new FileReader(rmsd_file);
                BufferedReader bufferedReader = new BufferedReader(fr);
                String line;
                String str = "";
                while ((line = bufferedReader.readLine()) != null) {
                    str += line + "\n";
                }

                final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/org/vmol/app/qchem/QChemInput.fxml"));

                libEFPInputController controller;
                controller = new libEFPInputController(str);
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

            } else {

            }
        } else {
            ((Stage) choices.getScene().getWindow()).close();
            String homeDir = System.getProperty("user.home");
            File rmsd_file = new File(homeDir + "/Desktop/benzene_na_water_box/inp_tail");
            FileReader fr = new FileReader(rmsd_file);
            BufferedReader bufferedReader = new BufferedReader(fr);
            String line;
            String str = "";
            while ((line = bufferedReader.readLine()) != null) {
                str += line + "\n";
            }

            final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/org/vmol/app/qchem/QChemInput.fxml"));

            libEFPInputController controller;
            controller = new libEFPInputController(str);
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
    }


}
