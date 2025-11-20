package com.mycompany.app;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import javafx.scene.paint.Color;
import java.util.List;


public class DrawPolygon{

    GraphicsOverlay graphicsOverlay;
    PointCollection testCollection;
    Polygon testPolygon;
    SimpleFillSymbol polygonFill;
    Graphic polygonGraphic;
    List<Point> pointList;


    public DrawPolygon( GraphicsOverlay graphicsOverlay, List<Point> pointList, String gradeLevel) {
        this.pointList = pointList;
        this.graphicsOverlay = graphicsOverlay;
        testCollection = new PointCollection(SpatialReferences.getWgs84());
        for  (Point point : pointList) {
            testCollection.add(point);
        }

        testPolygon = new Polygon(testCollection);

        if(gradeLevel.equals("EL")){
            polygonFill =  new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.web("0xFF8000",0.4), null);
        } else if (gradeLevel.equals("EJ")) {
            polygonFill =  new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.web("0x00FF80",0.4), null);
        } else if (gradeLevel.equals("JR")) {
            polygonFill =  new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.web("0x0000FF",0.4), null);
        } else if (gradeLevel.equals("JS")) {
            polygonFill =  new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.web("0xFF0000",0.4), null);
        } else if (gradeLevel.equals("SR")) {
            polygonFill =  new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.web("0x800020",0.4), null);
        } else if (gradeLevel.equals("EJS")) {
            polygonFill =  new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.web("8a00c2",0.4), null);
        }
        else {
            polygonFill =  new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.web("0xFF007F",0.4), null);
        }
        //See https://developers.arcgis.com/java/maps-2d/tutorials/add-a-point-line-and-polygon/

        polygonGraphic = new Graphic(testPolygon, polygonFill);
        graphicsOverlay.getGraphics().add(polygonGraphic);

    }

    public boolean inPolygon(double xp, double yp){
        int count = 0;
        for (int i= 0 ; i < (testCollection.size() - 1); i++){
            if(((yp < testCollection.get(i).getY()) != (yp < testCollection.get(i+1).getY()))
            && (xp < (testCollection.get(i).getX() + (((yp - testCollection.get(i).getY())/(testCollection.get(i+1).getY() - testCollection.get(i).getY())) * (testCollection.get(i+1).getX()- testCollection.get(i).getX()))))){
                count++;
            }
        }
        return (count%2 == 1);
    }

    public void removeGraphic(){
        graphicsOverlay.getGraphics().remove(polygonGraphic);
    }
}
