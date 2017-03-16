package com.github.isuert.surgery.models;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class TestResult {
    private String name;
    private String unit;
    private float value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
