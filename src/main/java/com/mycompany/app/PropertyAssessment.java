package com.mycompany.app;

import java.util.List;
import java.util.Objects;

public class PropertyAssessment implements Comparable<PropertyAssessment> {
    private final int accountNumber;
    private final Address address;
    private final boolean garage;
    private final Neighborhood neighborhood;
    private final int assessedValue;
    private final Location location;
    private final List<AssessmentClass> assessmentClasses;

    public PropertyAssessment(int accountNumber, Address address, boolean garage, Neighborhood neighborhood,
                              int assessedValue, Location location, List<AssessmentClass> assessmentClasses) {
        this.accountNumber = accountNumber;
        this.address = address;
        this.garage = garage;
        this.neighborhood = neighborhood;
        this.assessedValue = assessedValue;
        this.location = location;
        this.assessmentClasses = assessmentClasses;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public Address getAddress() {
        return address;
    }

    public boolean isGarage() {
        return garage;
    }

    public Neighborhood getNeighborhood() {
        return neighborhood;
    }

    public int getAssessedValue() {
        return assessedValue;
    }

    public Location getLocation() {
        return location;
    }

    public List<AssessmentClass> getAssessmentClasses() {
        return assessmentClasses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyAssessment other = (PropertyAssessment) o;
        return this.accountNumber == other.accountNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber, address, garage, neighborhood, location, assessmentClasses);
    }

    @Override
    public int compareTo(PropertyAssessment other) {
        if (other == null) {
            throw new NullPointerException();
        }
        return Integer.compare(assessedValue, other.assessedValue);
    }

    @Override
    public String toString() {
        return "PropertyAssessment{" +
                "accountNumber=" + accountNumber +
                ", address=" + address +
                ", garage=" + garage +
                ", neighborhood=" + neighborhood +
                ", assessedValue=" + assessedValue +
                ", location=" + location +
                ", assessmentClasses" + assessmentClasses +
                '}';
    }
}
