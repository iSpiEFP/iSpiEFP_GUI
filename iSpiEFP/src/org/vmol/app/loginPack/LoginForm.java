package org.vmol.app.loginPack;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;

import org.vmol.app.installer.BundleManager;
import org.vmol.app.server.ServerConfigController;
import org.vmol.app.server.ServerDetails;

import ch.ethz.ssh2.Connection;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class LoginForm {
    
    private String username;
    private String password;
    private String hostname;
    private String formType;
    private String bundleType;
    
    private boolean cancel;
    
    private final String CANCEL = "CANCEL";
    private final String INVALID = "INVALID";
    private final String VALID = "VALID";
    private final String FAILED_TO_CONNECT = "FAILED_TO_CONNECT";
    
    private final String DEFAULT = "DEFAULT";
    private final String SERVER_FORM = "SERVER_FORM";
    
    public LoginForm(String hostname, String bundleType) {
        this.hostname = hostname;
        this.username = null;
        this.password = null;
        this.bundleType = bundleType;
        
        this.cancel = true;
        this.formType = DEFAULT;
    }
    
    public LoginForm(String bundleType) {
        this.hostname = null;
        this.username = null;
        this.password = null;
        this.bundleType = bundleType; 
        
        this.cancel = true;
        this.formType = SERVER_FORM;
    }

    private void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    public String getHostname() {
        return this.hostname;
    }
    
    private void setUsername(String username) {
        this.username = username;
    }
    
    private void setPassword(String password) {
        this.password = password;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    private void setCancel(boolean value) {
        this.cancel = value;
    }
    
    private boolean getCancel() {
        return this.cancel;
    }
    
    public boolean authenticate() {
        String response = new String();
        do {
            response = handleAuth();        
        } while(response.equals(INVALID));
        
        switch (response) {
            case VALID:  
                BundleManager bundleManager = new BundleManager(this.username, this.password, this.hostname, this.bundleType);
                boolean installed = bundleManager.manageRemote();
                return installed;
            case FAILED_TO_CONNECT:  
                notifyBadConnection(this.hostname);
                return false;
            case CANCEL:  
                return false;
            default:
                return false;
        }
    }
    
    private String handleAuth() { 
        if(this.formType.equals(DEFAULT)) {
            initializeDefaultForm();

        } else {
            try {
                initializeServerForm();
            } catch (BackingStoreException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        
        if(getCancel() == true) {
            return CANCEL;
        } else {
            Connection conn = new Connection(this.hostname);
            try {
                conn.connect();
                try {
                    String username = getUsername();
                    String password = getPassword();
                    boolean isAuthenticated = conn.authenticateWithPassword(username, password);
                    conn.close();
                    
                    if (!isAuthenticated) {
                        return INVALID;
                    } else {
                        return VALID;
                    } 
                } catch (IOException e) {
                    return INVALID;
                }
            } catch (IOException e) {
                return FAILED_TO_CONNECT;
            }
        }
    }
    
    private void initializeDefaultForm() {
        setPassword(null);
        setUsername(null);
        setCancel(true);
        
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        
        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        
        
        ////////////////////////////////////////////////////////////////////////////////////
        username.setText("apolcyn");
        password.setText("P15mac&new");
        
        
        
        /////////////////////////////////////////////////////////////////////////////////////////

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);
        
        ////////////////////////////////////////////////////
        loginButton.setDisable(false);
        
        /////////////////////////////////////////////////

        // Do some validation (using the Java 8 lambda syntax).
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> username.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        
        if(!result.isPresent()) {
            //user hit cancel
        } else {
            result.ifPresent(usernamePassword -> {
                System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
                setUsername(usernamePassword.getKey());
                setPassword(usernamePassword.getValue());
                setCancel(false);
            });
        }
    }
    
    private void notifyBadConnection(String hostname) {
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                Alert alert = new Alert(AlertType.ERROR);
                String msg = "Unable to connect to: "+hostname;
                
                alert.setTitle("Login");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                Optional<ButtonType> result = alert.showAndWait();
            }
       });
    }
    
    @SuppressWarnings("unchecked")
    private void initializeServerForm() throws BackingStoreException, IOException {
        setPassword(null);
        setUsername(null);
        setHostname(null);
        setCancel(true);
        
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        
        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        
        
        ////////////////////////////////////////////////////////////////////////////////////
        username.setText("apolcyn");
        password.setText("P15mac&new");
        
        
        
        /////////////////////////////////////////////////////////////////////////////////////////
        String defaultServer = "";
        
        @SuppressWarnings("rawtypes")
        final ComboBox comboBox = new ComboBox();
        ServerConfigController serverConfig = new ServerConfigController();
        try {
            List<ServerDetails> savedList = serverConfig.getServerDetailsList();
            int i = 0;
            for(ServerDetails details: savedList){
                System.out.println(details.getAddress());
               // System.out.println(details.getServerName());
               // System.out.println(savedList);
                if(i == 1) {
                    //defaultServer = details.getAddress();
                    this.hostname = details.getAddress();
                }
                comboBox.getItems().add(details.getAddress());
                i++;
            }
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        comboBox.setPromptText("Server address");
        comboBox.setValue(this.hostname);
        comboBox.setValue("hoicheso");

        comboBox.setEditable(true);        
        comboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override 
            public void changed(@SuppressWarnings("rawtypes") ObservableValue ov, String t, String t1) {                
                String address = t1; 
                System.out.println("Selected:"+address);
                setHostname(address);
            }    
        });
        
        grid.add(new Label("Server:"), 0, 0);
        grid.add(comboBox, 1, 0);
        grid.add(new Label("Username:"), 0, 1);
        grid.add(username, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(password, 1, 2);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);
        
        ////////////////////////////////////////////////////
        loginButton.setDisable(false);
        
        /////////////////////////////////////////////////

        // Do some validation (using the Java 8 lambda syntax).
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> username.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        
        if(!result.isPresent()) {
            //user hit cancel
        } else {
            result.ifPresent(usernamePassword -> {
                System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
                setUsername(usernamePassword.getKey());
                setPassword(usernamePassword.getValue());
                setCancel(false);
            });
        }
    }
}
