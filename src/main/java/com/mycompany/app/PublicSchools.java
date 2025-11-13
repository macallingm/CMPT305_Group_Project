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

        try (BufferedReader reader = Files.newBufferedReader(Path.of(filename))) {
            reader.readLine();

            // read the file line by line and store all rows as PublicSchool objects
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = PublicSchools.parseCSVLine(line);

                Point location = new Point(Double.parseDouble(values[14]), Double.parseDouble(values[15]));

                List<Point> catchmentArea = new ArrayList<>();
                Point catchmentPoint = null;

                if (!values[19].isEmpty()) {
                    String multipolygon = values[19].substring(16, values[19].length() - 3);
                    String[] points = multipolygon.split(",");

                    for (String point : points) {
                        System.out.println(point);
                        String[] pointValues = point.stripLeading().split(" ");
                        System.out.println(pointValues.length);
                        catchmentPoint = new Point(Double.parseDouble(pointValues[0]),
                                Double.parseDouble(pointValues[1]));
                        catchmentArea.add(catchmentPoint);
                    }
                }

                PublicSchool school = new PublicSchool(Integer.parseInt(values[4]), values[5], values[6], values[7],
                        values[8], location, catchmentArea);

                System.out.print(school);
            }
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
    public PublicSchools getBySchoolType(String schoolType) {
        List<PublicSchool> typeSchools = new ArrayList<>();
        for (PublicSchool publicSchool : publicSchools) {
            if (publicSchool.getSchoolType().equalsIgnoreCase(schoolType)) {
                typeSchools.add(publicSchool);
            }
        }
        return new PublicSchools(typeSchools);
    }
}
