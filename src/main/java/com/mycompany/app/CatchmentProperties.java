package com.mycompany.app;

import java.util.*;
import java.util.stream.Collectors;

public class CatchmentProperties {

    // Making this private to prevent instantiation.
    private CatchmentProperties() {}

    public static PropertyAssessments getPolygonProperties(PropertyAssessments properties, DrawPolygon polygon) {
        List<PropertyAssessment> residentialAssessments = properties.getResidentialProperties().getPropertyAssessments().stream()
                .filter(p -> (polygon.inPolygon(p.getLocation().getLongitude(), p.getLocation().getLatitude())))
                .collect(Collectors.toList());
        return new PropertyAssessments(residentialAssessments);
    }
}

