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
import org.vmol.app.util.TermsofAgreement;
import org.vmol.app.visualizer.JmolMainPanel;
import org.vmol.app.visualizer.JmolPanel;

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

        TermsofAgreement terms = new TermsofAgreement();
        //terms.show();

        System.out.println("33366running...");
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
