package com.carto.advancedmap.model;

/**
 * Created by aareundo on 22/09/2017.
 */

public class City {

    public String name;

    public BoundingBox bbox;

    public City(String name, BoundingBox bbox) {
        this.name = name;
        this.bbox = bbox;
    }
}
