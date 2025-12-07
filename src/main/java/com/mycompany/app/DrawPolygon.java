package com.mycompany.app;

import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import javafx.scene.paint.Color;
import java.util.List;


public class DrawPolygon{

    GraphicsOverlay graphicsOverlay;
    PointCollection coordinateCollection;
    Polygon aPolygon;
    SimpleFillSymbol polygonFill;
    Graphic polygonGraphic;
    List<Point> pointList;
    String gradeLevel;

    // Takes a point list from the Edmonton public school dataset and converts it into a polygon.
    public DrawPolygon(GraphicsOverlay graphicsOverlay, List<Point> pointList, String gradeLevel) {
        this.pointList = pointList;
        this.graphicsOverlay = graphicsOverlay;
        this.gradeLevel = gradeLevel;
        coordinateCollection = new PointCollection(SpatialReferences.getWgs84());

        for (Point point : pointList) {
            coordinateCollection.add(point);
        }

        aPolygon = new Polygon(coordinateCollection);
        float borderWidth = 3.0F;

        // Based on the school type the polygon will have a different color.
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

        polygonGraphic = new Graphic(aPolygon, polygonFill);
        graphicsOverlay.getGraphics().add(polygonGraphic);

    }

    // Logic that determines if a click or object is within a polygon or another object.
    public boolean inPolygon(double xp, double yp){
        Point aPoint = new Point(xp,yp, SpatialReferences.getWgs84());
        return GeometryEngine.contains(aPolygon, aPoint);
    }

    // Used with filtering to remove polygons when the filtering is turned off.
    public void removeGraphic(){
        graphicsOverlay.getGraphics().remove(polygonGraphic);
    }
    
    public String getGradeLevel(){
        return gradeLevel;
    }
}
