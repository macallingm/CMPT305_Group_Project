package com.mycompany.app;

import java.util.List;
import java.util.Objects;

public class PublicSchool {
    private final int schoolID;
    private final String schoolName;
    private final String schoolType;
    private final String grades;
    private final String address;
    private final Location location;
    private final List<Location> catchmentArea;

    public PublicSchool(int schoolID, String schoolName,  String schoolType, String grades, String address,
                        Location location, List<Location> catchmentArea) {
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

    public Location getLocation() {
        return location;
    }

    public List<Location> getCatchmentArea() {
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
