package org.vmol.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.util.Logger;
import org.jmol.viewer.Viewer;
import org.vmol.app.installer.BundleManager;
import org.vmol.app.visualizer.JmolMainPanel;
import org.vmol.app.visualizer.JmolPanel2;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;


public class Main extends Application {

    /**
     * iSpiEFP Server Credentials
     * <p>
     * Here are the server credentials to the iSpiEFP server
     * Development Server: Server where developing takes place for development
     * Test Server: Server constantly running for current iSpiEFP(jar file release) for real users
     */
    //public static final String iSpiEFP_SERVER = "ec2-18-220-105-41.us-east-2.compute.amazonaws.com"; //This is the Development Server
    public static final String iSpiEFP_SERVER = "ec2-3-16-11-177.us-east-2.compute.amazonaws.com"; //This is the Test Server
    public static final int iSpiEFP_PORT = 8080;

    private static Stage primaryStage;
    private static BorderPane mainLayout;

    /**
     * The Main function which starts iSpiEFP
     *
     * @param args javaFX arguments to be loaded
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Main.setPrimaryStage(primaryStage);
        Main.getPrimaryStage().setTitle("iSpiEFP");
        showMainView();        //showJmolViewer(true, null);
        
        //Show JMOL Main Panel
        SplitPane splitpane = (SplitPane) Main.getMainLayout().getChildren().get(2);
        ObservableList<Node> list = splitpane.getItems();
        SplitPane nodepane = (SplitPane) list.get(1);
        ObservableList<Node> sublist = nodepane.getItems();
        Pane pane = (Pane) sublist.get(0);
        JmolMainPanel jmolMainViewer = new JmolMainPanel(pane, null);

        //optional terms of agreement for test jar files
        Alert alert = new Alert(AlertType.WARNING);
        String msg = "Welcome to iSpiEFP\n\n"
                + "I acknowledge that this is a Pre-Alpha Release which means my jobs and data can be damaged or lost.\n"
                + "My credentials will be safe however.\n"
                + "This application will install a directory: '/iSpiClient' on a remote machine and\n"
                + "a working directory on the local machine in the app's current directory: '/iSpiWorkspace for storing data'\n\n"
                + "This product is new and will have lots of bugs. There are no loading bars so please wait\n"
                + "a few seconds while submitting jobs, searching for parameters, and configuring servers.\n"
                + "If I encounter a freeze or serious issue I will shake the app or restart it.\n"
                + "If I encounter an issue I will report it.\n";
        alert.setTitle("Terms of Agreement");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        //Optional<ButtonType> result = alert.showAndWait(); //Terms of Agreement
    }

    /**
     * Loads the main stage with class resources, and specified settings.
     * The BundleManager is called to check and create directories
     *
     * @throws IOException when a resource or logo is failed to be found
     */
    private void showMainView() throws IOException {
        setMainLayout(FXMLLoader.load(getClass().getResource("MainView.fxml")));

        getMainLayout().setScaleShape(true);
        getPrimaryStage().setScene(new Scene(getMainLayout()));
        getPrimaryStage().setOnCloseRequest(new EventHandler<javafx.stage.WindowEvent>() {
            public void handle(javafx.stage.WindowEvent we) {
                System.out.println("Stage is closing");
                System.exit(0);
            }
        });
        getPrimaryStage().setResizable(false);
        //primaryStage.initModality(Modality.WINDOW_MODAL);
        getPrimaryStage().setAlwaysOnTop(false);
        getPrimaryStage().setHeight(700);
        getPrimaryStage().setWidth(1200);

        //add iSpiEFP Spider icon
        String url = Main.class.getResource("/images/iSpiEFP_Logo.png").toString();
        getPrimaryStage().getIcons().add(new Image(url));

        //Manage Working Directory
        BundleManager bundleManager = new BundleManager("LOCAL");
        bundleManager.manageLocal();

        //launch stage
        getPrimaryStage().show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setPrimaryStage(Stage primaryStage) {
        Main.primaryStage = primaryStage;
    }

    public static BorderPane getMainLayout() {
        return mainLayout;
    }

    public static void setMainLayout(BorderPane mainLayout) {
        Main.mainLayout = mainLayout;
    }
}
