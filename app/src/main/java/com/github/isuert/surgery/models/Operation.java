package com.github.isuert.surgery.models;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class Operation {
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
