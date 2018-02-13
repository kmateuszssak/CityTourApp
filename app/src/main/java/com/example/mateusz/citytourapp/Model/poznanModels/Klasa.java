package com.example.mateusz.citytourapp.Model.poznanModels;

import com.example.mateusz.citytourapp.Model.AtrybutyKlasy;

import java.util.List;

/**
* Created by Mateusz on 30.12.2017.
*/
public class Klasa {

    private String opisKlasy;
    private List<AtrybutyKlasy> atrybutyKlasy = null;
    private String idKlasy;

    public String getOpisKlasy() {
        return opisKlasy;
    }

    public void setOpisKlasy(String opisKlasy) {
        this.opisKlasy = opisKlasy;
    }

    public List<AtrybutyKlasy> getAtrybutyKlasy() {
        return atrybutyKlasy;
    }

    public void setAtrybutyKlasy(List<AtrybutyKlasy> atrybutyKlasy) {
        this.atrybutyKlasy = atrybutyKlasy;
    }

    public String getIdKlasy() {
        return idKlasy;
    }

    public void setIdKlasy(String idKlasy) {
        this.idKlasy = idKlasy;
    }
}
