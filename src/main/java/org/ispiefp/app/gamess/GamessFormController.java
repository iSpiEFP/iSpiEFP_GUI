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

package org.ispiefp.app.gamess;

/**
 * {@link GamessFormController} Class handles Input form for Gamess submission
 */
public class GamessFormController {
//    private ArrayList<Integer> to_be_submitted;
//    private ArrayList<Integer> unknownGroups;
//    private ArrayList<ArrayList> groups;
//    private Scene scene;
//    private Stage newStage;
//    private TableView table;
//    private Viewer viewer;
//    private JmolMainPanel jmolMainPanel;
//
//    /**
//     * Constructor
//     *
//     * @param groups        the total molecule groups
//     * @param unknowngroups the unknown molecules
//     */
//    public GamessFormController(ArrayList<ArrayList> groups, ArrayList<Integer> unknowngroups, JmolMainPanel jmolMainPanel) {
//        this.to_be_submitted = new ArrayList<Integer>();
//        this.unknownGroups = unknowngroups;
//        this.groups = groups;
//        this.viewer = jmolMainPanel.viewer;
//        this.jmolMainPanel = jmolMainPanel;
//    }
//
//    /**
//     * Main Function that runs and manages all database Controller Processes
//     */
//    public void run() {
//        VBox root = new VBox();
//        root.setPadding(new Insets(10));
//        root.setSpacing(5);
//
//        Label label = new Label("The following fragments either were not found, or returned an RMSD\n value above 0.5. Please select any fragments you would like\n to have Gamess calculate.");
//
//        //Add table to pane
//        this.table = new TableView();
//        TableColumn column1 = new TableColumn("Choice");
//        TableColumn column2 = new TableColumn("Select");
//
//        TableColumn<GamessRecord, String> index = column1;
//        index.setCellValueFactory(new PropertyValueFactory<GamessRecord, String>("choice"));
//        index.setPrefWidth(400.0);
//
//        TableColumn<GamessRecord, Boolean> check = column2;
//        check.setCellValueFactory(new PropertyValueFactory<GamessRecord, Boolean>("check"));
//        check.setCellFactory(column -> new CheckBoxTableCell());
//        check.setEditable(true);
//        check.setPrefWidth(100);
//
//        table.getColumns().addAll(column1, column2);
//        table.setEditable(true);
//
//        ObservableList<GamessRecord> data = loadData(unknownGroups).get(0);
//        runTable(data);
//
//        // To contain the buttons
//        HBox buttonBar = new HBox();
//
//        /**
//         * Handle the continue button
//         * User is done selecting, send the selected fragments to be submitted and load the gamess form
//         */
//        Button buttonContinue = new Button("Continue");
//        buttonContinue.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                System.out.println("gamess");
//                try {
//                    newStage.close();
//
//                    for (GamessRecord record : data) {
//                        if (record.getCheck()) {
//                            to_be_submitted.add(record.getIndex());
//                        }
//                    }
//                    sendRealGamessForm(groups, to_be_submitted);
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        /**
//         * Handle the select All button
//         * Select all fragments, and set check values to true
//         */
//        Button buttonSelectAll = new Button("Select All");
//        buttonSelectAll.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                System.out.println("analyze");
//                for (GamessRecord record : data) {
//                    record.setCheck(true);
//                }
//            }
//        });
//
//        //Create UI
//        buttonBar.getChildren().addAll(buttonSelectAll, buttonContinue);
//        root.getChildren().addAll(label, table, buttonBar);
//
//        this.scene = new Scene(root, 520, 520);
//        this.newStage = new Stage();
//
//        newStage.setTitle("Fragments not found in Database");
//        newStage.setScene(scene);
//        newStage.showAndWait();
//    }
//
//    /**
//     * Load
//     *
//     * @param groups
//     * @return
//     */
//    private List<ObservableList<GamessRecord>> loadData(ArrayList<Integer> groups) {
//        ArrayList<GamessRecord> items = new ArrayList<GamessRecord>();
//        for (Integer group : groups) {
//            items.add(new GamessRecord("Fragment: " + (group + 1), group, false));
//        }
//
//        //LOAD LIST STRUCTURES
//        List<ObservableList<GamessRecord>> data = new ArrayList<ObservableList<GamessRecord>>();
//        for (int i = 0; i < items.size(); i++) {
//            data.add(FXCollections.observableArrayList(new Callback<GamessRecord, Observable[]>() {
//                @Override
//                public Observable[] call(GamessRecord param) {
//                    return new Observable[]{
//                            param.checkProperty()};
//
//                }
//            }));
//            data.get(0).add(items.get(i));
//        }
//        return data;
//    }
//
//
//    @SuppressWarnings("unchecked")
//    /**
//     * Run the data for the table, handle row selections
//     */
//    private void runTable(ObservableList<GamessRecord> data) {
//        //LOAD  LIST
//        @SuppressWarnings("rawtypes")
//        TableView table = this.table;
//
//        table.setItems(data);
//
//        //ADD SELECTION TO BE SUBMITTED LIST
//        table.setRowFactory(tv -> {
//            TableRow<GamessRecord> row = new TableRow<>();
//            row.setOnMouseClicked(event -> {
//
//                if (event.getClickCount() == 1 && (!row.isEmpty())) {
//                    // SubmissionRecord rowData = row.getItem();
//                    System.out.println("hit a row");
//
//
//                }
//            });
//            return row;
//        });
//    }
//
//    /**
//     * Send the Gamess Form which contains all the gamess input
//     *
//     * @param groups          the entire group of molecules
//     * @param to_be_submitted the selected group of molecules to be submitted
//     * @throws IOException
//     */
//    private void sendRealGamessForm(ArrayList<ArrayList> groups, ArrayList to_be_submitted) throws IOException {
//
//        if (true) {
//            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
//            Date date = new Date();
//
//            StringBuilder sb = new StringBuilder();
//
//            final FXMLLoader gamess_loader = new FXMLLoader(this.getClass().getResource("/views/gamessInput.fxml"));
//            GamessInputController gamess_controller;
//            gamess_controller = new GamessInputController(new File(MainViewController.getLastOpenedFile()), groups, to_be_submitted, jmolMainPanel);
//            gamess_loader.setController(gamess_controller);
//            ArrayList<ArrayList> fragments = gamess_controller.get_fragments_with_h();
//
//            HashMap<String, Integer> protons = new HashMap<>();
//            protons.put("H", 1);
//            protons.put("C", 6);
//            protons.put("N", 7);
//            protons.put("O", 8);
//            protons.put("S", 16);
//            protons.put("CL", 17);
//            protons.put("H000", 1);
//
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    BorderPane bp;
//                    try {
//                        bp = gamess_loader.load();
//                        Scene scene = new Scene(bp, 659.0, 500.0);
//                        Stage stage = new Stage();
//                        stage.initModality(Modality.WINDOW_MODAL);
//                        stage.setTitle("Gamess Input");
//                        stage.setScene(scene);
//                        stage.show();
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//
//                }
//            });
//        }
//    }
}

