package com.mycompany.app;

import java.util.Objects;

public class AssessmentClass {
    private final String assessmentClass;
    private final int percent;

    public AssessmentClass() {
        this("", 0);
    }

    public AssessmentClass(String assessmentClass, int percent) {
        this.assessmentClass = assessmentClass;
        this.percent = percent;
    }
    public String getClassName() {
        return assessmentClass;
    }

    public int getPercent() {
        return percent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssessmentClass other = (AssessmentClass) o;
        return this.assessmentClass.equals(other.assessmentClass) && this.percent == other.percent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assessmentClass, percent);
    }

    @Override
    public String toString() {
        return assessmentClass + " " + percent + '%';
    }
}
