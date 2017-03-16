package com.github.isuert.surgery.models;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class Display {
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
