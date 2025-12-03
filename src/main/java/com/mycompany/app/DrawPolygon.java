package com.mycompany.app;

import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import javafx.scene.paint.Color;
import java.util.List;
//import com.esri.arcgisruntime.geometry.GeometryEngine;


public class DrawPolygon{

    GraphicsOverlay graphicsOverlay;
    PointCollection coordinateCollection;
    Polygon aPolygon;
    SimpleFillSymbol polygonFill;
    Graphic polygonGraphic;
    List<Point> pointList;
    String gradeLevel;
//    double xMinBounds = Double.POSITIVE_INFINITY;
//    double yMinBounds = Double.POSITIVE_INFINITY;
//    double xMaxBounds = Double.NEGATIVE_INFINITY;
//    double yMaxBounds = Double.NEGATIVE_INFINITY;


    public DrawPolygon(GraphicsOverlay graphicsOverlay, List<Point> pointList, String gradeLevel) {
        this.pointList = pointList;
        this.graphicsOverlay = graphicsOverlay;
        this.gradeLevel = gradeLevel;
        coordinateCollection = new PointCollection(SpatialReferences.getWgs84());

        for (Point point : pointList) {
            coordinateCollection.add(point);
//            xMinBounds = Double.min(xMinBounds, point.getX());
//            yMinBounds = Double.min(yMinBounds, point.getY());
//            xMaxBounds = Double.max(xMaxBounds, point.getX());
//            yMaxBounds = Double.max(yMaxBounds, point.getY());
        }

        aPolygon = new Polygon(coordinateCollection);
        float borderWidth = 3.0F;

        switch (gradeLevel) {
            case "EL":
                polygonFill = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.web("0xFF8000", 0.4),
                        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.web("A35200", 0.4), borderWidth));
                break;
            case "EJ":
                polygonFill = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.web("0x00FF80", 0.4),
                        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.web("00A352", 0.4), borderWidth));
                break;
            case "JR":
                polygonFill = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.web("0x0000FF", 0.4),
                        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.web("0000A3", 0.4), borderWidth));
                break;
            case "JS":
                polygonFill = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.web("0xFF0000", 0.4),
                        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.web("A30000", 0.4), borderWidth));
                break;
            case "SR":
                polygonFill = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.web("0xFF007F", 0.4),
                        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.web("A30052", 0.4), borderWidth));
                break;
            case "EJS":
                polygonFill = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.web("8a00c2", 0.4),
                        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.web("58007C", 0.4), borderWidth));
                break;
            default:
                polygonFill = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.web("FFEA00", 0.4),
                        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.web("CCBB00", 0.4), borderWidth));
                break;
        }
        //See https://developers.arcgis.com/java/maps-2d/tutorials/add-a-point-line-and-polygon/

        polygonGraphic = new Graphic(aPolygon, polygonFill);
        graphicsOverlay.getGraphics().add(polygonGraphic);

    }
//
//    public boolean inBoundingBox(Point point){
//        return (((point.getX()) >= xMinBounds && (point.getX()) <= xMaxBounds) &&  (point.getY() >= yMinBounds && point.getY() <= yMaxBounds));
//
//    }

    public boolean inPolygon(double xp, double yp){
//        Point aPoint = new Point(xp,yp);
//        if (!inBoundingBox(aPoint)){
//            return false;
//        }
//        int count = 0;
//        for (int i = 0; i < (coordinateCollection.size() - 1); i++){
//            if(((yp < coordinateCollection.get(i).getY()) != (yp < coordinateCollection.get(i+1).getY()))
//            && (xp < (coordinateCollection.get(i).getX() + (((yp - coordinateCollection.get(i).getY())/(coordinateCollection.get(i+1).getY() - coordinateCollection.get(i).getY())) * (coordinateCollection.get(i+1).getX()- coordinateCollection.get(i).getX()))))){
//                count++;
//            }
//        }
//        return (count%2 == 1);

        Point aPoint = new Point(xp,yp, SpatialReferences.getWgs84());
        return GeometryEngine.contains(aPolygon, aPoint);
    }

    public void removeGraphic(){
        graphicsOverlay.getGraphics().remove(polygonGraphic);
    }
    
    public String getGradeLevel(){
        return gradeLevel;
    }
}
