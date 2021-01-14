/*
 *     iSpiEFP is an open source workflow optimization program for chemical simulation which provides an interactive GUI and interfaces with the existing libraries GAMESS and LibEFP.
 *     Copyright (C) 2021  Lyudmila V. Slipchenko
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please direct all questions regarding iSpiEFP to Lyudmila V. Slipchenko (lslipche@purdue.edu)
 */

package org.ispiefp.app;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.ispiefp.app.MetaData.LocalFragmentTree;
import org.ispiefp.app.util.TermsofAgreement;
import org.ispiefp.app.util.UserPreferences;

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
    private static AnchorPane mainLayout;

    public static HostServices hostServices;

    public static LocalFragmentTree fragmentTree; /* Contains all currently available metaData */

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
        Thread mainThread = Thread.currentThread();
        Initializer initializer = new Initializer();
        initializer.init();
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            public void run() {
//                UserPreferences.setJobsMonitor(UserPreferences.getJobsMonitor().toJson());
//                try {
//                    mainThread.join();
//                } catch (InterruptedException e) {
//                    System.out.println("here");
//                    e.printStackTrace();
//                }
//            }
//        });

        Main.setPrimaryStage(primaryStage);
        Main.getPrimaryStage().setTitle("iSpiEFP");
        showMainView();
        //get User Default Browser
        hostServices = getHostServices();

        TermsofAgreement terms = new TermsofAgreement();
        //terms.show();

        System.out.println("33366running...");
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        UserPreferences.setJobsMonitorBeforeExit(UserPreferences.getJobsMonitor().toJson());
        System.exit(0);
    }

    /**
     * Loads the main stage with class resources, and specified settings.
     * The BundleManager is called to check and create directories
     *
     * @throws IOException when a resource or logo is failed to be found
     */
    private void showMainView() throws IOException {
        setMainLayout(FXMLLoader.load(getClass().getResource("/views/MainView.fxml")));

        getMainLayout().setScaleShape(true);
        getPrimaryStage().setScene(new Scene(getMainLayout()));
        getPrimaryStage().setOnCloseRequest(new EventHandler<javafx.stage.WindowEvent>() {
            public void handle(javafx.stage.WindowEvent we) {
                System.out.println("Stage is closing");
                Platform.exit();
            }
        });
        getPrimaryStage().setResizable(true);
        getPrimaryStage().setAlwaysOnTop(false);

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

    public static AnchorPane getMainLayout() {
        return mainLayout;
    }

    public static void setMainLayout(AnchorPane mainLayout) {
        Main.mainLayout = mainLayout;
    }
}
