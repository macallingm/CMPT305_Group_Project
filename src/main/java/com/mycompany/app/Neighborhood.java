package com.mycompany.app;

import java.util.Objects;

public class Neighborhood {
    private final int neighborhoodID;
    private final String neighborhoodName;
    private final String ward;

    public Neighborhood() {
        this(0, "", "");
    }

    public Neighborhood(int neighborhoodID, String neighborhoodName, String ward) {
        this.neighborhoodID = neighborhoodID;
        this.neighborhoodName = neighborhoodName;
        this.ward = ward;
    }

    public int getNeighborhoodID() {
        return neighborhoodID;
    }

    public String getNeighborhoodName() {
        return neighborhoodName;
    }

    public String getWard() {
        return ward;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neighborhood other = (Neighborhood) o;
        return this.neighborhoodID == other.neighborhoodID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(neighborhoodID);
    }

    @Override
    public String toString() {
        return "Neighborhood{neighborhoodID=" + neighborhoodID +
                ", neighborhoodName=" + neighborhoodName +
                ", ward=" + ward + '}';
    }
}
