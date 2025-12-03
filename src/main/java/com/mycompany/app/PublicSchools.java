package com.mycompany.app;

import com.esri.arcgisruntime.portal.PortalUserContent;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.esri.arcgisruntime.geometry.Point;

// Method parseCSVLine copied from Philip Mees' post

public class PublicSchools {
    List<PublicSchool> publicSchools;

    public PublicSchools(String filename) throws IOException {
        List<PublicSchool> publicSchools = new ArrayList<>();

//        int countNewSchools = 0;

        try (BufferedReader reader = Files.newBufferedReader(Path.of(filename))) {
            reader.readLine();

            // read the file line by line and store all rows as PublicSchool objects
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = PublicSchools.parseCSVLine(line);

                Point location = new Point(Double.parseDouble(values[14]), Double.parseDouble(values[15]));

                List<List<Point>> catchmentAreas = new ArrayList<>();
                catchmentAreas.add(new ArrayList<>()); // initialize first inner list for points
                Point catchmentPoint = null;
                int catchmentAreaCounter = 0;

                if (!values[19].isEmpty()) {
                    // strip leading and trailing non-numeric characters and split on commas
                    String multipolygon = values[19].substring(16, values[19].length() - 3);
                    String[] polygonPoints = multipolygon.split(",");

                    for (String point : polygonPoints) {
                        String[] singlePointPair = point.stripLeading().split(" ");

                        // some last values will be "43757))" and some first values will be "((3489..."
                        for (String pointValue : singlePointPair) {
                            if (pointValue.contains(")")) {
                                singlePointPair[1] = pointValue.substring(0, pointValue.indexOf(")"));
                            }
                            else if (pointValue.contains("(")) {
                                singlePointPair[0] = pointValue.substring(2);
                                // first value of new catchment area increments number of catchment area inner lists
                                catchmentAreaCounter += 1;
                                catchmentAreas.add(new ArrayList<>());
                            }
                        }

                        catchmentPoint = new Point(Double.parseDouble(singlePointPair[0]),
                                Double.parseDouble(singlePointPair[1]));
                        catchmentAreas.get(catchmentAreaCounter).add(catchmentPoint);
                    }
                }

//                if (catchmentAreas.size() > 1) {
//                    countNewSchools += 1;
//                    System.out.println("Number of catchment areas: " + catchmentAreas.size());
//                    System.out.println(catchmentAreas);
//                }

                PublicSchool school = new PublicSchool(Integer.parseInt(values[4]), values[5], values[6], values[7],
                        values[8], location, catchmentAreas);

                publicSchools.add(school);
            }

            this.publicSchools = publicSchools;

//            System.out.println("Number of new schools: " + countNewSchools);
        }
    }

    public PublicSchools(List<PublicSchool> publicSchools) {
        this.publicSchools = publicSchools;
    }

    public static String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Toggle inQuotes, unless it's an escaped quote ("")
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"'); // Escaped quote
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of a token
                tokens.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        // Add last token
        tokens.add(current.toString());

        return tokens.toArray(new String[0]);
    }

    public PublicSchool getWithSchoolID(int schoolID) {
        for (PublicSchool school : publicSchools) {
            if (school.getSchoolID() == schoolID) {
                return school;
            }
        }
        return null;
    }

    // method to get list of schools matching school type
    public List<PublicSchool> getBySchoolType(String schoolType) {
        List<PublicSchool> typeSchools = new ArrayList<>();
        for (PublicSchool publicSchool : publicSchools) {
            if (publicSchool.getSchoolType().equalsIgnoreCase(schoolType)) {
                typeSchools.add(publicSchool);
            }
        }
        return typeSchools;
    }

    // method to get all schools
    public List<PublicSchool> getAllSchools() {
        return new ArrayList<>(publicSchools);
    }
}
