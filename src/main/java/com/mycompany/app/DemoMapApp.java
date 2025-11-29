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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DemoMapApp extends Application {

    private MapView mapView;
    private TextField searchField;
    private Button schoolTypeBtn;
    private VBox leftPanel;
    private GraphicsOverlay polyGraphic = new GraphicsOverlay();

    private GraphicsOverlay schoolPointsGraphic = new GraphicsOverlay();

    private List<DrawPolygon> polygons = new ArrayList<>();
    private Map<Graphic, String> graphicToSchoolTypeMap = new HashMap<>();
    private Map<Graphic, PublicSchool> graphicToSchoolMap = new HashMap<>();
    private Map<DrawPolygon, PublicSchool> polygonToSchoolMap = new HashMap<>();
    private List<String> activeFilters = new ArrayList<>();
    private Popup schoolInfoPopup;
    private Popup catchmentStatsPopup;
    private PublicSchools  publicSchools = null;
    private PropertyAssessments propertyAssessments = null;
    private String filename= "Property_Assessment_Data_2025.csv";


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

        // draw points for all schools
        try {
            propertyAssessments = new PropertyAssessments(filename);
        } catch (IOException e) {
            System.err.println("Error: can't open file " + filename);
            return;
        }

        // Draw points for all schools
        for (PublicSchool school : publicSchools.getAllSchools()) {
            Point location = school.getLocation();
            if (location != null) {
                Graphic schoolGraphic = createPoints(location.getY(), location.getX(), schoolPointsGraphic);
                schoolGraphic.setVisible(false);
                graphicToSchoolTypeMap.put(schoolGraphic, school.getSchoolType());
                graphicToSchoolMap.put(schoolGraphic, school);
            }
        }

        setupSchoolPointHoverHandler();

        mapView.setOnMouseClicked(event -> {
            // Convert screen point to map point
            javafx.geometry.Point2D screenPoint = new javafx.geometry.Point2D(event.getX(), event.getY());
            Point mapPoint = mapView.screenToLocation(screenPoint);

            // Project from Web Mercator (map's SR) to WGS84 (degrees)
            Point wgsPoint = (Point) com.esri.arcgisruntime.geometry.GeometryEngine.project(
                    mapPoint, SpatialReferences.getWgs84());

            double latitude = wgsPoint.getY();
            double longitude = wgsPoint.getX();
            
            // check all polygons and collect all overlapping ones
            List<DrawPolygon> overlappingPolygons = new ArrayList<>();
            for (DrawPolygon poly : polygons) {
                if (poly.inPolygon(longitude, latitude)) {
                    overlappingPolygons.add(poly);
                }
            }
            
            if (!overlappingPolygons.isEmpty()) {
                // show popup with information for all overlapping polygons
                showOverlappingPolygonsPopup(overlappingPolygons, event.getScreenX(), event.getScreenY());
            }
        });
    }

    // Setup hover handler for school points
    private void setupSchoolPointHoverHandler() {
        mapView.setOnMouseMoved(event -> {
            if (!schoolPointsGraphic.isVisible()) {
                if (schoolInfoPopup != null) {
                    schoolInfoPopup.hide();
                }
                return;
            }

            PublicSchool hoveredSchool = null;

            // Convert screen point to map point
            javafx.geometry.Point2D screenPoint = new javafx.geometry.Point2D(event.getX(), event.getY());
            Point mapPoint = mapView.screenToLocation(screenPoint);

            // Project from Web Mercator (map's SR) to WGS84 (degrees)
            Point wgsPoint = (Point) com.esri.arcgisruntime.geometry.GeometryEngine.project(
                    mapPoint, SpatialReferences.getWgs84());

            double mouseLongitude = wgsPoint.getX();
            double mouseLatitude = wgsPoint.getY();

            // check if mouse is over a red dot
            for (Map.Entry<Graphic, PublicSchool> entry : graphicToSchoolMap.entrySet()) {
                Graphic graphic = entry.getKey();
                if (graphic.isVisible()) {
                    Point graphicPoint = (Point) graphic.getGeometry();

                    // Euclidean distance using pythagorean theorem check from red dot to mouse cursor
                    double distance = Math.sqrt(
                        Math.pow(mouseLongitude - graphicPoint.getX(), 2) +
                        Math.pow(mouseLatitude - graphicPoint.getY(), 2)
                    );


                    if (distance < 0.001) {
                        hoveredSchool = entry.getValue();
                        break;
                    }
                }
            }

            if (hoveredSchool != null) {
                showSchoolInfoPopup(hoveredSchool, event.getScreenX(), event.getScreenY());
            } else {
                if (schoolInfoPopup != null) {
                    schoolInfoPopup.hide();
                }
            }
        });
    }

    private Graphic createPoints(double lat,  double lon, GraphicsOverlay graphicsOverlay) {

        // create a red circle simple marker symbol
        SimpleMarkerSymbol redCircleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10);

        // create graphics and add to graphics overlay
        Graphic graphic = new Graphic(new Point(lon,lat, SpatialReferences.getWgs84()), redCircleSymbol);
        graphicsOverlay.getGraphics().add(graphic);
        return graphic;
    }
    
    // show popup with info for overlapping polys
    private void showOverlappingPolygonsPopup(List<DrawPolygon> overlappingPolygons, double screenX, double screenY) {
        if (catchmentStatsPopup != null) {
            catchmentStatsPopup.hide();
        }
        
        catchmentStatsPopup = new Popup();
        catchmentStatsPopup.setAutoHide(true);
        
        VBox popupContent = new VBox(10);
        popupContent.setPadding(new Insets(20, 25, 20, 25));
        popupContent.setStyle("" +
                "-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #cccccc; " +
                "-fx-border-radius: 10; " +
                "-fx-border-width: 2; ");
        popupContent.setPrefWidth(500);
        popupContent.setMaxWidth(500);
        popupContent.setMaxHeight(600);
        
        // title
        Label titleLabel = new Label("Overlapping Catchment Zones");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#1a5490"));
        popupContent.getChildren().add(titleLabel);

        
        Separator separator1 = new Separator();
        popupContent.getChildren().add(separator1);
        
        // to scroll for multiple polygons
        ScrollPane scrollPane = new ScrollPane();
        VBox polygonsList = new VBox(15);
        polygonsList.setPadding(new Insets(5));

        Set<PublicSchool> shownSchoolsOnPopUp = new HashSet<>();
        
        for (DrawPolygon polygon : overlappingPolygons) {
            PublicSchool school = polygonToSchoolMap.get(polygon);
            if (school != null && !shownSchoolsOnPopUp.contains(school)) {
                shownSchoolsOnPopUp.add(school);
                VBox polygonInfoBox = createPolygonInfoBox(school, polygon);
                polygonsList.getChildren().add(polygonInfoBox);
            }
        }
        
        scrollPane.setContent(polygonsList);
        scrollPane.setPrefHeight(450);
        scrollPane.setMaxHeight(450);
        scrollPane.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #e0e0e0;");
        popupContent.getChildren().add(scrollPane);
        
        catchmentStatsPopup.getContent().add(popupContent);
        catchmentStatsPopup.show(mapView.getScene().getWindow(), screenX - 250, screenY - 300);
    }
    
    // create box for one polygon
    private VBox createPolygonInfoBox(PublicSchool school, DrawPolygon polygon) {
        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(12));
        infoBox.setStyle("" +
                "-fx-background-color: white; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 8; " +
                "-fx-border-width: 1;");
        
        // school name
        Label schoolNameLabel = new Label(school.getSchoolName());
        schoolNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        schoolNameLabel.setTextFill(Color.web("#1a5490"));
        schoolNameLabel.setWrapText(true);
        infoBox.getChildren().add(schoolNameLabel);
        
        Separator separator = new Separator();
        infoBox.getChildren().add(separator);
        
        // school type
        if (school.getSchoolType() != null && !school.getSchoolType().isEmpty()) {
            HBox typeBox = new HBox(5);
            Label typeLabel = new Label("Type:");
            typeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            typeLabel.setTextFill(Color.web("#666666"));
            Label typeValue = new Label(school.getSchoolType());
            typeValue.setFont(Font.font("Arial", 12));
            typeValue.setTextFill(Color.web("#333333"));
            typeBox.getChildren().addAll(typeLabel, typeValue);
            infoBox.getChildren().add(typeBox);
        }
        
        // grades
        if (school.getGrades() != null && !school.getGrades().isEmpty()) {
            HBox gradeBox = new HBox(5);
            Label gradeLabel = new Label("Grades:");
            gradeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gradeLabel.setTextFill(Color.web("#666666"));
            Label gradeValue = new Label(school.getGrades());
            gradeValue.setFont(Font.font("Arial", 12));
            gradeValue.setTextFill(Color.web("#333333"));
            gradeBox.getChildren().addAll(gradeLabel, gradeValue);
            infoBox.getChildren().add(gradeBox);
        }
        
        // catchment properties
        if (propertyAssessments != null) {
            CatchmentProperties props = new CatchmentProperties(propertyAssessments, polygon);
            props.getCatchmentProperties();
            
            Separator propsSeparator = new Separator();
            infoBox.getChildren().add(propsSeparator);
            
            Label propsLabel = new Label("Catchment Statistics:");
            propsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            propsLabel.setTextFill(Color.web("#666666"));
            infoBox.getChildren().add(propsLabel);

            // fill with actual stats, just placeholder for now
            Label statsLabel = new Label("(Statistics available)");
            statsLabel.setFont(Font.font("Arial", 11));
            statsLabel.setTextFill(Color.web("#999999"));
            infoBox.getChildren().add(statsLabel);
        }
        
        return infoBox;
    }

    // show popup with school information on hover
    private void showSchoolInfoPopup(PublicSchool school, double screenX, double screenY) {
        if (schoolInfoPopup != null) {
            schoolInfoPopup.hide();
        }
        schoolInfoPopup = new Popup();
        schoolInfoPopup.setAutoHide(true);

        VBox popupContent = new VBox(10);
        popupContent.setPadding(new Insets(15, 20, 15, 20));
        popupContent.setStyle("" +
                "-fx-background-color: white; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #cccccc; " +
                "-fx-border-radius: 8; " +
                "-fx-border-width: 1; ");
        popupContent.setPrefWidth(280);
        popupContent.setMaxWidth(280);

        // school name
        Label schoolNameLabel = new Label(school.getSchoolName());
        schoolNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        schoolNameLabel.setTextFill(Color.web("#1a5490"));
        schoolNameLabel.setWrapText(true);
        popupContent.getChildren().add(schoolNameLabel);

        // separator
        Separator separator1 = new Separator();
        popupContent.getChildren().add(separator1);

        // school type
        if (school.getSchoolType() != null && !school.getSchoolType().isEmpty()) {
            HBox typeBox = new HBox(5);
            Label typeLabel = new Label("Type:");
            typeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            typeLabel.setTextFill(Color.web("#666666"));
            Label typeValue = new Label(school.getSchoolType());
            typeValue.setFont(Font.font("Arial", 12));
            typeValue.setTextFill(Color.web("#333333"));
            typeBox.getChildren().addAll(typeLabel, typeValue);
            popupContent.getChildren().add(typeBox);
        }

        // grade range
        if (school.getGrades() != null && !school.getGrades().isEmpty()) {
            HBox gradeBox = new HBox(5);
            Label gradeLabel = new Label("Grades:");
            gradeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gradeLabel.setTextFill(Color.web("#666666"));
            Label gradeValue = new Label(school.getGrades());
            gradeValue.setFont(Font.font("Arial", 12));
            gradeValue.setTextFill(Color.web("#333333"));
            gradeBox.getChildren().addAll(gradeLabel, gradeValue);
            popupContent.getChildren().add(gradeBox);
        }

        // address
        if (school.getAddress() != null && !school.getAddress().isEmpty()) {
            popupContent.getChildren().add(new Separator());
            VBox addressBox = new VBox(3);
            Label addressLabel = new Label("Address:");
            addressLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            addressLabel.setTextFill(Color.web("#666666"));
            Label addressValue = new Label(school.getAddress());
            addressValue.setFont(Font.font("Arial", 12));
            addressValue.setTextFill(Color.web("#333333"));
            addressValue.setWrapText(true);
            addressBox.getChildren().addAll(addressLabel, addressValue);
            popupContent.getChildren().add(addressBox);
        }

        schoolInfoPopup.getContent().add(popupContent);
        schoolInfoPopup.show(mapView.getScene().getWindow(), screenX - 140, screenY - 150);
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

        leftPanelVBox.getChildren().addAll(searchContainer, schoolTypeBtn);

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
                // check if filter is already applied to account for dupes
                if (!activeFilters.contains(menuItemText)) {
                    addFilterTag(menuItemText, filterButton);
                    applyFilterToBackend(publicSchools, menuItemText, polyGraphic, polygons);
                    activeFilters.add(menuItemText);
                }
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
    private void addFilterTag(String tagText, Button filterButton) {
        // pane
        Pane filterTagPane = new Pane();
        filterTagPane.setPrefWidth(250);
        filterTagPane.setPrefHeight(40);
        String color = "000000";

        switch(tagText) {
            case "Elementary":
                color = "#A35200";
                break;
            case "Junior High":
                color = "#0000A3";
                break;
            case "High School":
                color = "#A30052";
                break;
            case "Elementary + Junior High":
                color = "#00A352";
                break;
            case "Junior High + High School":
                color = "#A30000";
                break;
            case "All Grades":
                color = "#58007C";
                break;
            case "Specialized Programming":
                color = "#CCBB00";
                break;
            default:
                break;
        }

        filterTagPane.setStyle("" +
                "-fx-background-color: " + color + "; " +
                "-fx-background-radius: 20;");
        

        filterTagPane.setUserData(tagText);
        
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
            removeFilterFromBackend(publicSchools, tagText, polyGraphic, polygons);
            activeFilters.remove(tagText);
            
            // remove alll filter tags
            VBox parentVBox = (VBox) filterButton.getParent();
            if (parentVBox != null) {
                List<javafx.scene.Node> nodesToRemove = new ArrayList<>();
                for (javafx.scene.Node node : parentVBox.getChildren()) {
                    if (node instanceof Pane) {
                        Object userData = ((Pane) node).getUserData();
                        if (userData != null && userData.equals(tagText)) {
                            nodesToRemove.add(node);
                        }
                    }
                }
                parentVBox.getChildren().removeAll(nodesToRemove);
            }
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
            // check if polygons for this filter type already exist to prevent duplicates
            boolean filterAlreadyApplied = false;
            for (DrawPolygon polygon : polygons) {
                if (polygon.getGradeLevel().equals(abbrevType)) {
                    filterAlreadyApplied = true;
                    break;
                }
            }
            
            // only add polygons if thefuilter type hasn't been applied yet
            if (!filterAlreadyApplied) {
                for (PublicSchool school : publicSchools.getBySchoolType(abbrevType)) {
                    if (school.getCatchmentArea() != null && !school.getCatchmentArea().isEmpty()) {
                        DrawPolygon polygon = new DrawPolygon(polyGraphic, school.getCatchmentArea(), abbrevType);
                        polygons.add(polygon);
                        polygonToSchoolMap.put(polygon, school);
                    }
                }
            }

            
            // show school points overlay when filter is applied
            schoolPointsGraphic.setVisible(true);
            

            // show school pts for filter type
            for (Map.Entry<Graphic, String> entry : graphicToSchoolTypeMap.entrySet()) {
                if (entry.getValue().equals(abbrevType)) {
                    entry.getKey().setVisible(true);
                }
            }
        }
    }
    
    // remove filter
    private void removeFilterFromBackend(PublicSchools publicSchools, String schoolType, GraphicsOverlay polyGraphic, List<DrawPolygon> polygons) {
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
            // remove for the filter type
            List<DrawPolygon> polygonsToRemove = new ArrayList<>();
            for (DrawPolygon polygon : polygons) {
                if (polygon.getGradeLevel().equals(abbrevType)) {
                    polygon.removeGraphic();
                    polygonsToRemove.add(polygon);
                    polygonToSchoolMap.remove(polygon);
                }
            }
            polygons.removeAll(polygonsToRemove);

            
            // hide school pts for this filter type


            // hide school pts

            for (Map.Entry<Graphic, String> entry : graphicToSchoolTypeMap.entrySet()) {
                if (entry.getValue().equals(abbrevType)) {
                    entry.getKey().setVisible(false);
                }
            }
            
            // hide school points overlay if there are no filters
            if (polygons.isEmpty()) {
                schoolPointsGraphic.setVisible(false);
            }
        }
    }

}



