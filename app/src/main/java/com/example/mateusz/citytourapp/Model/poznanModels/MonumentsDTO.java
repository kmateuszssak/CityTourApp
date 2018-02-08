package com.example.mateusz.citytourapp.Model.poznanModels;

import java.util.List;

public class MonumentsDTO {

    private List<Feature> features = null;
    private Crs crs;
    private Klasa klasa;
    private String type;

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public Crs getCrs() {
        return crs;
    }

    public void setCrs(Crs crs) {
        this.crs = crs;
    }

    public Klasa getKlasa() {
        return klasa;
    }

    public void setKlasa(Klasa klasa) {
        this.klasa = klasa;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}