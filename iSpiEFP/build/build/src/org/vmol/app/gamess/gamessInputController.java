package org.vmol.app.gamess;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.vmol.app.server.ServerConfigController;
import org.vmol.app.server.ServerDetails;
import org.vmol.app.util.Atom;
import org.vmol.app.util.PDBParser;
import org.vmol.app.util.UnrecognizedAtomException;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class gamessInputController implements Initializable {

    @FXML
    private Parent root;

//    @FXML
//    private TextField title;
//    
//    @FXML
//    private ComboBox<String> calculation;
//    
//    private Map<String, String> calculationMap = new HashMap<String, String>() {{
//    	put("Single Point Energy", "SP");
//    	put("Geometry Optimization", "Opt");
//    	put("Frequencies", "Freq");
//    }};
//    
//    @FXML
//    private ComboBox<String> theory;
//    
//    @FXML
//    private ComboBox<String> basis;
//    
//    @FXML
//    private TextField charge;
//    
//    @FXML
//    private TextField multiplicity;
//    
//    @FXML
//    private ComboBox<String> format;

    @FXML
    private TextArea gamessInputArea;

    @FXML
    private ComboBox<String> serversList;

    private ArrayList<ArrayList<Atom>> connections;
    private ArrayList atoms;
    private ArrayList<ArrayList> final_lists = new ArrayList<ArrayList>();
    //TODO: change this to use hashmap
    String[] bonds = {"HH", "CC", "NN", "OO", "FF", "CLCL", "BRBR", "II", "CN", "NC", "CO", "OC", "CS", "SC", "CF", "FC", "CCL", "CLC", "CBR", "BRC", "CI", "IC", "HC", "CH", "HN", "NH", "HO", "OH", "HF", "FH", "HCL", "CLH", "HBR", "BRH", "HI", "IH"};
    double[] lengths = {0.74, 1.54, 0.45, 1.48, 1.42, 1.99, 2.28, 2.67, 1.47, 1.47, 1.43, 1.43, 1.82, 1.82, 1.35, 1.35, 1.77, 1.77, 1.94, 1.94, 2.14, 2.14, 1.09, 1.09, 1.01, 1.01, 0.96, 0.96, 0.92, 0.92, 1.27, 1.27, 1.41, 1.41, 1.61, 1.61};
    List<ServerDetails> serverDetailsList;
    Map<String, Double> charges;


    public gamessInputController(File file, ArrayList<ArrayList> groups) {

        try {
            connections = PDBParser.connectivity(file);
            atoms = PDBParser.get_atoms(file);
        } catch (IOException | UnrecognizedAtomException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (int i = 0; i < groups.size(); i++) {
            ArrayList<Atom> curr_group = new ArrayList<Atom>();
            for (int j = 0; j < groups.get(i).size(); j++) {
                curr_group.add((Atom) atoms.get((int) groups.get(i).get(j)));
            }
            final_lists.add(curr_group);
        }

        for (int i = 0; i < groups.size(); i++) {
            ArrayList<Atom> new_hydrogens = addHydrogens(groups.get(i));
            for (int j = 0; j < new_hydrogens.size(); j++) {

                final_lists.get(i).add(new_hydrogens.get(j));

            }
        }

        for (int i = 0; i < final_lists.size(); i++) {
            System.out.println("Group " + Integer.toString(i));
            for (int j = 0; j < final_lists.get(i).size(); j++) {
                Atom p = (Atom) final_lists.get(i).get(j);
                System.out.println(p.type + "  " + Double.toString(p.x) + "  " + Double.toString(p.y) + "  " + Double.toString(p.z));
            }
            System.out.println(" ");
            System.out.println(" ");
        }
        charges = new HashMap<String, Double>();
        charges.put("H", 1.0);
        charges.put("H000", 1.0);
        charges.put("C", 6.0);
        charges.put("N", 7.0);
        charges.put("O", 8.0);
        charges.put("S", 16.0);

        generateGamessInputFiles();
    }

    private Atom missingAtom(ArrayList frag, ArrayList given) {

        for (int i = 0; i < given.size(); i++) {
            boolean found = false;
            for (int j = 0; j < frag.size(); j++) {


                if ((int) frag.get(j) == ((Atom) given.get(i)).index) {
                    found = true;
                }
            }
            if (found == false) {
                return (Atom) given.get(i);
            }
        }
        return null;
    }

    private static int searchArray(String[] a, String to_be_matched) {
        for (int i = 0; i < a.length; i++) {
            if (a[i].equals(to_be_matched)) {
                return i;
            }
        }
        return -1;
    }

    public ArrayList<Atom> addHydrogens(ArrayList frag) {
        ArrayList<Atom> hydrogens = new ArrayList<Atom>();
        for (int i = 0; i < frag.size(); i++) {


            Atom cut_off_atom = missingAtom(frag, connections.get((int) frag.get(i)));

            if (cut_off_atom != null) {
                String a_type = ((Atom) atoms.get((int) frag.get(i))).type;
                String b_type = cut_off_atom.type;
                int index = searchArray(bonds, a_type + b_type);
                double desired_length = lengths[index];
                double x1 = ((Atom) atoms.get((int) frag.get(i))).x;
                double y1 = ((Atom) atoms.get((int) frag.get(i))).y;
                double z1 = ((Atom) atoms.get((int) frag.get(i))).z;
                double x2 = cut_off_atom.x;
                double y2 = cut_off_atom.y;
                double z2 = cut_off_atom.z;
                double actual_length = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));
                double x3 = ((x2 - x1) * desired_length / actual_length) + x1;
                double y3 = ((y2 - y1) * desired_length / actual_length) + y1;
                double z3 = ((z2 - z1) * desired_length / actual_length) + z1;
                hydrogens.add(new Atom("H000", -1, x3, y3, z3));

            }
        }

        return hydrogens;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Adding listener to title
//		title.textProperty().addListener((observable, oldValue, newValue) -> {
//		    try {
//				updateQChemInputText();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		});
//		
//		// Initializing calculation ComboBox
//		calculation.setItems(FXCollections.observableList(new ArrayList<>(calculationMap.keySet())));
//		calculation.setValue("Geometry Optimization");
//		
//		// Initializing theory ComboBox
//		List<String> theoryTypes = new ArrayList<String>();
//		theoryTypes.add("HF");
//		theoryTypes.add("MP2");
//		theoryTypes.add("B3LYP");
//		theoryTypes.add("B3LYP5");
//		theoryTypes.add("EDF1");
//		theoryTypes.add("M06-2X");
//		theoryTypes.add("CCSD");
//		
//		theory.setItems(FXCollections.observableList(theoryTypes));
//		theory.setValue("B3LYP");
//		
//		// Initializing basis ComboBox
//		List<String> basisTypes = new ArrayList<String>();
//		basisTypes.add("STO-3G");
//		basisTypes.add("3-21G");
//		basisTypes.add("6-31G(d)");
//		basisTypes.add("6-31G(d,p)");
//		basisTypes.add("6-31+G(d)");
//		basisTypes.add("6-311G(d)");
//		basisTypes.add("cc-pVDZ");
//		basisTypes.add("LANL2DZ");
//		basisTypes.add("LACVP");
//		
//		basis.setItems(FXCollections.observableList(basisTypes));
//		basis.setValue("6-31G(d)");
//		
//		// TODO : Make both charge and multiplicity fields accept only Numbers 
//		// Initializing Charge textField
//		charge.setText("0");
//		charge.textProperty().addListener((observable, oldValue, newValue) -> {
//			// force the field to be numeric only
//            if (!newValue.matches("-?[0-9]+")) {
//                charge.setText(newValue.replaceAll("[^\\d]", ""));
//            }
//		    try {
//				updateQChemInputText();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		});
//		
//		// Initializing Multiplicity textField
//		multiplicity.setText("1");
//		multiplicity.textProperty().addListener((observable, oldValue, newValue) -> {
//			// force the field to be numeric only
//            if (!newValue.matches("^[1-9]\\d*$")) {
//                multiplicity.setText("");
//            }
//		    try {
//				updateQChemInputText();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		});
//		
//		// Initializing Format ComboBox
//		List<String> formatTypes = new ArrayList<String>();
//		
//		formatTypes.add("Cartesian");
////		formatTypes.add("Z-matrix");
////		formatTypes.add("Z-matrix (compact)");
//		
//		format.setItems(FXCollections.observableList(formatTypes));
//		if (formatTypes.size() > 0) format.setValue(formatTypes.get(0));
//		
        // Initializing qChemInputTextArea
        try {
            ArrayList files = generateGamessInputFiles();

            for (int i = 0; i < final_lists.size(); i++) {
                updateGamessInputArea((String) files.get(i));
                updateGamessInputArea("\n\n\n");
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
            serverNames.add(server.getServerName());
        }
        serversList.setItems(FXCollections.observableList(serverNames));
        if (serverNames.size() > 0) serversList.setValue(serverNames.get(0));
    }


    // This method will be called on update of any of the input fields
    public void updateGamessInputArea(String text) throws FileNotFoundException {
        gamessInputArea.setText(gamessInputArea.getText() + text);
    }

    public ArrayList generateGamessInputFiles() {
        ArrayList gamessFiles = new ArrayList();
        for (int i = 0; i < final_lists.size(); i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(" $contrl units=angs local=boys runtyp=makefp\n");
            sb.append("       mult=1 icharg=0 coord=cart icut=11 $end\n");
            sb.append(" $system timlim=99999   mwords=200 $end\n");
            sb.append(" $scf soscf=.f. dirscf=.t. diis=.t. CONV=1.0d-06  $end\n");
            sb.append(" $basis gbasis=n31 ngauss=6 ndfunc=1 $end\n");
            sb.append(" $DAMP IFTTYP(1)=2,0 IFTFIX(1)=1,1 thrsh=500.0 $end\n");
            sb.append(" $MAKEFP  POL=.t. DISP=.t. CHTR=.f.  EXREP=.t. $end\n");
            sb.append(" $data\n");
            sb.append(" gamess_" + Integer.toString(i) + "\n");
            sb.append(" C1\n");
            for (int j = 0; j < final_lists.get(i).size(); j++) {
                Atom a = (Atom) final_lists.get(i).get(j);
                sb.append("  ");
                sb.append(a.type);
                sb.append("   ");
                sb.append(String.format("%.1f", charges.get(a.type)));
                sb.append("   ");
                sb.append(Double.toString(a.x));
                sb.append("   ");
                sb.append(Double.toString(a.y));
                sb.append("   ");
                sb.append(Double.toString(a.z));
                sb.append("\n");
            }
            sb.append(" $end\n $comment Atoms to be erased:  $end\n");
            System.out.println(sb.toString());
            gamessFiles.add(sb.toString());


        }
        return gamessFiles;


    }

    // Generate Q-Chem Input file

    public void export() throws IOException {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Save Zip Files to...");
        File defaultDirectory = new File(".");
        chooser.setInitialDirectory(defaultDirectory);
        Stage currStage = (Stage) root.getScene().getWindow();
        File selectedDirectory = chooser.showDialog(currStage);

        System.out.println(selectedDirectory.getAbsolutePath());
        ArrayList gamessFiles = generateGamessInputFiles();
        FileOutputStream fos = new FileOutputStream(selectedDirectory.getAbsolutePath() + "/gamess.zip");
        ZipOutputStream zos = new ZipOutputStream(fos);
        for (int i = 0; i < gamessFiles.size(); i++) {

            byte[] b = ((String) gamessFiles.get(i)).getBytes(Charset.forName("UTF-8"));
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


    private static void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws FileNotFoundException, IOException {

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

    // Method to handle the submit action to selected server
    public void handleSubmit() throws IOException, InterruptedException {
        ServerDetails selectedServer = serverDetailsList.get(serversList.getSelectionModel().getSelectedIndex());
        System.out.println(selectedServer.getServerType());
        if (selectedServer.getServerType().equalsIgnoreCase("local"))
            submitJobToLocalServer(selectedServer);
        else {
            System.out.println("Ready");
            String hostname = "halstead.rcac.purdue.edu";
            Connection conn = new Connection(hostname);
            conn.connect();
            String username = "xu675";
            String password = "He00719614";
            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            if (!isAuthenticated)
                throw new IOException("Authentication failed.");
            Session sess = conn.openSession();
            sess.execCommand("ls");
            InputStream stdout = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                System.out.println(line);
            }
            System.out.println("ExitCode: " + sess.getExitStatus());
            br.close();
            sess.close();
            conn.close();

        }
        // Handle SSH case later
    }

    private void submitJobToLocalServer(ServerDetails selectedServer) throws IOException, InterruptedException {
//		// Printing the complete absolute path from where the application was initialized
//		System.out.println("Your working Directory is " + System.getProperty("user.dir"));
//	    String path = System.getProperty("user.dir");
//	    String executablePath = selectedServer.getWorkingDirectory();
//	    System.out.println("Executable path is: " + executablePath);
//	    String inputFileName = path + "/vmolAppJob_" + title.getText();
//	    String outputFileName = path += "/vmolAppJob_" + title.getText() + "_output";
//
//	    // First create the input File at that location with the content in qchemInputTextArea
//	    boolean inputFileCreated = createInputFile(inputFileName);
//	    if (!inputFileCreated) return; // Can probably return some error here
//	    List<String> command = new ArrayList<String>();
//	    command.add(executablePath);
//	    command.add(inputFileName);
//	    ProcessBuilder builder = new ProcessBuilder(command);
//	    builder.redirectOutput(new File(outputFileName));
//
//	    final Process process = builder.start();
//	    
//	    int errorCode = process.waitFor(); // 0 means everything went well!
//	    System.out.println("Process execution completed with errorCode : " + String.valueOf(errorCode)); 
//	    
//	    if (outputFileName.length() != 0) {
//	    	System.out.println("Printing output File contents: ");
//	    	Scanner s = null;
//		    try {
//		    	s = new Scanner(new File(outputFileName));
//		    	while (s.hasNextLine()) {
//		    		System.out.println(s.nextLine());
//		    	}
//		    } catch (Exception e){
//		    	e.printStackTrace(System.out);
//		    } finally {
//		    	if (s != null) s.close();
//		    }
//	    }
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
