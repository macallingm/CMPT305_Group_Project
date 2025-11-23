package com.mycompany.app;

import java.util.*;
import java.util.stream.Collectors;

public class CatchmentProperties {
    PropertyAssessments propertyAssessments;
    DrawPolygon polygon;
    Set<Neighborhood> neighborhoods =  new HashSet<Neighborhood>();



    public CatchmentProperties(PropertyAssessments properties, DrawPolygon polygon) {
        this.propertyAssessments = properties.getResidentialProperties();
        this.polygon = polygon;
    }

    public void getCatchmentProperties() {

        for (PropertyAssessment property : propertyAssessments.getPropertyAssessments()) {
            if (polygon.inPolygon(property.getLocation().getLongitude(), property.getLocation().getLatitude())){
                neighborhoods.add(property.getNeighborhood());
            }
        }
        System.out.println(neighborhoods);
    }
}

