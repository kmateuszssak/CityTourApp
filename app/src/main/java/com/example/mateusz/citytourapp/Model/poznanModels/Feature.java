package com.example.mateusz.citytourapp.Model.poznanModels;

import com.example.mateusz.citytourapp.Model.Geometry;

/**
 * Created by Mateusz on 30.12.2017.
 */
public class Feature {

    private Geometry geometry;
    private Integer id;
    private String type;
    private Properties properties;

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
