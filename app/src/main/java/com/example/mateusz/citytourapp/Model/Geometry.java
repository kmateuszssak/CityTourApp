package com.example.mateusz.citytourapp.Model;

import java.util.List;

/**
 * Created by Mateusz on 30.12.2017.
 */
public class Geometry {

    public List<Double> coordinates = null;
    public String type;

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
