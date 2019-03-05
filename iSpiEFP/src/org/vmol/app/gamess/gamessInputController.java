package org.vmol.app.gamess;

import ch.ethz.ssh2.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.jmol.viewer.Viewer;
import org.vmol.app.Main;
import org.vmol.app.MainViewController;
import org.vmol.app.gamessSubmission.gamessSubmissionHistoryController;
import org.vmol.app.loginPack.LoginForm;
import org.vmol.app.server.JobManager;
import org.vmol.app.server.ServerConfigController;
import org.vmol.app.server.ServerDetails;
import org.vmol.app.server.iSpiEFPServer;
import org.vmol.app.util.Atom;
import org.vmol.app.visualizer.JmolVisualizer;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Handle all Gamess input and job submission for Gamess
 */
public class gamessInputController implements Initializable {

    @FXML
    private Parent root;

    @FXML
    private TextArea gamessInputArea;

    @FXML
    private ComboBox<String> serversList;

    private ArrayList<ArrayList<Atom>> connections;
    private ArrayList<Atom> atoms;
    private ArrayList<ArrayList> final_lists = new ArrayList<ArrayList>();
    // TODO: change this to use hashmap
    String[] bonds = {"HH", "CC", "NN", "OO", "FF", "CLCL", "BRBR", "II", "CN", "NC", "CO", "OC", "CS", "SC", "CF",
            "FC", "CCL", "CLC", "CBR", "BRC", "CI", "IC", "HC", "CH", "HN", "NH", "HO", "OH", "HF", "FH", "HCL", "CLH",
            "HBR", "BRH", "HI", "IH"};
    double[] lengths = {0.74, 1.54, 0.45, 1.48, 1.42, 1.99, 2.28, 2.67, 1.47, 1.47, 1.43, 1.43, 1.82, 1.82, 1.35, 1.35,
            1.77, 1.77, 1.94, 1.94, 2.14, 2.14, 1.09, 1.09, 1.01, 1.01, 0.96, 0.96, 0.92, 0.92, 1.27, 1.27, 1.41, 1.41,
            1.61, 1.61};
    List<ServerDetails> serverDetailsList;
    Map<String, Double> charges;

    private ArrayList inputs;
    private ArrayList<Integer> fragmentNumbers;

    public gamessInputController(File file, ArrayList<ArrayList> groups, ArrayList to_be_submitted) {
        inputs = new ArrayList();
        fragmentNumbers = to_be_submitted;

        Viewer viewer = Main.jmolPanel.viewer;

        // get atoms from to be submitted atoms list
        for (int i = 0; i < to_be_submitted.size(); i++) {
            ArrayList<Atom> curr_group = new ArrayList<Atom>();

            for (int j = 0; j < groups.get((Integer) to_be_submitted.get(i)).size(); j++) {
                int atomNum = (int) groups.get((Integer) to_be_submitted.get(i)).get(j);
                org.jmol.modelset.Atom atom = viewer.ms.at[atomNum];
                Atom liteAtom = new Atom(atom.getElementSymbol(), atomNum, atom.getElementNumber(), atom.x, atom.y, atom.z);
                curr_group.add(liteAtom);
            }
            final_lists.add(curr_group);
        }

        // find missing bonds and add hydrogens to them
        for (int i = 0; i < to_be_submitted.size(); i++) {
            ArrayList<Atom> new_hydrogens = addHydrogens(groups.get((Integer) to_be_submitted.get(i)));
            for (int j = 0; j < new_hydrogens.size(); j++) {

                final_lists.get(i).add(new_hydrogens.get(j));

            }
        }
        generateGamessInputFiles();
    }

    /**
     * Find the missing atom where there used to be one from a bond
     *
     * @param fragAtomIndex fragmented atom index
     * @param frag          list of frags
     * @param originalBonds the original bonds before slicing
     * @return
     */
    private ArrayList<Integer> missingAtom(int fragAtomIndex, ArrayList<Integer> frag,
                                           ArrayList<ArrayList<Integer>> originalBonds) {
        ArrayList<Integer> missingAtoms = new ArrayList<Integer>();
        for (Integer atomIndex : originalBonds.get(fragAtomIndex)) {
            boolean found = false;
            // search frag for atomIndex, if not there then it has been broken
            for (Integer fragAtom : frag) {
                if (fragAtom.equals(atomIndex)) {
                    found = true;
                }
            }
            if (!found) {
                // bond has been cut here
                System.out.println("bond missing between atom:" + (fragAtomIndex + 1) + " and atom:" + (atomIndex + 1));
                missingAtoms.add(atomIndex);
            }
        }
        return missingAtoms;
    }

    private static int searchArray(String[] a, String to_be_matched) {
        for (int i = 0; i < a.length; i++) {
            if (a[i].equals(to_be_matched)) {
                return i;
            }
        }
        return -1;
    }

    public ArrayList<ArrayList> get_fragments_with_h() {
        return final_lists;
    }

    /**
     * Add dummy hydrogens where bonds were sliced
     *
     * @param frag list of fragments
     * @return
     */
    public ArrayList<Atom> addHydrogens(ArrayList frag) {
        ArrayList<ArrayList<Integer>> originalBonds = JmolVisualizer.bondMap;
        Viewer viewer = Main.jmolPanel.viewer;

        ArrayList<Atom> hydrogens = new ArrayList<Atom>();
        for (int i = 0; i < frag.size(); i++) {
            int atomIndex = (int) frag.get(i);
            org.jmol.modelset.Atom atom1 = viewer.ms.at[atomIndex];

            ArrayList<Integer> cutOffAtoms = missingAtom(atomIndex, frag, originalBonds);

            for (Integer atomNum : cutOffAtoms) {
                org.jmol.modelset.Atom atom2 = viewer.ms.at[atomNum];

                String a_type = atom1.getAtomName();
                String b_type = atom2.getAtomName();
                int index = searchArray(bonds, a_type + b_type);
                double desired_length = lengths[index];
                double x1 = atom1.x;
                double y1 = atom1.y;
                double z1 = atom1.z;
                double x2 = atom2.x;
                double y2 = atom2.y;
                double z2 = atom2.z;
                double actual_length = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));
                double x3 = ((x2 - x1) * desired_length / actual_length) + x1;
                double y3 = ((y2 - y1) * desired_length / actual_length) + y1;
                double z3 = ((z2 - z1) * desired_length / actual_length) + z1;
                hydrogens.add(new Atom("H000", -1, 1, x3, y3, z3));
            }
        }
        return hydrogens;
    }

    @Override
    /**
     * Initialize thee Gamess form for javaFX
     */
    public void initialize(URL location, ResourceBundle resources) {

        try {

            for (int i = 0; i < final_lists.size(); i++) {
                if (i < final_lists.size() - 1) {
                    updateGamessInputArea((String) inputs.get(i));
                    updateGamessInputArea("\n\n\n");
                } else {
                    updateGamessInputArea((String) inputs.get(i));
                }

            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Initializing serversList
        serverDetailsList = new ArrayList<>();
        try {
            serverDetailsList = ServerConfigController.getServerDetailsList();
        } catch (ClassNotFoundException | BackingStoreException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<String> serverNames = new ArrayList<>();
        for (ServerDetails server : serverDetailsList) {
            serverNames.add(server.getAddress());
        }
        serversList.setItems(FXCollections.observableList(serverNames));
        if (serverNames.size() > 0)
            serversList.setValue(serverNames.get(0));

        gamessInputArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue,
                                final String newValue) {
                // this will run whenever text is changed
                String[] arrays = newValue.split("\n\n\n");
                inputs.clear();
                for (int i = 0; i < arrays.length; i++) {
                    if (arrays[i] != null && !arrays[i].trim().isEmpty()) {
                        inputs.add(arrays[i]);
                        System.out.println(arrays[i]);
                    }

                }
            }
        });
    }

    // This method will be called on update of any of the input fields
    public void updateGamessInputArea(String text) throws FileNotFoundException {
        gamessInputArea.setText(gamessInputArea.getText() + text);
    }

    /**
     * Generate Gamess input Form from selected Fragments
     */
    public void generateGamessInputFiles() {
        Viewer viewer = Main.jmolPanel.viewer;

        for (int i = 0; i < final_lists.size(); i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(" $contrl units=angs local=boys runtyp=makefp\n");
            sb.append("       mult=1 icharg=0 coord=cart icut=11 $end\n");
            sb.append(" $system timlim=99999   mwords=200 $end\n");
            sb.append(" $scf soscf=.f. dirscf=.t. diis=.t. CONV=1.0d-06  $end\n");
            sb.append(" $basis gbasis=n31 ngauss=6 ndfunc=1 $end\n");
            sb.append(" $DAMP IFTTYP(1)=2,0 IFTFIX(1)=1,1 thrsh=500.0 $end\n");
            String pol = "f";
            String disp = "f";
            String exrep = "f";
            boolean[] interteted_parameters = MainViewController.get_interested_parameters();
            if (interteted_parameters[0] == true) {
                pol = "t";
            }
            if (interteted_parameters[1] == true) {
                disp = "t";
            }
            if (interteted_parameters[2] == true) {
                exrep = "t";
            }

            sb.append(" $MAKEFP  POL=." + pol + ". DISP=." + disp + ". CHTR=.f.  EXREP=." + exrep + ". $end\n");
            sb.append(" $data\n");
            sb.append("Fragment" + " " + (fragmentNumbers.get(i) + 1) + "\n");
            sb.append(" C1\n");
            for (int j = 0; j < final_lists.get(i).size(); j++) {
                Atom a = (Atom) final_lists.get(i).get(j);

                sb.append("  ");
                sb.append(a.type);
                sb.append("   ");
                sb.append(String.format("%.1f", a.charge));
                sb.append("   ");
                sb.append(Double.toString(a.x));
                sb.append("   ");
                sb.append(Double.toString(a.y));
                sb.append("   ");
                sb.append(Double.toString(a.z));
                sb.append("\n");

            }
            sb.append(" $end\n $comment Atoms to be erased:  $end\n");
            // System.out.println(sb.toString());
            inputs.add(sb.toString());
        }
    }

    // Generate Q-Chem Input file
    public void export() throws IOException {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Save Zip Files to...");
        File defaultDirectory = new File(".");
        chooser.setInitialDirectory(defaultDirectory);
        Stage currStage = (Stage) root.getScene().getWindow();
        File selectedDirectory = chooser.showDialog(currStage);

        // System.out.println(selectedDirectory.getAbsolutePath());
        FileOutputStream fos = new FileOutputStream(selectedDirectory.getAbsolutePath() + "/gamess.zip");
        ZipOutputStream zos = new ZipOutputStream(fos);
        for (int i = 0; i < inputs.size(); i++) {

            byte[] b = ((String) inputs.get(i)).getBytes(Charset.forName("UTF-8"));
            ZipEntry entry = new ZipEntry("gamess_" + Integer.toString(i) + ".inp");
            entry.setSize(b.length);
            zos.putNextEntry(entry);
            zos.write(b);
            zos.closeEntry();

        }
        zos.close();
        fos.close();

    }

    public static void writeZipFile(File directoryToZip, List<File> fileList) {
        try {
            FileOutputStream fos = new FileOutputStream(directoryToZip.getName() + ".zip");
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (File file : fileList) {
                if (!file.isDirectory()) { // we only zip files, not directories
                    addToZip(directoryToZip, file, zos);
                }
            }

            zos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addToZip(File directoryToZip, File file, ZipOutputStream zos)
            throws FileNotFoundException, IOException {

        FileInputStream fis = new FileInputStream(file);

        String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
                file.getCanonicalPath().length());
        System.out.println("Writing '" + zipFilePath + "' to zip file");
        ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }

    /**
     * Handle job submission button on Gamess Form
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void handleSubmit() throws IOException, InterruptedException {
        ServerDetails selectedServer = serverDetailsList.get(serversList.getSelectionModel().getSelectedIndex());
        System.out.println(selectedServer.getServerType());
        if (selectedServer.getServerType().equalsIgnoreCase("local"))
            submitJobToLocalServer(selectedServer);
        else {

            String hostname = selectedServer.getAddress();
            LoginForm loginForm = new LoginForm(hostname, "GAMESS");
            boolean authorized = loginForm.authenticate();

            if (authorized) {
                Connection conn = loginForm.getConnection(authorized);

                String username = loginForm.getUsername();
                String password = loginForm.getPassword();

                ArrayList jobids = new ArrayList();

                //For each fragment, generate input
                for (int i = 0; i < inputs.size(); i++) {

                    SCPClient scp = conn.createSCPClient();
                    DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
                    Date date = new Date();
                    String currentTime = dateFormat.format(date).toString();

                    String jobID = (new JobManager()).generateJobID().toString();

                    SCPOutputStream scpos = scp.put("gamess_" + jobID + ".inp", ((String) inputs.get(i)).length(),
                            "./iSpiClient/Gamess/src", "0666");
                    InputStream istream = IOUtils.toInputStream((String) inputs.get(i), "UTF-8");
                    IOUtils.copy(istream, scpos);
                    istream.close();
                    scpos.close();

                    String pbs_script = "cd iSpiClient/Gamess/src;\n ./rungms gamess_" + jobID + ".inp" + " > gamess_"
                            + jobID + ".log";

                    scpos = scp.put("pbs_" + jobID, pbs_script.length(), "./iSpiClient/Gamess/src", "0666");

                    istream = IOUtils.toInputStream(pbs_script, "UTF-8");
                    IOUtils.copy(istream, scpos);
                    istream.close();
                    scpos.close();

                    Session sess = conn.openSession();

                    sess.execCommand(
                            "source /etc/profile; cd iSpiClient/Gamess/src; qsub -l walltime=00:30:00 -l nodes=1:ppn=1 -q standby pbs_"
                                    + jobID);

                    InputStream stdout = new StreamGobbler(sess.getStdout());
                    BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
                    String clusterjobID = "";
                    while (true) {
                        String line = br.readLine();
                        if (line == null)
                            break;
                        String[] tokens = line.split("\\.");
                        if (tokens[0].matches("\\d+")) {
                            clusterjobID = tokens[0];
                        }
                    }
                    br.close();
                    stdout.close();
                    sess.close();


                    jobids.add(clusterjobID);
                    // Date date = new Date();
                    Preferences userPrefs = Preferences.userNodeForPackage(gamessSubmissionHistoryController.class);
                    String str = date.toString() + "\n" + MainViewController.getLastOpenedFileName() + "\n" + hostname
                            + "\n";// + uname + "\n" + pwd + "\n";
                    System.out.println(str);
                    System.out.println(clusterjobID);
                    userPrefs.put(clusterjobID, str);

                    userPrefs.put(clusterjobID, clusterjobID + "\n" + currentTime + "\n");

                    String serverName = Main.iSpiEFP_SERVER;
                    int port = Main.iSpiEFP_PORT;

                    String title = "A_Default_title";
                    String time = currentTime;

                    // send over job data to database
                    String query = "Submit2";
                    query += "$END$";
                    query += username + "  " + hostname + "  " + jobID + "  " + title + "  " + time + "  " + "QUEUE"
                            + "  " + "GAMESS";
                    query += "$ENDALL$";

                    // Socket client = new Socket(serverName, port);
                    iSpiEFPServer iSpiServer = new iSpiEFPServer();
                    Socket client = iSpiServer.connect(serverName, port);
                    if (client == null) {
                        return;
                    }
                    OutputStream outToServer = client.getOutputStream();
                    // DataOutputStream out = new DataOutputStream(outToServer);

                    System.out.println(query);
                    outToServer.write(query.getBytes("UTF-8"));
                    client.close();
                    outToServer.close();

                    //poll for job finishing
                    JobManager jobManager = new JobManager(username, password, hostname, jobID, title, time, "QUEUE",
                            "GAMESS");
                    jobManager.watchJobStatus();

                    //send success alert to user
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Gamess Submission");
                    alert.setHeaderText(null);
                    alert.setContentText("Job submitted to cluster successfully.");
                    alert.showAndWait();
                }
                conn.close();

            }

        }
        // Handle SSH case later
    }

    private void submitJobToLocalServer(ServerDetails selectedServer) throws IOException, InterruptedException {
        // // Printing the complete absolute path from where the application was
        // initialized
        // System.out.println("Your working Directory is " +
        // System.getProperty("user.dir"));
        // String path = System.getProperty("user.dir");
        // String executablePath = selectedServer.getWorkingDirectory();
        // System.out.println("Executable path is: " + executablePath);
        // String inputFileName = path + "/vmolAppJob_" + title.getText();
        // String outputFileName = path += "/vmolAppJob_" + title.getText() +
        // "_output";
        //
        // // First create the input File at that location with the content in
        // qchemInputTextArea
        // boolean inputFileCreated = createInputFile(inputFileName);
        // if (!inputFileCreated) return; // Can probably return some error here
        // List<String> command = new ArrayList<String>();
        // command.add(executablePath);
        // command.add(inputFileName);
        // ProcessBuilder builder = new ProcessBuilder(command);
        // builder.redirectOutput(new File(outputFileName));
        //
        // final Process process = builder.start();
        //
        // int errorCode = process.waitFor(); // 0 means everything went well!
        // System.out.println("Process execution completed with errorCode : " +
        // String.valueOf(errorCode));
        //
        // if (outputFileName.length() != 0) {
        // System.out.println("Printing output File contents: ");
        // Scanner s = null;
        // try {
        // s = new Scanner(new File(outputFileName));
        // while (s.hasNextLine()) {
        // System.out.println(s.nextLine());
        // }
        // } catch (Exception e){
        // e.printStackTrace(System.out);
        // } finally {
        // if (s != null) s.close();
        // }
        // }
    }

    // Creates an input file at this location
    private boolean createInputFile(String inputFileName) {
        BufferedWriter output = null;
        try {
            File file = new File(inputFileName);
            output = new BufferedWriter(new FileWriter(file));
            output.write(gamessInputArea.getText());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
