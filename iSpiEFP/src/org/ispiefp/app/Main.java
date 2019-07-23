package org.ispiefp.app;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.ispiefp.app.installer.BundleManager;
import org.ispiefp.app.util.TermsofAgreement;

import java.io.IOException;


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
        showMainView();

        //Manage Working Directory
        BundleManager bundleManager = new BundleManager("LOCAL");
        bundleManager.manageLocal();

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
        getPrimaryStage().setResizable(true);
        //primaryStage.initModality(Modality.WINDOW_MODAL);
        getPrimaryStage().setAlwaysOnTop(false);
        getPrimaryStage().setHeight(700);
        getPrimaryStage().setWidth(1200);

        //add iSpiEFP Spider icon
        String url = Main.class.getResource("/images/iSpiEFP_Logo.png").toString();
        getPrimaryStage().getIcons().add(new Image(url));

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