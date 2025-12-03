package com.mycompany.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PropertyAssessments {
    List<PropertyAssessment> propertyAssessments;

    public PropertyAssessments(String filename) throws IOException { // give this constructor the filename and read data
        List<PropertyAssessment> propertyAssessments = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(filename))) {
            reader.readLine();

            // read the file line by line and store all rows as PropertyAssessment objects
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");

                Address address = new Address(values[1], values[2], values[3]);
                boolean garage = false;
                if (values[4].equals("Y")) {
                    garage = true;
                }

                int neighborhoodID = -1;
                if (!values[5].isEmpty()) neighborhoodID = Integer.parseInt(values[5]);
                Neighborhood neighborhood = new Neighborhood(neighborhoodID, values[6], values[7]);

                Location location = new Location(Double.parseDouble(values[9]), Double.parseDouble(values[10]));
                List<AssessmentClass> assessmentClasses = new ArrayList<>();

                // add assessment classes as necessary to assessmentClasses
                if (!values[12].isEmpty()) {
                    AssessmentClass assessmentClass = new AssessmentClass(values[15], Integer.parseInt(values[12]));
                    assessmentClasses.add(assessmentClass);
                }
                if (!values[13].isEmpty()) {
                    AssessmentClass assessmentClass = new AssessmentClass(values[16], Integer.parseInt(values[13]));
                    assessmentClasses.add(assessmentClass);
                }
                if (!values[14].isEmpty()) {
                    AssessmentClass assessmentClass = new AssessmentClass(values[17], Integer.parseInt(values[14]));
                    assessmentClasses.add(assessmentClass);
                }

                PropertyAssessment property = new PropertyAssessment(Integer.parseInt(values[0]), address, garage,
                        neighborhood, Integer.parseInt(values[8]), location, assessmentClasses);

                propertyAssessments.add(property);
            }

            this.propertyAssessments = propertyAssessments;
        }
    }

    public PropertyAssessments(List<PropertyAssessment> propertyAssessments) { // use for making new object when filtering
        this.propertyAssessments = propertyAssessments;
    }

    public PropertyAssessment getWithAccountNum(int accountNumber) {
        for (PropertyAssessment propertyAssessment : propertyAssessments) {
            if (propertyAssessment.getAccountNumber() == accountNumber) {
                return propertyAssessment;
            }
        }
        return null;
    }

    public PropertyAssessments getByCitySubsection(String type, String subsectionName) {
        List<PropertyAssessment> subsectionProperties = new ArrayList<>();
        if (type.equals("neighborhood")) {
            for (PropertyAssessment propertyAssessment : propertyAssessments) {
                if (propertyAssessment.getNeighborhood().getNeighborhoodName().equals(subsectionName.toUpperCase())) {
                    subsectionProperties.add(propertyAssessment);
                }
            }
        }
        else if (type.equals("ward")) {
            for (PropertyAssessment propertyAssessment : propertyAssessments) {
                if (propertyAssessment.getNeighborhood().getWard().equals(subsectionName)) {
                    subsectionProperties.add(propertyAssessment);
                }
            }
        }
        return new PropertyAssessments(subsectionProperties);
    }

    public PropertyAssessments getByAssessmentClass(String assessmentClassMatch) {
        List<PropertyAssessment> assessmentClassProperties = new ArrayList<>();
        for (PropertyAssessment propertyAssessment : propertyAssessments) {
            for (AssessmentClass assessmentClass : propertyAssessment.getAssessmentClasses()) {
                if (assessmentClass.getClassName().equals(assessmentClassMatch.toUpperCase())) {
                    assessmentClassProperties.add(propertyAssessment);
                    break; // had !assessmentClassProperties.contains(propertyAssessment) but it hangs
                }
            }
        }
        return new PropertyAssessments(assessmentClassProperties);
    }

    public int getSize() {
        return propertyAssessments.size();
    }

    public int getMinAssessedValue() {
        if (propertyAssessments.isEmpty()) return -1;
        int min = propertyAssessments.get(0).getAssessedValue();
        for (PropertyAssessment propertyAssessment : propertyAssessments) {
            if (propertyAssessment.getAssessedValue() < min) {
                min = propertyAssessment.getAssessedValue();
            }
        }
        return min;
    }

    public int getMaxAssessedValue() {
        if (propertyAssessments.isEmpty()) return -1;
        int max = propertyAssessments.get(0).getAssessedValue();
        for (PropertyAssessment propertyAssessment : propertyAssessments) {
            if (propertyAssessment.getAssessedValue() > max) {
                max = propertyAssessment.getAssessedValue();
            }
        }
        return max;
    }

    public int getRangeAssessedValue() {
        if (propertyAssessments.isEmpty()) return -1;
        return this.getMaxAssessedValue() - this.getMinAssessedValue();
    }

    public int getMeanAssessedValue() {
        if (propertyAssessments.isEmpty()) return -1;
        long sum = 0;
        for (PropertyAssessment propertyAssessment : propertyAssessments) {
            sum += propertyAssessment.getAssessedValue();
        }
        return (int) (sum / propertyAssessments.size());
    }

    public int getMedianAssessedValue() {
        if (propertyAssessments.isEmpty()) return -1;
        propertyAssessments.sort(null);
        if (propertyAssessments.size() % 2 == 0) {
            return ((propertyAssessments.get(propertyAssessments.size() / 2).getAssessedValue() +
                                propertyAssessments.get(propertyAssessments.size() / 2 - 1).getAssessedValue()) / 2);
        }
        return propertyAssessments.get(propertyAssessments.size() / 2).getAssessedValue();
    }

    public List<String> getWardNames() {
        List<String> wardNames = new ArrayList<>();
        for (PropertyAssessment propertyAssessment : propertyAssessments) {
            if (!wardNames.contains(propertyAssessment.getNeighborhood().getWard())) {
                wardNames.add(propertyAssessment.getNeighborhood().getWard());
            }
        }
        return wardNames;
    }

    public List<String> getAssessmentClassNames() {
        List<String> assessmentClassNames = new ArrayList<>();
        for (PropertyAssessment propertyAssessment : propertyAssessments) {
            for (AssessmentClass assessmentClass : propertyAssessment.getAssessmentClasses()) {
                if (!assessmentClassNames.contains(assessmentClass.getClassName())) {
                    assessmentClassNames.add(assessmentClass.getClassName());
                }
            }
        }
        return assessmentClassNames;
    }

    public PropertyAssessments getResidentialProperties() {
        List<PropertyAssessment> residentialProperties = new ArrayList<>();
        for (PropertyAssessment propertyAssessment : propertyAssessments) {
            if (propertyAssessment.getAssessmentClasses().size() == 1 &&
                    propertyAssessment.getAssessmentClasses().get(0).getClassName().equals("RESIDENTIAL") &&
                    propertyAssessment.getAddress().getSuite().isEmpty() &&
                    propertyAssessment.isGarage() &&
                    propertyAssessment.getAssessedValue() > 0) {
                    residentialProperties.add(propertyAssessment);
            }
        }
        return new PropertyAssessments(residentialProperties);
    }

    public List <PropertyAssessment> getPropertyAssessments() {
        return propertyAssessments;
    }
}
