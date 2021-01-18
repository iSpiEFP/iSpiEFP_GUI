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

package org.ispiefp.app.jobSubmission;

/**
 * Handle submission History for LIBEFP
 */
public class SubmissionHistoryController {
//    @FXML
//    private Parent root;
//    @FXML
//    private TableView<SubmissionRecord> tableView;
//    private static Preferences userPrefs = Preferences.userNodeForPackage(SubmissionHistoryController.class);
//    private Map<String, ServerInfo> serverMap;
//
//
//    public SubmissionHistoryController() {
//    }
//
//    @FXML
//    public void initialize() {
//        serverMap = UserPreferences.getServers();
//
//        Enumeration<SubmissionRecord> records = UserPreferences.getJobsMonitor().getRecords().elements();
//        ObservableList<SubmissionRecord> data = FXCollections.observableArrayList();
//
//        while (records.hasMoreElements()) { data.add(records.nextElement()); }
//
//        tableView.getItems().setAll(data);
//    }
//
//    private void loadData(ArrayList<String[]> jobHistory, ObservableList<SubmissionRecord> data) {
//        for (String[] line : jobHistory) {
//            String job_id = line[0];
//            String title = line[1];
//            String date = line[2];
//            String status = line[3];
//
//            String statement = new String();
//            if (status.equals("QUEUE")) {
//                statement = "Queuing";
//            } else {
//                statement = "Ready to open";
//            }
//            SubmissionRecord record = new SubmissionRecord(title, statement, date, job_id);
//            data.add(record);
//        }
//    }
//
//    @FXML
//    protected void addRecord() {
//        ObservableList<SubmissionRecord> data = tableView.getItems();
//
//    }
//
//    public void clearRecords() throws BackingStoreException {
//        userPrefs.clear();
//        for (int i = 0; i < tableView.getItems().size(); i++) {
//            tableView.getItems().clear();
//        }
//    }
//
//    public void deleteRecord() {
//        UserPreferences.getJobsMonitor().deleteRecord(tableView.getSelectionModel().getSelectedItem());
//    }
//
//    public void refresh()
//            throws IOException, SAXException, SQLException, ParseException, URISyntaxException, BackingStoreException {
//        for (int i = 0; i < tableView.getItems().size(); i++) {
//            tableView.getItems().clear();
//        }
//        initialize();
//    }
//
////    /**
////     * User has selected a row, load the efp files and xyz files and send the user the output controller form
////     * @throws ParseException
////     * @throws UnrecognizedAtomException
////     */
//    public void visualize() throws ParseException, UnrecognizedAtomException {
//
//        SubmissionRecord record = tableView.getSelectionModel().getSelectedItem();
//
//        if (record.getStatus().equalsIgnoreCase("complete")) {
//            System.out.println("opening record");
//            //Get output form
//            String output = "Error.";
//            JobManager jobManager = new JobManager(this.username, this.password, this.hostname);
//            try {
//                output = jobManager.getRemoteFile("iSpiClient/Libefp/output/" + record.getOutputFilePath());
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            //get error form
//            String log = "Error.";
//            JobManager jobManager2 = new JobManager(this.username, this.password, this.hostname);
//            try {
//                log = jobManager2.getRemoteFile("iSpiClient/Libefp/output/" + record.getStdoutputFilePath());
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            OutputController outputController = new OutputController();
//            outputController.initialize(output, log, "LIBEFP");
//        }
//    }

}
