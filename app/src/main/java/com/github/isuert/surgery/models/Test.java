package com.github.isuert.surgery.models;

import org.parceler.Parcel;

import java.util.Date;
import java.util.List;

@Parcel(Parcel.Serialization.BEAN)
public class Test {
    private int id;
    private String type;
    private Date datetime;
    private List<TestResult> results;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public List<TestResult> getResults() {
        return results;
    }

    public void setResults(List<TestResult> results) {
        this.results = results;
    }
}
