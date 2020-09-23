package org.ispiefp.app.analysis;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.ispiefp.app.libEFP.OutputFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

public class GeometryAnalysisController implements Initializable {
    /* Generic class variables */
    @FXML LineChart<Integer, Double> chart;
    @FXML NumberAxis xAxis;
    @FXML NumberAxis yAxis;
    @FXML Button leftArrow;
    @FXML Button playPause;
    @FXML Button rightArrow;
    @FXML Button autoSize;
    @FXML Button exportPNG;
    @FXML Button exportCSV;
    @FXML ComboBox<String> unitsSelector;
    @FXML TextField customXBound;
    @FXML TextField customYBound;
    final String[] unitTypes = {"hartrees, kcal/mol, kJ/mol, cm^-1"};
    Map<String, Double> fromHartreeMap;
    Map<String, Double> toHartreeMap;
    double maxXVal;
    double maxYVal;
    double upperXBound;
    double upperYBound;

    /* Cursor Logic */
    boolean xPressed;
    double lastXPosition;
    boolean yPressed;
    double lastYPosition;

    /* Instance specific variables */
    OutputFile outputFile;
    XYChart.Series<Number, Number> dataSeries;

    public GeometryAnalysisController(){
        super();
        fromHartreeMap = new HashMap<>();
        fromHartreeMap.put("hartrees", 1.0); //1 hartree : 1.0 hartrees
        fromHartreeMap.put("kcal/mol", 627.5); //1 hartree : 627.5 kcal/mol
        fromHartreeMap.put("kJ/mol", 2625.5); //1 hartree : 2625.5 kJ/mol
        fromHartreeMap.put("eV/mol", 27.211); //1 hartree : 27.211 eV
        fromHartreeMap.put("cm^-1", 219474.6); //1 hartree : 219474.6 cm^-1
        toHartreeMap = new HashMap<>();
        toHartreeMap.put("hartrees", 1.0);
        toHartreeMap.put("kcal/mol", 1.0/627.5);
        toHartreeMap.put("kJ/mol", 1.0/2625.5);
        toHartreeMap.put("eV/mol", 1.0/27.211);
        toHartreeMap.put("cm^-1", 1.0/219474.6);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        maxXVal = 0;
        maxYVal = 0;
        upperXBound = maxXVal + 1;
        upperYBound = maxYVal + 10;
        /* Set Unit converter options */
        unitsSelector.setItems(FXCollections.observableArrayList(unitTypes));

        /* Register Event Handler for selection of Units */
        unitsSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<XYChart.Series<Integer, Double>> currentData = chart.getData();
            Map<Integer, Double> convertedMap = new TreeMap<>();
            updateYAxis();
            Iterator<XYChart.Data<Integer, Double>> seriesIterator = currentData.get(0).getData().iterator();
            while (seriesIterator.hasNext()) {
                XYChart.Data<Integer, Double> entry = seriesIterator.next();
                convertedMap.put(entry.getXValue(), convert(oldValue, newValue) * entry.getYValue());
            }
            for (Integer step : convertedMap.keySet())
                currentData.get(0).getData().add(new XYChart.Data<>(step, convertedMap.get(step)));
        });
        unitsSelector.getSelectionModel().selectFirst();

        /* Register Event Handler for autosize */
        autoSize.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                int xPow = getExponent(maxXVal);
                int yPow = getExponent(maxYVal);

                xAxis.setUpperBound(maxXVal + Math.pow(10, xPow));
                yAxis.setUpperBound(maxYVal + Math.pow(10, yPow));

            }
        }));

        /* Register Event Handlers for x axis */
        xAxis.setOnMousePressed((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                xPressed = true;
                lastXPosition = event.getSceneX();
            }
        }));

        xAxis.setOnMouseDragged((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (xPressed) {
                    if (event.getSceneX() - lastXPosition >= 20.0) {
                        xAxis.setAutoRanging(false);
                        double tempMaxXVal = upperXBound * 2;
                        xAxis.setUpperBound(tempMaxXVal);
                        upperXBound *= 2;

                        xPressed = false;
                        System.out.println("Finished right drag logic");

                    } else if (lastXPosition - event.getSceneX() >= 20.0) {
                        xAxis.setAutoRanging(false);
                        //xAxis.setTickUnit();
                        xAxis.setUpperBound(Math.ceil(upperXBound / 2));
                        upperXBound /= 2;
                        xPressed = false;

                    }
                }
            }
        }));

        /* Register Event handlers for Y Axis */
        yAxis.setOnMousePressed((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                lastYPosition = event.getSceneY();
                yPressed = true;
            }
        }));

        yAxis.setOnMouseDragged((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                //If the user drags right
                if (yPressed) {
                    if (lastYPosition - event.getSceneY() >= 20.0) {
                        yAxis.setAutoRanging(false);
                        yAxis.setUpperBound(upperYBound * 2);
                        upperYBound *= 2;
                        yPressed = false;
                        // System.out.println("Finished up drag logic");
                    }
                    else if (event.getSceneY() - lastYPosition >= 20.0) {
                        yAxis.setAutoRanging(false);
                        yAxis.setUpperBound(upperYBound / 2);
                        upperYBound /= 2;
                        yPressed = false;
                    }
                }
            }
        }));
    }

    public int getExponent(double bound) {
        int pow = 0;
        int boundInt = (int) bound;

        boundInt /= 10;
        while (boundInt != 0) {
            pow++;
            boundInt /= 10;
        }
        return pow;
    }


    public void updateYAxis(){
        yAxis.setLabel(String.format("Energy (%s)", unitsSelector.getValue()));
    }

    public void updateChartData(Map<Integer, Double> map){
        dataSeries.getData().clear();
        for (Integer xValue : map.keySet()){
            Double yValue = map.get(xValue);
            if (xValue > maxXVal){
                maxXVal = xValue;
                upperXBound = xValue + 1;
            }
            if (yValue > maxYVal){
                maxYVal = yValue;
                upperYBound = yValue + 1;
            }
            dataSeries.getData().add(new XYChart.Data<>(xValue, yValue));
        }
        /* This may not update the graph. If it doesn't, wrap it in an observable list and pass to chart */
    }

    public void setOutputFile(OutputFile outputFile) {
        this.outputFile = outputFile;
        Map<Integer, Double> dataMap = new HashMap<>();
        for (OutputFile
        )
    }

    public Double convert(String oldUnits, String newUnits) {
        return toHartreeMap.get(oldUnits) * fromHartreeMap.get(newUnits);
    }

    //    private String energyUnits;
//
//
//    public class EMGraph {
//        private String title;
//        private String energyUnits;
//        @FXML private NumberAxis yAxis;
//        @FXML private NumberAxis xAxis;
//
//        public EMGraph() {
//            title = "Energy vs. Steps";
//            energyUnits = "Hartrees";
//            yAxis.setLabel("")
//        }
//    }
//    void displayEnergyMinimizationGraph(EMGraph graph){
//        LineChart chart = new LineChart(graph.xAxis, graph.yAxis);
//        chart.setTitle(graph.title);
//    }
//    public void showGeomAnalysis(OutputFile outFile) throws IOException {
//        ArrayList states = outFile.getStates();
//
//        Parent geomAnalysis = FXMLLoader.load(getClass().getResource("views/analysisViews/GeometryAnalysisView.fxml"));
//        Stage stage = new Stage();
//        stage.setTitle("Geometry Analysis");
//        stage.initModality(Modality.WINDOW_MODAL);
//        stage.setScene(new Scene(geomAnalysis));
//        stage.show();
//
//        HashMap<Integer, Integer> graphDataMap = new HashMap<>();
//        currUnitLabelStr = "hartrees";
//        //boolean isDefaultUnit = true;
//        //Initial setup of chart
//        NumberAxis xAxis = new NumberAxis();
//        xAxis.setLabel("Geometry");
//        NumberAxis yAxis = new NumberAxis();
//        yAxis.setLabel("Energy " + "(" + currUnitLabelStr + ")");
//
//        LineChart geomVsEnergyChart = new LineChart(xAxis, yAxis);
//        geomVsEnergyChart.setTitle("Energy vs. Geometry");
//        geomVsEnergyChart.setLegendVisible(false);
//
//        XYChart.Series series = new XYChart.Series();
//        series.setName("Dummy Vals");
//
//        graphDataMap.put(1, 60);
//        graphDataMap.put(2, 40);
//        graphDataMap.put(3, 25);
//        graphDataMap.put(4, 20);
//
//        for (Integer key : graphDataMap.keySet()) {
//            XYChart.Data<Number, Number> data1 = new XYChart.Data<Number, Number>(key, graphDataMap.get(key));
//            series.getData().add(data1);
//
//        }
//
//        //End chart setup
//
//        GridPane geomGrid = new GridPane();
//        geomGrid.setPadding(new Insets(10, 10, 10, 10));
//
//        ListView<String> list = new ListView<String>();
//        ObservableList<String> items = FXCollections.observableArrayList (
//                "1. XXX", "2. XXX", "3. XXX", "4. XXX", "5. ...");
//        list.setItems(items);
//        list.setMaxHeight(300);
//
//        Button autosizeBtn = new Button("Autosize");
//
//        autosizeBtn.setPrefWidth(120);
//        autosizeBtn.setOnMouseClicked((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                int xPow = getExponent(maxXVal);
//                int yPow = getExponent(maxYVal);
//
//                xAxis.setUpperBound(maxXVal + Math.pow(10, xPow));
//                yAxis.setUpperBound(maxYVal + Math.pow(10, yPow));
//
//            }
//        }));
//
//        String[] unit_types = {"hartrees", "kcals/mol", "kJ/mol", "cm-1"};
//
//        Label unitsSelectLabel = new Label("Select Units");
//
//        ComboBox unitsSelectCombBox = new ComboBox(FXCollections
//                .observableArrayList(unit_types));
//
//        HashMap<Integer, Integer> convertedUnitsMap = new HashMap<>();
//        EventHandler<ActionEvent> unitSelectedEvent =
//                new EventHandler<ActionEvent>() {
//                    public void handle(ActionEvent e)
//                    {
//                        int maxXUnit = 0;
//                        int maxYUnit = 0;
//                        yAxis.setLabel("Energy (" + unitsSelectCombBox.getValue() + ")");
//
//                        if (unitsSelectCombBox.getValue().equals("hartrees")) {
//                            //Do nothing
//                            for (Integer key : graphDataMap.keySet()) {
//                                convertedUnitsMap.put(key, graphDataMap.get(key));
//
//                                if (key > maxXUnit) {
//                                    maxXUnit = key;
//                                }
////                                    if (convertedUnitsMap.get(key) > )
//                            }
//                        }
//
//                        if (unitsSelectCombBox.getValue().equals("kcal/mol")) {
//                            for (Integer key : graphDataMap.keySet()) {
//                                convertedUnitsMap.put(key, graphDataMap.get(key) * 628);
//
////                                    if (key > maxXUnit) {
////                                        maxXUnit = key;
////                                    }
////                                    if (convertedUnitsMap.get(key) > )
//                            }
//                        }
//
//                        else if (unitsSelectCombBox.getValue().equals("kJ/mol")) {
//                            for (Integer key : graphDataMap.keySet()) {
//                                convertedUnitsMap.put(key, graphDataMap.get(key) * 2626);
//                            }
//                        }
//
//                        else if (unitsSelectCombBox.getValue().equals("cm-1"))  {
//                            for (Integer key : graphDataMap.keySet()) {
//                                convertedUnitsMap.put(key, graphDataMap.get(key) * 219475);
//                            }
//                        }
//
//                        series.getData().clear();
//                        for (Integer key : convertedUnitsMap.keySet()) {
//                            series.getData().add(new XYChart.Data<Number, Number>(key, convertedUnitsMap.get(key)));
//                        }
//
//                    }
//                };
//
//        unitsSelectCombBox.setOnAction(unitSelectedEvent);
//
//        unitsSelectCombBox.getSelectionModel().selectFirst();
//        Button leftArrowBtn = new Button();
//        leftArrowBtn.setStyle("-fx-shape: \"M 0 -3.5 v 7 l 4 -3.5 z\"");
//        leftArrowBtn.setRotate(180);
//
//        Button circularPlayButton = new Button();
//        circularPlayButton.setStyle("-fx-border-radius: 20;");
//        circularPlayButton.setPrefWidth(20);
//
//        Button rightArrowBtn = new Button();
//        rightArrowBtn.setStyle("-fx-shape: \"M 0 -3.5 v 7 l 4 -3.5 z\"");
//
//        ToggleButton chartInfoButton = new ToggleButton();
//
//        chartInfoButton.setText("");
//        chartInfoButton.setGraphic(new ImageView(chartUsageInfo));
//
//        chartInfoButton.setOnMouseClicked((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                Alert alert = new Alert(Alert.AlertType.ERROR);
//                alert.setTitle("Info!");
//                alert.setHeaderText("Number Entry Error");
//                alert.setContentText("Info");
//                alert.showAndWait();
//            }
//        }));
//        HBox navBtnsHBox = new HBox(10);
//
//        maxYVal = 60;
//        maxXVal = 7;
//
//        upperXBound = maxXVal + 1;
//        upperYBound = maxYVal + 10;
//        xAxis.setAutoRanging(false);
//        xAxis.setLowerBound(0);
//        xAxis.setUpperBound(maxXVal + 1);
//        xAxis.setTickUnit(1);
//
//        geomVsEnergyChart.getData().add(series); //TODO:
//
////        Tooltip.install(data1.getNode(), new Tooltip("(" + data1.getXValue() + ", " + data1.getYValue() + ")"));
////        Tooltip.install(data2.getNode(), new Tooltip("(" + data2.getXValue() + ", " + data2.getYValue() + ")"));
////        Tooltip.install(data3.getNode(), new Tooltip("(" + data3.getXValue() + ", " + data3.getYValue() + ")"));
////        Tooltip.install(data4.getNode(), new Tooltip("(" + data4.getXValue() + ", " + data4.getYValue() + ")"));
//
//        Tooltip.install(xAxis, new Tooltip("Drag right to double scale, drag left to half scale"));
//        Tooltip.install(yAxis, new Tooltip("Drag up to double scale, drag down to half scale"));
//
//        xAxis.setOnMousePressed((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                xPressed = true;
//                lastXPosition = event.getSceneX();
//            }
//        }));
//
//        xAxis.setOnMouseDragged((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                if (xPressed) {
//                    if (event.getSceneX() - lastXPosition >= 20.0) {
//                        xAxis.setAutoRanging(false);
//                        double tempMaxXVal = upperXBound * 2;
//                        xAxis.setUpperBound(tempMaxXVal);
//                        upperXBound *= 2;
//
//                        xPressed = false;
//                        System.out.println("Finished right drag logic");
//
//                    } else if (lastXPosition - event.getSceneX() >= 20.0) {
//                        xAxis.setAutoRanging(false);
//                        //xAxis.setTickUnit();
//                        xAxis.setUpperBound(Math.ceil(upperXBound / 2));
//                        upperXBound /= 2;
//                        xPressed = false;
//
//                    }
//                }
//            }
//        }));
//
//        yAxis.setOnMousePressed((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                lastYPosition = event.getSceneY();
//                yPressed = true;
//            }
//        }));
//
//        yAxis.setOnMouseDragged((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                //If the user drags right
//                if (yPressed) {
//                    if (lastYPosition - event.getSceneY() >= 20.0) {
//                        yAxis.setAutoRanging(false);
//                        yAxis.setUpperBound(upperYBound * 2);
//                        upperYBound *= 2;
//                        yPressed = false;
//                        // System.out.println("Finished up drag logic");
//                    }
//                    else if (event.getSceneY() - lastYPosition >= 20.0) {
//                        yAxis.setAutoRanging(false);
//                        yAxis.setUpperBound(upperYBound / 2);
//                        upperYBound /= 2;
//                        yPressed = false;
//                    }
//                }
//            }
//        }));
//
//        VBox axesEditVBox = new VBox(8);
//        HBox xHBox = new HBox(10);
//        HBox yHBox = new HBox(10);
//        HBox scaleBtnsHBox = new HBox(10);
//
//        Label xLabel = new Label("Set Custom X Bound");
//        TextField xAxeInput = new TextField();
//        xAxeInput.setPromptText("Current X axis Tick unit: " + xAxis.getTickUnit());
//        xHBox.getChildren().addAll(xLabel, xAxeInput);
//
//        Label yLabel = new Label("Set Custom Y Bound");
//        TextField yAxeInput = new TextField();
//        yAxeInput.setPromptText("Current Y axis Tick unit: " + yAxis.getTickUnit());
//        yHBox.getChildren().addAll(yLabel, yAxeInput);
//
//        Button scaleBtn = new Button("Scale");
//        scaleBtn.setOnMouseClicked((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                xAxis.setAutoRanging(false);
//                yAxis.setAutoRanging(false);
//
//                try {
//
//                    Double parsedXInput = Double.parseDouble(xAxeInput.getText());
//                    Double parsedYInput = Double.parseDouble(xAxeInput.getText());
//
////                    if (parsedXInput < 0.0|| parsedYInput < 0.0) {
////                        showErrorDialog("Please make sure the scale values are greater than zero!");
////                    }
//
//                    /*
//                    Tasks 7/6/20
//
//                    - Offer to save at the out file initially
//                    - Format csv into columns, not rows
//                    - Give titles for each column
//                    - For prev and next buttons, you are plotting the "step number" on the x axis and total energy
//                    on the y axis
//                     */
//
//                    xAxis.setUpperBound(Double.parseDouble(xAxeInput.getText()));
//                    yAxis.setUpperBound(Double.parseDouble(yAxeInput.getText()));
//
//                }
//                catch (NumberFormatException e) {
//                    showErrorDialog("Please make sure the scale values are valid numbers!");
//                }
//
//
//            }
//        }));
//
//
//        Button saveAsPNGBtn = new Button("Save as PNG");
//        saveAsPNGBtn.setOnMouseClicked((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//
//                WritableImage chartPNG = geomVsEnergyChart.snapshot(new SnapshotParameters(), null);
//                String home = System.getProperty("user.home");
//
//                // System.out.println("Home: " + home);
//                TextInputDialog pngDialog = new TextInputDialog("chart_snapshot");
//                pngDialog.setTitle("Name Your File");
//                pngDialog.setHeaderText("File Name");
//                pngDialog.setContentText("Please name your file (no extension) or use the default: ");
//
//                Optional<String> result = pngDialog.showAndWait();
//
//                File file = new File(home + "/Documents/" + result.get() + ".png");
//
//                try {
//                    ImageIO.write(SwingFXUtils.fromFXImage(chartPNG, null), "png", file);
//                }
//                catch (Exception e) {
//                    System.out.println("PNG Exception");
//                }
//
//
//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setHeaderText("File Saved!");
//                alert.setContentText("Your file has been saved! It is located in the Documents folder at: " + file.getAbsolutePath());
//                alert.showAndWait();
//
//            }
//        }));
//
//        Button saveAsCSVButton = new Button("Save as CSV File");
//
//        List<String[]> dataLines = new ArrayList<>();
//
////        dataLines.add(new String[] {"1", "2", "3", "4"});
////        dataLines.add(new String[] {"60", "40", "25", "20"});
//
//        dataLines.add(new String[] {"Geometry Data"});
//        dataLines.add(new String[] {"X", "Y"});
//        dataLines.add(new String[] {"1", "60"});
//        dataLines.add(new String[] {"2", "40"});
//        dataLines.add(new String[] {"3", "25"});
//        dataLines.add(new String[] {"4", "20"});
//
//        saveAsCSVButton.setOnMouseClicked((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                try {
//
//                    TextInputDialog csvDialog = new TextInputDialog("geom_data");
//                    csvDialog.setTitle("Name Your File");
//                    csvDialog.setHeaderText("File Name");
//                    csvDialog.setContentText("Please name your file (no extension) or use the default: ");
//
//                    Optional<String> result1 = csvDialog.showAndWait();
//
//                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//                    alert.setTitle("Output Directory");
//                    alert.setHeaderText("Choose a directory");
//                    alert.setContentText("Would you like to ");
//
//                    ButtonType buttonTypeOne = new ButtonType("Choose Directory");
//                    ButtonType buttonTypeTwo = new ButtonType("Use Documents as Default");
//                    ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
//
//                    alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo,  buttonTypeCancel);
//
//                    Optional<ButtonType> result = alert.showAndWait();
//                    File selectedDirectory;
//                    String csvPath = "Empty";
//                    if (result.get() == buttonTypeOne){
//                        // ... user chose "One"
//                        DirectoryChooser directoryChooser = new DirectoryChooser();
//                        String h1 = System.getProperty("user.home");
//                        directoryChooser.setInitialDirectory(new File(h1));
//                        selectedDirectory = directoryChooser.showDialog(stage);
//                        csvPath = selectedDirectory.getAbsolutePath();
//
//                    } else if (result.get() == buttonTypeTwo) {
//                        // ... user chose "Two"
//                        String home = System.getProperty("user.home");
//                        csvPath = home + "/Documents/";
//
//                    }  else {
//                        // ... user chose CANCEL or closed the dialog
//                    }
//
//                    givenDataArray_whenConvertToCSV_thenOutputCreated(dataLines, csvPath, result1.get());
//                }
//                catch (Exception e) {
//                    System.out.println("CSV filechooser fail Exception");
//                    e.printStackTrace();
//                }
//            }
//        }));
//
//        scaleBtnsHBox.getChildren().addAll(scaleBtn, autosizeBtn);
//        HBox unitsHBox = new HBox(10);
//        unitsHBox.getChildren().addAll(unitsSelectLabel, unitsSelectCombBox);
//        axesEditVBox.getChildren().addAll(xHBox, yHBox, scaleBtnsHBox, unitsHBox);
//
//        navBtnsHBox.getChildren().addAll(axesEditVBox, leftArrowBtn, circularPlayButton, rightArrowBtn, chartInfoButton, saveAsPNGBtn, saveAsCSVButton);
//
//        GridPane.setConstraints(list, 0, 0);
//        GridPane.setConstraints(geomVsEnergyChart, 1, 0);
//        GridPane.setConstraints(navBtnsHBox, 1, 2);
//        //GridPane.setConstraints(axesEditVBox, 2, 4);
//
//        geomGrid.getChildren().addAll(list, geomVsEnergyChart, navBtnsHBox);
//        gridScene = new Scene(geomGrid, 1000, 1000);
//        stage.setScene(gridScene);
//        stage.show();
//
//    }
//    public void givenDataArray_whenConvertToCSV_thenOutputCreated(List<String[]> dataLines, String csvPath, String fileName) throws IOException {
//
//
//        File csvOutputFile = new File(csvPath + "/" + fileName + ".csv");
//        try (PrintWriter pw = new PrintWriter(csvOutputFile)) { dataLines.stream().map(this::convertToCSV).forEach(pw::println);
//        }
//        assertTrue(csvOutputFile.exists());
//    }
//
//
//
//    public String convertToCSV(String[] data) {
//        return Stream.of(data)
//                .map(this::escapeSpecialCharacters)
//                .collect(Collectors.joining(","));
//    }
//
//    public String escapeSpecialCharacters(String data) {
//        String escapedData = data.replaceAll("\\R", " ");
//        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
//            data = data.replace("\"", "\"\"");
//            escapedData = "\"" + data + "\"";
//        }
//        return escapedData;
//    }
//
//    public int getExponent(double bound) {
//        int pow = 0;
//        int boundInt = (int) bound;
//
//        boundInt /= 10;
//        while (boundInt != 0) {
//            pow++;
//            boundInt /= 10;
//        }
//        return pow;
//    }
//
//    public void showErrorDialog(String message) {
//        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.setTitle("Error!");
//        alert.setHeaderText("Number Entry Error");
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//
//    @FXML
//    public void showEnergyAnalysis() {
//
//        Stage stage = new Stage();
//        stage.setTitle("Energy Analysis");
//
//        CategoryAxis xAxis = new CategoryAxis();
//        xAxis.setLabel("Total");
//        NumberAxis yAxis = new NumberAxis();
//        yAxis.setLabel("Energy (kcal/mol)");
//        yAxis.setTickUnit(100);
//
//        BarChart chart = new BarChart(xAxis, yAxis);
//
//        XYChart.Series dataSeries1 = new XYChart.Series();
//
//        dataSeries1.setName("Dummy Vals");
//
//        double electrostatVal = -30.0;
//        double exchRepulsVal = 40.0;
//        double polarVal = -12.5;
//        double dispersVal = -20.0;
//        double totalVal = electrostatVal + exchRepulsVal + polarVal + dispersVal;
//
//        VBox vBox = new VBox(chart);
//        Scene scene = new Scene(vBox, 800, 800);
//
//        scene.getStylesheets().add("bar_styles.css");
//
//
//        dataSeries1.getData().add(new XYChart.Data("Electrostatic", electrostatVal));
//        dataSeries1.getData().add(new XYChart.Data("Exchange-Repulsion", exchRepulsVal));
//        dataSeries1.getData().add(new XYChart.Data("Polarization", polarVal));
//        dataSeries1.getData().add(new XYChart.Data("Dispersion", dispersVal));
//        dataSeries1.getData().add(new XYChart.Data("Total", totalVal));
//
//        chart.getData().add(dataSeries1);
//
//        // System.out.println("Chart Width: " + chart.getWidth());        //chart.setMaxWidth(50);
//
//        chart.setMaxWidth(500);
//        stage.setScene(scene);
//        stage.setHeight(450);
//        stage.setWidth(500);
//        stage.show();
//    }
}
