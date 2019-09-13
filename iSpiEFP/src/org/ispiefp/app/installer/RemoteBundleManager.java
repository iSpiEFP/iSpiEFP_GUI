package org.ispiefp.app.installer;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.ispiefp.app.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * Manage remote directories, package testing, path setting, and package installation
 */
public class RemoteBundleManager {

    private String username;
    private String password;
    private String hostname;
    private String bundleType;
    private Connection conn;

    private final String NEW_USER = "NEW_USER";
    private final String USER_READY = "USER_READY";
    private final String LIBEFP = "LIBEFP";
    private final String GAMESS = "GAMESS";

    private static final String BASH_EFPMD_NOT_FOUND = "iSpiClient/Libefp/src/efpmd: cannot open (No such file or directory)";
    private static final String BASH_GMS_NOT_FOUND = "iSpiClient/Gamess/src/rungms: cannot open (No such file or directory)";

    public RemoteBundleManager(String username, String password, String hostname, String bundleType, Connection conn) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.bundleType = bundleType;
        this.conn = conn;
    }

    private RemoteBundleManager() {

    }

    /**
     * Check if the user has a valid package
     *
     * @return
     */
    public boolean checkIfPackageIsReady() {
        String errorStatement = null;
        try {
            errorStatement = testPackage(this.bundleType);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false; //bad connection
        }
        System.out.println("statement from test package");
        System.out.println(errorStatement);
        if (errorStatement.equals(BASH_EFPMD_NOT_FOUND)) {
            System.out.println("user missing EFP PAckage!");
            return false;
        } else if (errorStatement.equals(BASH_GMS_NOT_FOUND)) {
            System.out.println("user missing GAMESS Package!");
            return false;
        } else {
            System.out.println("user ready!");
            return true;
        }
    }

    /**
     * Launch the wizard that handles installation or path setting for the missing package
     *
     * @param bundleType type of bundle missing ex: "GAMESS" or "LIBEFP"
     * @return
     */
    public boolean installMissingPackage(String bundleType) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Bundle Install Wizard");
        alert.setHeaderText("Looks like you are missing " + bundleType + " on the server:" + this.hostname);
        alert.setContentText("Would you like to install " + bundleType + " on the server, or give us the path location of where it is so we can set it?");
        Image image = new Image(Main.class.getResource("/images/wizard.png").toString());
        ImageView imageView = new ImageView(image);
        alert.setGraphic(imageView);

        ButtonType install = new ButtonType("Install(doesnt work dont click)");


        ButtonType setPath = new ButtonType("Set Path");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(install, setPath, buttonTypeCancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == install) {
            // ... user chose "One"
            System.out.println("Installing the package:" + bundleType);
            if (bundleType.equals(GAMESS)) {
                return installGamess();
            } else if (bundleType.equals(LIBEFP)) {
                return installLibefp();
            }
        } else if (result.get() == setPath) {
            System.out.println("Setting the path");
            return setCustomPath(bundleType);
        } else {
            // ... user chose CANCEL or closed the dialog
            return false;
        }
        return false;
    }

    //TODO
    private boolean installGamess() {
        System.out.println("This function needs to be implemented, no local installation of Gamess found");
        return false;
    }

    //TODO
    private boolean installLibefp() {
        System.out.println("This function needs to be implemented, no local installation of Libefp found");
        return false;
    }

    //Set a user defined path
    private boolean setCustomPath(String bundleType) {
        System.out.println("Setting a custom path for " + bundleType);
        TextInputDialog dialog;
        if (bundleType.equals(GAMESS)) {

            TextInputDialog gamess_dialog = new TextInputDialog("/group/lslipche/apps/gamess/gamess_2018_feb_yb/rungms");
            dialog = gamess_dialog;
            dialog.setTitle("Set Path");
            dialog.setContentText("Please enter the path of your executable:");

            Image image = new Image("file:wizard.png");
            ImageView imageView = new ImageView(image);
            dialog.setGraphic(imageView);
            //();

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                System.out.println("Your path: " + result.get());
                return setPathOnRemoteMachine(result.get(), bundleType);
            } else {
                return false;

            }
        } else if (bundleType.equals(LIBEFP)) {
            TextInputDialog efp_dialog = new TextInputDialog("/depot/lslipche/apps/libefp/libefp_yen_pairwise_july_2018_v5/efpmd/src/efpmd");
            dialog = efp_dialog;
            dialog.setTitle("Set Path");
            dialog.setContentText("Please enter the path of your executable:");

            Image image = new Image(Main.class.getResource("/images/wizard.png").toString());
            ImageView imageView = new ImageView(image);
            dialog.setGraphic(imageView);

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                System.out.println("Your path: " + result.get());
                return setPathOnRemoteMachine(result.get(), bundleType);
            } else {
                return false;

            }
        }
        return false;
    }

    /**
     * Set path on a remote machine to the specified package
     *
     * @param path       the path of the package
     * @param bundleType the type of package
     * @return True if package installation success, false if invalid package path
     */
    private boolean setPathOnRemoteMachine(String path, String bundleType) {
        // TODO Auto-generated method stub
        String defaultBundleScript = getBuildDefaultRemoteDirectoryScript();

        Connection conn = new Connection(hostname);
        try {
            conn.connect();

            String username = this.username;
            String password = this.password;

            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            if (!isAuthenticated)
                throw new IOException("Authentication failed.");

            Session sess = conn.openSession();

            String packagePath = path;
            if (bundleType.equals(LIBEFP)) {
                path = path + " iSpiClient/Libefp/src/;";
            } else if (bundleType.equals(GAMESS)) {
                path = path + " iSpiClient/Gamess/src/;";
            }
            String script = defaultBundleScript + "cp " + path;

            sess.execCommand(script);
            sess.close();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String errorStatement = testPackage(this.bundleType);
            System.out.println("printing error stream");
            System.out.println(errorStatement);

            String lastLine = errorStatement;


            if (lastLine.equals(BASH_EFPMD_NOT_FOUND) || lastLine.equals(BASH_GMS_NOT_FOUND)) {
                System.out.println("Path was incorrect!!!");
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Bundle Install Wizard");
                alert.setHeaderText(null);
                alert.setContentText("Your path:" + packagePath + " was incorrect!\nPlease retry with another path pointing to " + this.bundleType);
                Image image = new Image(Main.class.getResource("/images/wizard.png").toString());
                ImageView imageView = new ImageView(image);
                alert.setGraphic(imageView);
                Optional<ButtonType> result = alert.showAndWait();

                return false;

            } else if (bundleType.equals(GAMESS)) {
                //configure rungms script for user paths
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                boolean configureSuccess = runGMSConfigureScript(packagePath);

                if (!configureSuccess) {
                    //this is unhandled right now
                    System.out.println("unhandled exception in remote installer set path");
                    return false;
                }
            }

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Bundle Install Wizard");
            alert.setHeaderText(null);
            alert.setContentText("Path Success! We won't ask you again for your path on this machine,\nunless you delete it :)");
            Image image = new Image(Main.class.getResource("/images/wizard.png").toString());
            ImageView imageView = new ImageView(image);
            alert.setGraphic(imageView);
            Optional<ButtonType> result = alert.showAndWait();


            //System.out.println(jobID);
            //IF THE OUTPUT WORKS THEN ALLOW THE USER TO USE IT
            String updateBashConfig = "echo export PATH='$PATH:" + path + "' >> ~/.bashrc;";
            String readyUser = "source ~/.bashrc;";

            script = updateBashConfig + readyUser;

            conn = new Connection(hostname);
            conn.connect();


            isAuthenticated = conn.authenticateWithPassword(username, password);
            if (!isAuthenticated)
                throw new IOException("Authentication failed.");


            sess = conn.openSession();
            sess.execCommand(script);

            sess.close();
            conn.close();

            return true; //dangerous if user put a bad path
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    //test to see if package runs like expected 
    //read error output to see if any unexpected stuff happens
    private String testPackage(String packageType) throws IOException {
        StringBuilder errorString = new StringBuilder();
        Connection conn = this.conn;

        Session sess = conn.openSession();
        String script = new String();
        if (packageType.equals(LIBEFP)) {
            script = "file iSpiClient/Libefp/src/efpmd";
        } else if (packageType.equals(GAMESS)) {
            script = "file iSpiClient/Gamess/src/rungms";
        }
        sess.execCommand(script);

        InputStream stderr = new StreamGobbler(sess.getStdout());
        BufferedReader br = new BufferedReader(new InputStreamReader(stderr));
        String jobID = "";

        System.out.println("Reading bundle manager output:");

        String lastLine = new String();
        String line = br.readLine();
        while (line != null) {
            errorString.append(line);
            lastLine = line;
            line = br.readLine();
        }

        br.close();
        sess.close();
        return errorString.toString();
    }

    /**
     * Parse the Gamess Config script to modify it for iSpiEFP options
     *
     * @param gmsPath
     * @return
     * @throws IOException
     */
    private boolean runGMSConfigureScript(String gmsPath) throws IOException {
        //parse gamess file path
        //strip rungms from path
        gmsPath = gmsPath.replace("/rungms", "");
        //load with escapes
        gmsPath = gmsPath.replace("/", "\\/");

        StringBuilder errorString = new StringBuilder();
        Connection conn = this.conn;

        Session sess = conn.openSession();
        String script = new String();
        script += "sed -i 's/set SCR=${RCAC_SCRATCH}/set SCR=\\/home\\/$USER\\/iSpiClient\\/Gamess\\/output/g' /home/$USER/iSpiClient/Gamess/src/rungms;";
        script += "sed -i 's/set USERSCR=\\/home\\/$USER\\/scr/set USERSCR=\\/home\\/$USER\\/iSpiClient\\/Gamess\\/output/g' /home/$USER/iSpiClient/Gamess/src/rungms;";

        sess.execCommand(script);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        InputStream stderr = new StreamGobbler(sess.getStdout());
        BufferedReader br = new BufferedReader(new InputStreamReader(stderr));
        String jobID = "";

        System.out.println("Reading bundle manager output:");

        String lastLine = new String();
        String line = br.readLine();
        while (line != null) {
            errorString.append(line);
            lastLine = line;
            line = br.readLine();
        }

        br.close();
        sess.close();
        stderr.close();
        return true;
    }

    /**
     * Create Script to build the default remote directory setup
     *
     * @return
     */
    private String getBuildDefaultRemoteDirectoryScript() {
        String buildDirectoriesScript = new StringBuilder()
                .append("cd;")
                .append("mkdir iSpiClient;")
                .append("cd iSpiClient;")
                .append("mkdir Gamess;")
                .append("mkdir Libefp;")
                .append("cd Gamess; mkdir input; mkdir src; mkdir output; cd ..;")
                .append("cd Libefp; mkdir fraglib; mkdir input; mkdir src; mkdir output; cd ~;")
                .toString();
        return buildDirectoriesScript;
    }

}