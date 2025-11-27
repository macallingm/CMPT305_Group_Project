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
        propertyAssessments.getPropertyAssessments().stream()
                .filter(p -> polygon.inPolygon(p.getLocation().getLongitude(), p.getLocation().getLatitude()))
                .forEach(p -> neighborhoods.add(p.getNeighborhood()));
        System.out.println(neighborhoods);
    }
}

