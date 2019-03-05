package org.vmol.app.submission;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPInputStream;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.vmol.app.util.UnrecognizedAtomException;
import org.vmol.app.visualization.JmolVisualization;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

//
public class SubmissionHistoryController {
    @FXML
    private Parent root;
    @FXML
    private TableView<SubmissionRecord> tableView;
    private static Preferences userPrefs = Preferences.userNodeForPackage(SubmissionHistoryController.class);
    private boolean firstStart = true;


    @FXML
    public void initialize()
            throws IOException, SAXException, SQLException, ParseException, URISyntaxException, BackingStoreException {
        // userPrefs.clear();
        if (firstStart == true) {
            firstStart = false;
            tableView.setRowFactory(tv -> {
                TableRow<SubmissionRecord> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        // SubmissionRecord rowData = row.getItem();
                        try {
                            //visualize();
                            if (row.getItem().getName().equals("lysine")) {
                                Stage currStage = new Stage();
                                TextArea text = new TextArea();
                                text.setMinWidth(400);
                                text.setMinHeight(600);

                                String homeDir = System.getProperty("user.home");
                                File rmsd_file = new File(homeDir + "/Desktop/lysine_demo/lysine_simulation_job/test.inp.out");
                                FileReader fr = new FileReader(rmsd_file);
                                BufferedReader bufferedReader = new BufferedReader(fr);
                                String line;
                                String str = "";
                                while ((line = bufferedReader.readLine()) != null) {
                                    str += line + "\n";
                                }
                                text.setText(str);
                                VBox vbox = new VBox();
                                vbox.getChildren().addAll(text);
                                Scene scene = new Scene(vbox);
                                currStage.setScene(scene);
                                currStage.setMinWidth(400);
                                currStage.setMinHeight(600);
                                currStage.show();


                            } else if (row.getItem().getName().equals("benzene_na_h2o")) {
                                Stage currStage = (Stage) root.getScene().getWindow();
                                String homeDir = System.getProperty("user.home");
                                new JmolVisualization(currStage, true).show(new File(homeDir + "/Desktop/benzene_na_water_box/generate_traj/final.xyz"));
                            }
                            //Stage currStage = (Stage) root.getScene().getWindow();
                            //new JmolVisualization(currStage, false).show(new File("output_" + date_extension + ".xyz"));
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
                return row;
            });
        }

        ObservableList<SubmissionRecord> data = tableView.getItems();
//		String hostname = "halstead.rcac.purdue.edu";
//		Connection conn = new Connection(hostname);
//		conn.connect();
//		String username = "xu675";
//		String password = "He00719614";
//		boolean isAuthenticated = conn.authenticateWithPassword(username, password);
//		if (!isAuthenticated)
//			throw new IOException("Authentication failed.");
//
//		String[] keys = userPrefs.keys();
//
//		for (int i = 0; i < keys.length; i++) {
//			String value = userPrefs.get(keys[i], null);
//			String[] tokens = value.split("\\n");
//			Session sess = conn.openSession();
//			sess.execCommand("source /etc/profile; qstat " + tokens[0]);
//			InputStream stdout = new StreamGobbler(sess.getStdout());
//			BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
//			String status = "";
//			while (true) {
//				String line = br.readLine();
//				if (line == null)
//					break;
//				if (Character.isDigit(line.charAt(0)) == true) {
//					String[] status_tokens = line.split("\\s+");
//					status = status_tokens[status_tokens.length - 2];
//				}
//				// System.out.println(line);
//			}
//
//			br.close();
//			sess.close();
//			if (status.toUpperCase().equals("R")) {
//				status = "Running";
//			} else if (status.toUpperCase().equals("Q")) {
//				status = "Queuing";
//			} else if (status.toUpperCase().equals("C")) {
//				status = "Completed..Wrapping Up";
//			} else if (status.isEmpty()) {
//				status = "Ready to open";
//			}
//			SubmissionRecord sr = new SubmissionRecord(tokens[0], status, tokens[1]);
//			data.add(sr);
//
//		}
        SubmissionRecord sr = new SubmissionRecord("lysine", "Ready to open", "Wed Mar 14 15:18:34 EDT 2018");
        data.add(sr);
        sr = new SubmissionRecord("benzene_na_h2o", "Ready to open", "Wed Mar 14 15:18:34 EDT 2018");
        data.add(sr);
        String[] keys = userPrefs.keys();
        System.out.println(keys.length);
        for (int i = 0; i < keys.length; i++) {
            SubmissionRecord r = new SubmissionRecord(keys[i], "Queuing", userPrefs.get(keys[i], null));
            data.add(r);
        }
        //conn.close();

    }

    @FXML
    protected void addRecord() {
        ObservableList<SubmissionRecord> data = tableView.getItems();

    }

    public void clearRecords() throws BackingStoreException {
        userPrefs.clear();
        for (int i = 0; i < tableView.getItems().size(); i++) {
            tableView.getItems().clear();
        }
    }

    public void visualize() throws IOException, ParseException, UnrecognizedAtomException {
        String hostname = "halstead.rcac.purdue.edu";
        Connection conn = new Connection(hostname);
        conn.connect();
        String username = "xu675";
        String password = "He00719614";
        boolean isAuthenticated = conn.authenticateWithPassword(username, password);
        if (!isAuthenticated)
            throw new IOException("Authentication failed.");

        SubmissionRecord sr = tableView.getSelectionModel().getSelectedItem();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date dateValue = sdf.parse(sr.getTime());
        sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
        String date_extension = sdf.format(dateValue);

        SCPClient scp = conn.createSCPClient();
        SCPInputStream scpin = scp.get("vmol/output_" + date_extension);
        FileOutputStream out = new FileOutputStream(new File("output_" + date_extension));
        IOUtils.copy(scpin, out);
        out.close();
        scpin.close();
        conn.close();

        toXYZ("output_" + date_extension, date_extension);
        Stage currStage = (Stage) root.getScene().getWindow();
        new JmolVisualization(currStage, false).show(new File("output_" + date_extension + ".xyz"));
    }

    public void refresh()
            throws IOException, SAXException, SQLException, ParseException, URISyntaxException, BackingStoreException {
        for (int i = 0; i < tableView.getItems().size(); i++) {
            tableView.getItems().clear();
        }
        initialize();
    }

    private int getAtomNumber(String filename) {
        int atom_num = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("    GEOMETRY (ANGSTROMS)")) {
                    line = br.readLine();
                    line = br.readLine();
                    while (line != null && !line.isEmpty()) {
                        atom_num++;
                        line = br.readLine();
                    }
                    break;
                }
            }
            br.close();
            return atom_num;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void toXYZ(String filename, String date_extension) {

        try {
            int atom_num = getAtomNumber(filename);
            BufferedWriter bw = new BufferedWriter(new FileWriter("output_" + date_extension + ".xyz"));
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("    GEOMETRY (ANGSTROMS)")) {
                    bw.append(Integer.toString(atom_num));
                    bw.append("\n \n");
                    line = br.readLine();
                    line = br.readLine();
                    while (line != null && !line.isEmpty()) {
                        String[] tokens = line.split("\\s+");
                        bw.append(tokens[0].charAt(tokens[0].length() - 2));
                        bw.append("  " + tokens[1] + "  " + tokens[2] + "  " + tokens[3] + "\n");
                        line = br.readLine();
                    }
                    bw.append("\n");
                }
            }
            bw.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}
