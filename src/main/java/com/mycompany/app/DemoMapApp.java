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
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Stage;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DemoMapApp extends Application {

    private MapView mapView;
    private TextField searchField;
    private Button schoolTypeBtn;
    private VBox leftPanel;
    private GraphicsOverlay polyGraphic = new GraphicsOverlay();

    private CheckBox showSchoolPointsToggle;
    private GraphicsOverlay schoolPointsGraphic = new GraphicsOverlay();

    private List<DrawPolygon> polygons = new ArrayList<>();
    private PublicSchools  publicSchools = null;


    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Edmonton School Catchment Zones");

        Scene scene = new Scene(createContent(), 1400, 900);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (mapView != null) {
            mapView.dispose();
        }
    }

    // create map
    private void createMap() {
        String yourApiKey = "AAPTxy8BH1VEsoebNVZXo8HurAu481bqaz10i09Tq5xdE2fStszJY58xg5MHNHdSainjK_t6FAbSF0O-tK7Jn9NvFdMYwtqpno2RB5lY7QXinjCRTc9b-r0wQDoxzw4vqTKUwtrEv800k6RoVX57pAUZAUMlwbSKxAvo06mUL-09yZHt6RsQ3aI6tKF0s6GxqlxzaXlBo_vlBMaOZrWorKzigQjk6ZLOelnZBtqjpR2zp_M.AT1_oLwo9GUT";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);

        mapView = new MapView();
        ArcGISMap arcgisMapInstance = new ArcGISMap(BasemapStyle.ARCGIS_STREETS);
        mapView.setMap(arcgisMapInstance);
        mapView.setViewpoint(new Viewpoint(53.53, -113.48, 350000));
        mapView.getGraphicsOverlays().add(polyGraphic);
        mapView.getGraphicsOverlays().add(schoolPointsGraphic);
        
        // hide pts
        schoolPointsGraphic.setVisible(false);

        try {
            publicSchools = new PublicSchools("Edmonton_Public_School_Board_2025_Small.csv");
        } catch (IOException e) {
            System.err.println("Error: can't open file");
            return;
        }

        // Draw points for all schools
        for (PublicSchool school : publicSchools.getAllSchools()) {
            Point location = school.getLocation();
            if (location != null) {
                createPoints(location.getY(), location.getX(), schoolPointsGraphic);
            }
        }

        mapView.setOnMouseClicked(event -> {
            // Convert screen point to map point
            javafx.geometry.Point2D screenPoint = new javafx.geometry.Point2D(event.getX(), event.getY());
            Point mapPoint = mapView.screenToLocation(screenPoint);

            // Project from Web Mercator (map's SR) to WGS84 (degrees)
            Point wgsPoint = (Point) com.esri.arcgisruntime.geometry.GeometryEngine.project(
                    mapPoint, SpatialReferences.getWgs84());

            double latitude = wgsPoint.getY();
            double longitude = wgsPoint.getX();
            
            // Check all polygons
            boolean isIn = false;
            for (DrawPolygon poly : polygons) {
                if (poly.inPolygon(longitude, latitude)) {
                    isIn = true;
                    break;
                }
            }
            
            if (isIn) {
                System.out.println("True");
            }
            else {
                System.out.println("False");
            }
        });
    }

    private static void createPoints(double lat,  double lon, GraphicsOverlay graphicsOverlay) {

        // create a red circle simple marker symbol
        SimpleMarkerSymbol redCircleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10);

        // create graphics and add to graphics overlay
        Graphic graphic = new Graphic(new Point(lon,lat, SpatialReferences.getWgs84()), redCircleSymbol);
        graphicsOverlay.getGraphics().add(graphic);
    }

    // ui
    private Parent createContent() {
        createMap();

        // main screen
        StackPane mainRootPane = new StackPane();
        mainRootPane.getChildren().add(mapView);

        // title at the very top of screen
        Label mapTitle = new Label("Edmonton School Catchment Zones");
        mapTitle.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 48));
        mapTitle.setTextFill(Color.web("#1a5490"));
        mapTitle.setMouseTransparent(true);
        StackPane.setAlignment(mapTitle, Pos.TOP_CENTER);
        StackPane.setMargin(mapTitle, new Insets(20, 0, 0, 0));
        mainRootPane.getChildren().add(mapTitle);

        // left panel
        leftPanel = createLeftPanel();
        StackPane.setAlignment(leftPanel, Pos.TOP_LEFT);
        StackPane.setMargin(leftPanel, new Insets(80, 0, 0, 20));
        mainRootPane.getChildren().add(leftPanel);

        return mainRootPane;
    }

    // create left panel
    private VBox createLeftPanel() {
        VBox leftPanelVBox = new VBox(10);
        leftPanelVBox.setPrefWidth(250);
        leftPanelVBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // for the search bar
        searchField = new TextField();
        searchField.setPromptText("Search");
        searchField.setPrefWidth(250);
        searchField.setPrefHeight(40);
        searchField.setPadding(new Insets(5, 5, 5, 30));
        searchField.setStyle("" +
                "-fx-background-color: white; " +
                "-fx-background-radius: 5; " +
                "-fx-border-color: #cccccc; " +
                "-fx-border-radius: 5;");

        // search icon inside search bar
        StackPane searchContainer = new StackPane();
        searchContainer.setPrefWidth(250);
        Label searchIcon = new Label("🔍");
        searchIcon.setFont(Font.font(16));
        searchIcon.setMouseTransparent(true);
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 10));
        searchContainer.getChildren().addAll(searchField, searchIcon);

        setupSearchField();

        // filter button for school type
        schoolTypeBtn = createFilterButton("School Type");

        setupSchoolTypeFilter(schoolTypeBtn);

        // toggle for showing/hiding school points
        showSchoolPointsToggle = new CheckBox("Show School Points");
        showSchoolPointsToggle.setPrefWidth(250);
        showSchoolPointsToggle.setPrefHeight(40);
        showSchoolPointsToggle.setPadding(new Insets(5, 5, 5, 5));
        showSchoolPointsToggle.setStyle("" +
                "-fx-background-color: white; " +
                "-fx-background-radius: 5; " +
                "-fx-border-color: #cccccc; " +
                "-fx-border-radius: 5;");
        showSchoolPointsToggle.setFont(Font.font("Arial", 14));
        setupSchoolPointsToggle();

        leftPanelVBox.getChildren().addAll(searchContainer, schoolTypeBtn, showSchoolPointsToggle);

        leftPanelVBox.setPadding(new Insets(10));

        return leftPanelVBox;
    }
    
    private void setupSearchField() {
        searchField.textProperty().addListener((textObservable, previousTextValue, currentTextValue) -> {
            if (!currentTextValue.isEmpty() && currentTextValue.length() >= 3) {
                showSearchFillSuggestions(currentTextValue);
            }
        });
        
        // handling search when user presses Enter on keyboard
        searchField.setOnAction(searchActionEvent -> {
            String searchQuery = searchField.getText();
            if (!searchQuery.isEmpty()) {
                performSearch(searchQuery);
            }
        });
    }

    // search suggestions
    private void showSearchFillSuggestions(String searchFillQuery) {
        // backend to fetch search suggestions (if possible)
    }

    // filter button
    private Button createFilterButton(String text) {
        Button filterButtonInstance = new Button();
        filterButtonInstance.setPrefWidth(250);
        filterButtonInstance.setPrefHeight(40);
        filterButtonInstance.setAlignment(Pos.CENTER_LEFT);
        filterButtonInstance.setStyle("" +
                "-fx-background-color: #1a5490; " +
                "-fx-background-radius: 20;");

        // content with plus icon
        HBox buttonContentHBox = new HBox(10);
        buttonContentHBox.setAlignment(Pos.CENTER_LEFT);
        
        // "+" icon container
        StackPane filterPlusContainer = new StackPane();
        filterPlusContainer.setPrefWidth(24);
        filterPlusContainer.setPrefHeight(24);
        filterPlusContainer.setStyle("" +
                "-fx-background-color: #7fb8d8; " +
                "-fx-background-radius: 6;");
        
        Label filterPlusIcon = new Label("+");
        filterPlusIcon.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        filterPlusIcon.setTextFill(Color.WHITE);
        filterPlusContainer.getChildren().add(filterPlusIcon);
        
        // label
        Label filterButtonLabel = new Label(text);
        filterButtonLabel.setTextFill(Color.WHITE);
        filterButtonLabel.setFont(Font.font("Arial", 14));
        
        buttonContentHBox.getChildren().addAll(filterPlusContainer, filterButtonLabel);
        buttonContentHBox.setPadding(new Insets(0, 0, 0, 8)); // Left indent
        
        filterButtonInstance.setGraphic(buttonContentHBox);
        filterButtonInstance.setContentDisplay(ContentDisplay.LEFT);

        return filterButtonInstance;
    }

    // school type filter
    private void setupSchoolTypeFilter(Button filterButtonParam) {
        filterButtonParam.setOnAction(schoolTypeActionEvent -> {
            Popup schoolTypePopup = createCustomMenu("School Type", new String[]{"Elementary", "Junior High",
                    "High School", "Elementary + Junior High", "Junior High + High School", "All Grades",
                    "Specialized Programming"}, schoolTypeBtn);
            schoolTypePopup.show(filterButtonParam, 
                filterButtonParam.localToScreen(0, 0).getX(),
                filterButtonParam.localToScreen(0, filterButtonParam.getHeight()).getY());
        });
    }

    // school points toggle
    private void setupSchoolPointsToggle() {
        showSchoolPointsToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            schoolPointsGraphic.setVisible(newValue);
        });
    }


    // custom menu dropdown for the filters
    private Popup createCustomMenu(String title, String[] items, Button filterButton) {
        Popup filterMenuPopup = new Popup();
        filterMenuPopup.setAutoHide(true);
        
        VBox menuContainerVBox = new VBox(0);
        menuContainerVBox.setStyle("" +
                "-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #cccccc; " +
                "-fx-border-radius: 10; " +
                "-fx-border-width: 1;");
        menuContainerVBox.setPrefWidth(250);
        menuContainerVBox.setPadding(new Insets(12, 0, 12, 0));
        
        // header
        Label menuHeaderLabel = new Label(title);
        menuHeaderLabel.setTextFill(Color.web("#1a5490"));
        menuHeaderLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        menuHeaderLabel.setPadding(new Insets(0, 0, 8, 16));
        menuContainerVBox.getChildren().add(menuHeaderLabel);
        
        // separator
        Separator menuSeparator = new Separator();
        menuSeparator.setPadding(new Insets(0, 12, 8, 12));
        menuContainerVBox.getChildren().add(menuSeparator);
        
        // for each menu item
        for (String menuItemText : items) {
            HBox menuItemHBox = new HBox(10);
            menuItemHBox.setAlignment(Pos.CENTER_LEFT);
            menuItemHBox.setPadding(new Insets(8, 16, 8, 16));
            menuItemHBox.setCursor(Cursor.HAND);
            
            // container
            StackPane menuPlusContainer = new StackPane();
            menuPlusContainer.setPrefWidth(20);
            menuPlusContainer.setPrefHeight(20);
            menuPlusContainer.setStyle("" +
                    "-fx-background-color: #7fb8d8; " +
                    "-fx-background-radius: 4;");
            
            Label menuPlusIcon = new Label("+");
            menuPlusIcon.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            menuPlusIcon.setTextFill(Color.WHITE);
            menuPlusContainer.getChildren().add(menuPlusIcon);
            
            // text
            Label menuItemLabel = new Label(menuItemText);
            menuItemLabel.setTextFill(Color.web("#7fb8d8"));
            menuItemLabel.setFont(Font.font("Arial", 14));
            
            menuItemHBox.getChildren().addAll(menuPlusContainer, menuItemLabel);
            
            // when a filter is clicked
            menuItemHBox.setOnMouseClicked(menuItemClickEvent -> {
                addFilterTag(menuItemText, "#FF8C00", filterButton);
                applyFilterToBackend(publicSchools, menuItemText, polyGraphic, polygons);
                // ADD THAT LINE BACK ONCE METHOD TO DRAW POLYGONS IS READY!!
                filterMenuPopup.hide();
            });
            
            // hover effect when mouse cursor is on a filter option
            menuItemHBox.setOnMouseEntered(hoverEnterEvent -> menuItemHBox.setStyle("-fx-background-color: #f0f0f0;"));
            menuItemHBox.setOnMouseExited(hoverExitEvent -> menuItemHBox.setStyle("-fx-background-color: transparent;"));
            
            menuContainerVBox.getChildren().add(menuItemHBox);
        }
        
        filterMenuPopup.getContent().add(menuContainerVBox);
        return filterMenuPopup;
    }
    
    // filter tag
    private void addFilterTag(String tagText, String color, Button filterButton) {
        // pane
        Pane filterTagPane = new Pane();
        filterTagPane.setPrefWidth(250);
        filterTagPane.setPrefHeight(40);
        filterTagPane.setStyle("" +
                "-fx-background-color: " + color + "; " +
                "-fx-background-radius: 20;");
        
        // text
        Label filterTagLabel = new Label(tagText);
        filterTagLabel.setTextFill(Color.WHITE);
        filterTagLabel.setFont(Font.font("Arial", 14));
        filterTagPane.getChildren().add(filterTagLabel);
        filterTagLabel.layoutXProperty().bind(filterTagPane.widthProperty().divide(2).subtract(filterTagLabel.widthProperty().divide(2)));
        filterTagLabel.layoutYProperty().bind(filterTagPane.heightProperty().divide(2).subtract(filterTagLabel.heightProperty().divide(2)));
        
        // close
        Label filterCloseButton = new Label("×");
        filterCloseButton.setTextFill(Color.WHITE);
        filterCloseButton.setFont(Font.font(16));
        filterCloseButton.setCursor(Cursor.HAND);
        filterCloseButton.setOnMouseClicked(closeClickEvent -> {
            removeFilterFromBackend(filterButton.getText(), tagText);
            ((VBox) filterTagPane.getParent()).getChildren().remove(filterTagPane);
        });
        filterTagPane.getChildren().add(filterCloseButton);
        filterCloseButton.layoutXProperty().bind(filterTagPane.widthProperty().subtract(24));
        filterCloseButton.layoutYProperty().bind(filterTagPane.heightProperty().divide(2).subtract(filterCloseButton.heightProperty().divide(2)));
        
        // insert
        VBox parentVBox = (VBox) filterButton.getParent();
        int filterButtonIndex = parentVBox.getChildren().indexOf(filterButton);
        parentVBox.getChildren().add(filterButtonIndex + 1, filterTagPane);
    }


    // backend fetching for user actions
    
    // searching
    private void performSearch(String searchQuery) {
    }
    
    // filter
    private void applyFilterToBackend(PublicSchools publicSchools, String schoolType, GraphicsOverlay polyGraphic, List<DrawPolygon> polygons) {
        String abbrevType = "";

        switch(schoolType) {
            case "Elementary":
                abbrevType = "EL";
                break;
            case "Junior High":
                abbrevType = "JR";
                break;
            case "High School":
                abbrevType = "SR";
                break;
            case "Elementary + Junior High":
                abbrevType = "EJ";
                break;
            case "Junior High + High School":
                abbrevType = "JS";
                break;
            case "All Grades":
                abbrevType = "EJS";
                break;
            case "Specialized Programming":
                abbrevType = "SP";
                break;
            default:
                break;
        }

        if (!abbrevType.isEmpty()) {
            for (PublicSchool school : publicSchools.getBySchoolType(abbrevType)) {
                if (school.getCatchmentArea() != null && !school.getCatchmentArea().isEmpty()) {
                    DrawPolygon polygon = new DrawPolygon(polyGraphic, school.getCatchmentArea(), abbrevType);
                    polygons.add(polygon);
                }
            }
        }
    }
    
    // remove filter
    private void removeFilterFromBackend(String filterType, String filterValue) {
    }

}



