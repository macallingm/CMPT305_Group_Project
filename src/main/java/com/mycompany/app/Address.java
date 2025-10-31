package com.mycompany.app;

import java.util.Objects;

public class Address {
    private final String suite;
    private final String houseNumber;
    private final String streetName;

    public Address() {
        this("", "", "");
    }

    public Address(String suite, String houseNumber, String streetName) {
        this.suite = suite;
        this.houseNumber = houseNumber;
        this.streetName = streetName;
    }

    public String getSuite() {
        return suite;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getStreetName() {
        return streetName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address other = (Address) o;
        return this.suite.equals(other.suite) &&
                this.houseNumber.equals(other.houseNumber) &&
                this.streetName.equals(other.streetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suite, houseNumber, streetName);
    }

    @Override
    public String toString() {
        if (suite.isEmpty() && houseNumber.isEmpty() && streetName.isEmpty()) {
            return "";
        }
        if(suite.isEmpty()) {
            return houseNumber + " " + streetName;
        }
        if (houseNumber.isEmpty()) {
            return suite + " " + streetName;
        }
        return suite + " " + houseNumber + " " + streetName;
    }
}
