/**
 * Copyright 2019 Esri
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.mycompany.app;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.CoordinateFormatter;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class DemoMapApp extends Application {

    private TableView<Location> table;
    private ObservableList<Location> locations;

    private TextField latitudeField;
    private TextField longitudeField;
    private MapView mapView;
    private static final GraphicsOverlay locationGraphics = new GraphicsOverlay();


    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Show Locations on Map App");

        Scene scene = new Scene(createContent(), 1000, 700);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Stops and releases all resources used in application.
     */
    @Override
    public void stop() {
        if (mapView != null) {
            mapView.dispose();
        }
    }

    // Create the application user interface
    private Parent createContent() {
        // Title
        final Label label = new Label("Map Coordinates");
        label.setFont(new Font("Arial", 16));

        // Create input text, table, and map
        HBox hBox = createTextInput();
        createTable();
        createMap();

        // Put table and map together in an HBox
        HBox mainView = new HBox(10);
        mainView.getChildren().addAll(table, mapView);
        VBox.setVgrow(mainView, Priority.ALWAYS);

        // Put the UI elements into a VBox: title, input text fields, and main view with table and map
        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().addAll(label, hBox, mainView);

        return vBox;
    }

    // Create input text fields and the Add button
    private HBox createTextInput() {
        latitudeField = new TextField();
        latitudeField.setPromptText("Latitude");
        longitudeField = new TextField();
        longitudeField.setPromptText("Longitude");

        Button addBtn = new Button("Show");
        addBtn.setOnAction(event -> {
            double lat = Double.parseDouble(latitudeField.getText());
            double lon = Double.parseDouble(longitudeField.getText());
            Location location = new Location(lat, lon);
            // Add location to the table and show it on the map
            locations.add(location);
            latitudeField.clear();
            longitudeField.clear();
            addPoint(lat, lon, locationGraphics, "#00FF00", "#FF0000");
        });

        // Arrange the text fields and button horizontally
        HBox hBox = new HBox(10);
        hBox.getChildren().addAll(latitudeField, longitudeField, addBtn);

        return hBox;
    }

    // Create a table and configure it
    private void createTable() {
        table = new TableView<>();
        locations = FXCollections.observableArrayList();
        table.setItems(locations);

        TableColumn<Location, Double> latCol = new TableColumn<>("Latitude");
        latCol.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        latCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? "" : Double.toString(value));
            }
        });
        latCol.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        table.getColumns().add(latCol);

        TableColumn<Location, Double> lonCol = new TableColumn<>("Longitude");
        lonCol.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        lonCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? "" : Double.toString(value));
            }
        });
        lonCol.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        table.getColumns().add(lonCol);

        table.setPrefWidth(300);

    }

    // Create a table and configure it
    private void createMap() {

        // Note: it is not best practice to store API keys in source code.
        // An API key is required to enable access to services, web maps, and web scenes hosted in ArcGIS Online.
        // If you haven't already, go to your developer dashboard to get your API key.
        // Please refer to https://developers.arcgis.com/java/get-started/ for more information
        // String yourApiKey = "YOUR_API_KEY";
        String yourApiKey = "AAPTxy8BH1VEsoebNVZXo8HurAu481bqaz10i09Tq5xdE2fStszJY58xg5MHNHdSainjK_t6FAbSF0O-tK7Jn9NvFdMYwtqpno2RB5lY7QXinjCRTc9b-r0wQDoxzw4vqTKUwtrEv800k6RoVX57pAUZAUMlwbSKxAvo06mUL-09yZHt6RsQ3aI6tKF0s6GxqlxzaXlBo_vlBMaOZrWorKzigQjk6ZLOelnZBtqjpR2zp_M.AT1_oLwo9GUT";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);

        // create a MapView to display the map and add it to the stack pane
        mapView = new MapView();

        // create an ArcGISMap with an imagery streets basemap
        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_IMAGERY);

        // display the map by setting the map on the map view
        mapView.setMap(map);
        mapView.setViewpoint(new Viewpoint(53.53, -113.48, 350000));
        mapView.getGraphicsOverlays().add(locationGraphics);
        HBox.setHgrow(mapView, Priority.ALWAYS);

        // 👇 Add this block to make map clicks add lat/lon in degrees
        mapView.setOnMouseClicked(event -> {
            // Convert screen point to map point
            javafx.geometry.Point2D screenPoint = new javafx.geometry.Point2D(event.getX(), event.getY());
            Point mapPoint = mapView.screenToLocation(screenPoint);

            // Project from Web Mercator (map's SR) to WGS84 (degrees)
            Point wgsPoint = (Point) com.esri.arcgisruntime.geometry.GeometryEngine.project(
                    mapPoint, SpatialReferences.getWgs84());

            double latitude = wgsPoint.getY();
            double longitude = wgsPoint.getX();

            // Add to table
            Location location = new Location(latitude, longitude);
            locations.add(location);

            // Add graphic to map
            addPoint(latitude, longitude, locationGraphics, "#0000FF", "#00FF00");

            // Format coordinates
            String coordinates = CoordinateFormatter.toLatitudeLongitude(mapPoint, CoordinateFormatter.LatitudeLongitudeFormat.DECIMAL_DEGREES, 4);

            // Get and show a callout
            Callout callout = mapView.getCallout();
            callout.setTitle("Clicked Location");
            callout.setDetail(coordinates);
            callout.showCalloutAt(mapPoint);
        });

    }


    private static void addPoint(double lat, double lon, GraphicsOverlay graphicsOverlay, String fill, String line) {
        // create a point geometry with a location and spatial reference
        Point point = new Point(lon, lat, SpatialReferences.getWgs84());
        // create an opaque orange point symbol with a opaque blue outline symbol
        SimpleMarkerSymbol simpleMarkerSymbol =
                new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.valueOf(fill), 10);
        SimpleLineSymbol blueOutlineSymbol =
                new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.valueOf(line), 2);

        simpleMarkerSymbol.setOutline(blueOutlineSymbol);

        // create a graphic with the point geometry and symbol
        Graphic pointGraphic = new Graphic(point, simpleMarkerSymbol);

        graphicsOverlay.getGraphics().add(pointGraphic);
    }
}
