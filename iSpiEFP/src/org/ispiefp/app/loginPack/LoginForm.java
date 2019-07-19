package org.ispiefp.app.loginPack;

import ch.ethz.ssh2.Connection;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.util.Pair;
import org.ispiefp.app.installer.BundleManager;
import org.ispiefp.app.server.ServerConfigController;
import org.ispiefp.app.server.ServerDetails;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Presents a LoginForm that handles SSH auth
 */
public class LoginForm {

    private String username;
    private String password;
    private String hostname;
    private String formType;
    private String bundleType;
    private Connection connection;

    private boolean cancel;
    private boolean _remember = false;

    private final String CANCEL = "CANCEL";
    private final String INVALID = "INVALID";
    private final String VALID = "VALID";
    private final String FAILED_TO_CONNECT = "FAILED_TO_CONNECT";

    private final String DEFAULT = "DEFAULT";
    private final String SERVER_FORM = "SERVER_FORM";

    //These relate to random values, in a weak attempt to obfuscate them in local memory
    private static final String USERNAME = "!=U+Cyc^Z8VqvZuW+c$-";
    private static final String REMEMBER_ME = "+ue&$LT7=g%uDWfcNy6p";
    private static final String PASSWORD = "aA%v$mMcjwHE$^&^j5u_";

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
        if (_remember) {
            //overwrite preferences - May not work for windows computers
            if (username != null) {
                Preferences prefs = Preferences.userNodeForPackage(LoginForm.class);
                //prefs.put(USERNAME, Security.encrypt(username));
                prefs.put(USERNAME, username);
            }

        }
        this.username = username;
    }

    private void setPassword(String password) {
        if (_remember) {
            //overwrite preferences
            if (password != null) {
                Preferences prefs = Preferences.userNodeForPackage(LoginForm.class);
                //prefs.put(PASSWORD, Security.encrypt(password));
                prefs.put(PASSWORD, password);
            }
        }
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

    private void setValidConnection(Connection conn) {
        this.connection = conn;
    }

    public Connection getConnection(boolean valid) {
        if (valid) {
            return this.connection;
        } else {
            return null;
        }
    }

    /**
     * Attempt to authenticate user, on success check if the package the user is trying to use is valid
     *
     * @return boolean value, true on success, false on failure
     */
    public boolean authenticate() {
        String response = new String();
        do {
            response = handleAuth();
        } while (response.equals(INVALID));

        switch (response) {
            case VALID:
                BundleManager bundleManager = new BundleManager(this.username, this.password, this.hostname, this.bundleType, this.connection);
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

    /**
     * Handle user authentication
     *
     * @return
     */
    private String handleAuth() {
        if (this.formType.equals(DEFAULT)) {
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

        if (getCancel() == true) {
            return CANCEL;
        } else {

            Connection conn = new Connection(this.hostname);
            try {

                conn.connect();
                try {
                    String username = getUsername();
                    String password = getPassword();
                    boolean isAuthenticated = conn.authenticateWithPassword(username, password);

                    if (!isAuthenticated) {
                        conn.close();
                        return INVALID;
                    } else {
                        setValidConnection(conn);
                        return VALID;
                    }
                } catch (IOException e) {
                    conn.close();
                    return INVALID;
                }
            } catch (IOException e) {
                conn.close();
                return FAILED_TO_CONNECT;
            }
        }
    }

    /**
     * Init Defualt Login Form
     * default form contains only the username, password, and remember me field
     */
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

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        /*
         * Remember Me Field Notes
         *
         * This stores password and username in local computer
         * Java Preferences
         * The password will be encrypted however since it will
         * be encrypted on local machines it will not be 100% safe
         * Thus it is not recommended to remember user.
         */
        //Remember Me Field
        CheckBox rememberMe = new CheckBox();
        final Tooltip tooltip = new Tooltip("Not Recommended");
        tooltip.setFont(new Font("Arial", 16));
        rememberMe.setTooltip(tooltip);

        //Check if box has been selected
        Preferences prefs = Preferences.userNodeForPackage(LoginForm.class);
        String remember = prefs.get(REMEMBER_ME, "FALSE");
        if (remember.equals("TRUE")) {
            rememberMe.setSelected(true);
            _remember = true;
        } else {
            rememberMe.setSelected(false);
            _remember = false;
        }

        if (_remember) {
            //set fields to values
            String uname = prefs.get(USERNAME, null);
            String psw = prefs.get(PASSWORD, null);
            if (uname != null) {
                //username.setText(Security.decrypt(uname));
                username.setText((uname));
                loginButton.setDisable(false);

            }
            if (psw != null) {
                //password.setText(Security.decrypt(psw));
                password.setText((psw));
                loginButton.setDisable(false);
            }
        }

        //event handler for remember me checkbox 
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                Preferences prefs = Preferences.userNodeForPackage(LoginForm.class);
                if (rememberMe.isSelected()) {
                    _remember = true;
                    prefs.put(REMEMBER_ME, "TRUE");
                } else {
                    _remember = false;
                    prefs.put(REMEMBER_ME, "FALSE");
                }
            }

        };
        // set event to checkbox 
        rememberMe.setOnAction(event);
        ////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        grid.add(new Label("Remember me:"), 0, 2);
        grid.add(rememberMe, 1, 2);

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

        if (!result.isPresent()) {
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

    /**
     * notify user that there was a bad connection, yet username and password were valid
     *
     * @param hostname
     */
    private void notifyBadConnection(String hostname) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(AlertType.ERROR);
                String msg = "Unable to connect to: " + hostname;

                alert.setTitle("Login");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                Optional<ButtonType> result = alert.showAndWait();
            }
        });
    }

    @SuppressWarnings("unchecked")
    /**
     * Initialize the Server Form which contains username, password, remember me, and server selection
     * This form demands that the user select a host to login
     */
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

        //Username Text Field
        TextField username = new TextField();
        username.setPromptText("Username");

        //Password Text Field
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        /*
         * Remember Me Field Notes
         *
         * This stores password and username in local computer
         * Java Preferences
         * The password will be encrypted however since it will
         * be encrypted on local machines it will not be 100% safe
         * Thus it is not recommended to remember user.
         */
        //Remember Me Field
        CheckBox rememberMe = new CheckBox();
        final Tooltip tooltip = new Tooltip("Not Recommended");
        tooltip.setFont(new Font("Arial", 16));
        rememberMe.setTooltip(tooltip);

        //Check if box has been selected
        Preferences prefs = Preferences.userNodeForPackage(LoginForm.class);
        String remember = prefs.get(REMEMBER_ME, "FALSE");
        if (remember.equals("TRUE")) {
            rememberMe.setSelected(true);
            _remember = true;
        } else {
            rememberMe.setSelected(false);
            _remember = false;
        }

        if (_remember) {
            //set fields to values
            String uname = prefs.get(USERNAME, null);
            String psw = prefs.get(PASSWORD, null);
            if (uname != null) {
                //username.setText(Security.decrypt(uname));
                loginButton.setDisable(false);
                username.setText((uname));
            }
            if (psw != null) {
                //password.setText(Security.decrypt(psw));
                loginButton.setDisable(false);
                password.setText((psw));
            }
        }

        //event handler for remember me checkbox 
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                Preferences prefs = Preferences.userNodeForPackage(LoginForm.class);
                if (rememberMe.isSelected()) {
                    _remember = true;
                    prefs.put(REMEMBER_ME, "TRUE");
                } else {
                    _remember = false;
                    prefs.put(REMEMBER_ME, "FALSE");
                }
            }

        };
        // set event to checkbox 
        rememberMe.setOnAction(event);

        ////////////////////////////////////////////////////////////////////////////////////

        //load server selection choices
        String defaultServer = "";
        @SuppressWarnings("rawtypes") final ComboBox comboBox = new ComboBox();
        ServerConfigController serverConfig = new ServerConfigController();
        try {
            List<ServerDetails> savedList = serverConfig.getServerDetailsList();
            int i = 0;
            for (ServerDetails details : savedList) {
                System.out.println(details.getAddress());
                // System.out.println(details.getServerName());
                // System.out.println(savedList);
                if (i == 1) {
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

        //event handler for server selection
        comboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(@SuppressWarnings("rawtypes") ObservableValue ov, String t, String t1) {
                String address = t1;
                System.out.println("Selected:" + address);
                setHostname(address);
            }
        });


        //Add Fields
        grid.add(new Label("Server:"), 0, 0);
        grid.add(comboBox, 1, 0);
        grid.add(new Label("Username:"), 0, 1);
        grid.add(username, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(password, 1, 2);
        grid.add(new Label("Remember me:"), 0, 3);
        grid.add(rememberMe, 1, 3);

        ////////////////////////////////////////////////////

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

        if (!result.isPresent()) {
            //user hit cancel
        } else {
            result.ifPresent(usernamePassword -> {
                System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());


                //update username password
                setUsername(usernamePassword.getKey());
                setPassword(usernamePassword.getValue());
                setCancel(false);
            });
        }
    }
}
