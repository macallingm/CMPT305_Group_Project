package com.mycompany.app;

import java.util.Objects;

public class Location {
    private final double latitude;
    private final double longitude;

    public Location() {
        this(0,0);
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location other = (Location) o;
        return this.latitude == other.latitude && this.longitude == other.longitude;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "Location{latitude=" + latitude + ", longitude=" + longitude + '}';
    }
}
