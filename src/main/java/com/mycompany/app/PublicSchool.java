package com.mycompany.app;

import java.util.List;
import java.util.Objects;
import com.esri.arcgisruntime.geometry.Point;

public class PublicSchool {
    private final int schoolID;
    private final String schoolName;
    private final String schoolType;
    private final String grades;
    private final String address;
    private final Point location;
    private final List<Point> catchmentArea;

    public PublicSchool(int schoolID, String schoolName,  String schoolType, String grades, String address,
                        Point location, List<Point> catchmentArea) {
        this.schoolID = schoolID;
        this.schoolName = schoolName;
        this.schoolType = schoolType;
        this.grades = grades;
        this.address = address;
        this.location = location;
        this.catchmentArea = catchmentArea;
    }

    public int getSchoolID() {
        return schoolID;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public String getSchoolType() {
        return schoolType;
    }

    public String getGrades() {
        return grades;
    }

    public String getAddress() {
        return address;
    }

    public Point getLocation() {
        return location;
    }

    public List<Point> getCatchmentArea() {
        return catchmentArea;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PublicSchool that = (PublicSchool) o;
        return schoolID == that.schoolID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(schoolID);
    }

    @Override
    public String toString() {
        return "PublicSchool{" +
                "schoolID=" + schoolID +
                ", schoolName=" + schoolName +
                ", schoolType=" + schoolType +
                ", grades=" + grades +
                ", address=" + address +
                ", location=" + location +
                ", catchmentArea=" + catchmentArea +
                '}';
    }
}
