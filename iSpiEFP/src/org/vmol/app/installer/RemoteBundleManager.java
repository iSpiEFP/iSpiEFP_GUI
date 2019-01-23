package org.vmol.app.installer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.vmol.app.Main;
import org.vmol.app.util.Atom;

import com.google.gson.Gson;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPOutputStream;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class RemoteBundleManager {
    
    private String username;
    private String password;
    private String hostname;
    private String bundleType;
    
    //private ArrayList<String>
    private final String NEW_USER = "NEW_USER";
    private final String USER_READY = "USER_READY";
    private final String LIBEFP = "LIBEFP";
    private final String GAMESS = "GAMESS";
    
    private static final String BASH_EFPMD_NOT_FOUND = "bash: efpmd: command not found";
    
    public RemoteBundleManager(String username, String password, String hostname, String bundleType) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.bundleType = bundleType;
    }
    
    private RemoteBundleManager() {
     
    }

    public boolean checkIfPackageIsReady() {
        try {
            String packageType = queryUserHistory();
            if(packageType.equals(USER_READY)) {
                //user has all packages installed and is ready to go
                return false;
            } else if (packageType.equals(NEW_USER)) {
                //user needs basic installation with package
                return true;
            } else {
                if(packageType.equals(this.bundleType)) {
                    //user is ready, and has the desired package
                    return false;
                } else {
                    //user needs to install this package
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    private String queryUserHistory() throws IOException {
        String userHistory = new String();
        String serverName = Main.iSpiEFP_SERVER;
        int port = Main.iSpiEFP_PORT;
    
        Socket client = new Socket(serverName, port);
        OutputStream outToServer = client.getOutputStream();

                
        RemoteBundleManager remoteBundleManager = new RemoteBundleManager(); 
        remoteBundleManager.username = this.username;
        remoteBundleManager.hostname = this.hostname;
        
        Gson gson = new Gson();
        String jsonQuery = gson.toJson(remoteBundleManager);
        System.out.println(jsonQuery);

        StringBuilder query = new StringBuilder("Bundle_Check");
        query.append("$END$");
        query.append(jsonQuery);
        query.append("$ENDALL$"); 
        
        System.out.println(query);
        outToServer.write(query.toString().getBytes("UTF-8"));
               
        InputStream inFromServer = client.getInputStream();
        DataInputStream in = new DataInputStream(inFromServer);
        StringBuilder sb = new StringBuilder();
        int i;
        char c;
        boolean start = false;
        while (( i = in.read())!= -1) {
            c = (char)i;
            sb.append(c);
        }
                
        String reply = sb.toString();
        System.out.println("Bundle Manager Response:"+reply);
        if(reply.length() == 0) {
            //install all directories
            return NEW_USER;
        } else {
            String [] types = reply.split("$NEXT$");
            if(types.length > 1){
                return USER_READY;
            } else {
                return reply;
            }
        }   
    }

    public boolean installMissingPackage(String bundleType) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Bundle Install Wizard");
        alert.setHeaderText("Looks like you are missing "+bundleType+" on the server:"+this.hostname);
        alert.setContentText("Would you like to install "+bundleType+" on the server, or give us the path location of where it is so we can set it?");
        Image image = new Image("file:wizard.png");
        ImageView imageView = new ImageView(image);
        alert.setGraphic(imageView);
        
        ButtonType install = new ButtonType("Install(doesnt work)");
        ButtonType setPath = new ButtonType("Set Path");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(install, setPath, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == install){
            // ... user chose "One"
            System.out.println("Installing the package:"+bundleType);
            if(bundleType.equals(GAMESS)) {
                return installGamess();
            } else if(bundleType.equals(LIBEFP)) {
                return installLibefp();
            }
        } else if (result.get() == setPath) {
            System.out.println("Setting the path");
            // ... user chose "Two"
            return setCustomPath(bundleType);
        } else {
            // ... user chose CANCEL or closed the dialog
            return false;
        }
        return false; 
    }
    
    private boolean installGamess() {
        System.out.println("This function needs to be implemented, no local installation of Gamess found");
        return false;
    }
    
    private boolean installLibefp() {
        System.out.println("This function needs to be implemented, no local installation of Libefp found");
        return false;
    }
    
    private boolean setCustomPath(String bundleType) {
        System.out.println("Setting a custom path for "+bundleType);
        TextInputDialog dialog = new TextInputDialog("/depot/lslipche/apps/libefp/libefp_yen_pairwise_july_2018_v5/efpmd/src");
        dialog.setTitle("Set Path");
        dialog.setContentText("Please enter the path of your executable:");
        
        Image image = new Image("file:wizard.png");
        ImageView imageView = new ImageView(image);
        dialog.setGraphic(imageView);
       //();
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            System.out.println("Your path: " + result.get());
            return setPathOnRemoteMachine(result.get(),bundleType);
        } else {
            return false;
    
        }
    }

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
            
            //SCPClient scp = conn.createSCPClient();
            
            Session sess = conn.openSession();
          
            String exportPath = "export PATH='$PATH:"+path+"';";
            
            String testPackage = "";
            if(bundleType.equals(LIBEFP)) {
                testPackage = "module load intel;efpmd;";
            } else if(bundleType.equals(GAMESS)) {
                
            }
            String script = defaultBundleScript + exportPath + testPackage;
            //need to exportPath and place in bash
            
            
            //String script = defaultBundleScript + exportPath;
           // export PATH="$PATH:/depot/lslipche/apps/libefp/libefp_yen_pairwise_july_2018_v5/efpmd/src"
            
            sess.execCommand(script);
            //sess.close();
            
            /*
            InputStream stdout = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            String jobID = "";
            
            System.out.println("Reading bundle manager output:");
            
            String line = br.readLine();
            while (line != null) {
                System.out.println(line);
                line = br.readLine();
                
                
            }
            br.close();
            sess.close();
            conn.close();
            System.out.println("Done reading it");
            */
           // ErrorStream stderr = new StreamGobbler(sess.getStderr());
            InputStream stderr = new StreamGobbler(sess.getStderr());
            BufferedReader br = new BufferedReader(new InputStreamReader(stderr));
            String jobID = "";
            
            System.out.println("Reading bundle manager output:");
            
            String lastLine = new String();
            String line = br.readLine();
            while (line != null) {
                System.out.println(line);
                lastLine = line;
                line = br.readLine();
                
            }
            
            br.close();
            sess.close();
            conn.close();
            System.out.println("Done reading it");
            
            System.out.println("last line:"+lastLine);
            
            if(lastLine.equals(BASH_EFPMD_NOT_FOUND)) {
                System.out.println("Path was incorrect!!!");
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Bundle Install Wizard");
                alert.setHeaderText(null);
                alert.setContentText("Your path:"+path+" was incorrect!\nPlease retry with another path pointing to the\ndirectory of efpmd");
                Image image = new Image("file:wizard.png");
                ImageView imageView = new ImageView(image);
                alert.setGraphic(imageView);
                Optional<ButtonType> result = alert.showAndWait();
                
                return false;
            }
            
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Bundle Install Wizard");
            alert.setHeaderText(null);
            alert.setContentText("Path Success! We won't ask you again for your path on this machine,\nunless you delete it :)");
            Image image = new Image("file:wizard.png");
            ImageView imageView = new ImageView(image);
            alert.setGraphic(imageView);
            Optional<ButtonType> result = alert.showAndWait();
            
            
            //System.out.println(jobID);
            //IF THE OUTPUT WORKS THEN ALLOW THE USER TO USE IT
            String updateBashConfig = "echo export PATH='$PATH:"+path+"' >> ~/.bashrc;";
            String readyUser = "source ~/.bashrc;";

            script = updateBashConfig + readyUser;
    
            
            
            
            conn = new Connection(hostname);

            conn.connect();
            
          
            isAuthenticated = conn.authenticateWithPassword(username, password);
            if (!isAuthenticated)
                throw new IOException("Authentication failed.");
            
            //SCPClient scp = conn.createSCPClient();
            
            sess = conn.openSession();
          
           
            
            //String script = defaultBundleScript + exportPath;
           // export PATH="$PATH:/depot/lslipche/apps/libefp/libefp_yen_pairwise_july_2018_v5/efpmd/src"
            
            sess.execCommand(script);
            
            
            //sess.execCommand(script);

            
         //   br.close();
            sess.close();
            conn.close();
            
            return true; //dangerous if user put a bad path
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
    
    private String getBuildDefaultRemoteDirectoryScript() {
        String buildDirectoriesScript = new StringBuilder()
                .append("cd;")
                .append("mkdir iSpiClient;")
                .append("cd iSpiClient;")
                .append("mkdir Gamess;")
                .append("mkdir Libefp;")
                .append("cd Gamess; mkdir input; mkdir src; mkdir output; cd ..;")
                .append("cd Libefp; mkdir fraglib; mkdir input; mkdir src; mkdir output; cd ..;")
                .toString();
        return buildDirectoriesScript;
    }
}