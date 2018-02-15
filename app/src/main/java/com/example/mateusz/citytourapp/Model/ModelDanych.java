package com.example.mateusz.citytourapp.Model;

/**
 * Created by Luki on 2018-02-14.
 */

public class ModelDanych {

    private double promien;
    private long notyfikacja;
    private boolean wyswietlaj_zabytki;
    private boolean wyswietlaj_zabytkowe_koscioly;

    public double getPromien() {
        return promien;
    }

    public void setPromien(double promien) {
        this.promien = promien;
    }

    public boolean isWyswietlaj_zabytki() {
        return wyswietlaj_zabytki;
    }

    public void setWyswietlaj_zabytki(boolean wyswietlaj_zabytki) {
        this.wyswietlaj_zabytki = wyswietlaj_zabytki;
    }

    public boolean isWyswietlaj_zabytkowe_koscioly() {
        return wyswietlaj_zabytkowe_koscioly;
    }

    public void setWyswietlaj_zabytkowe_koscioly(boolean wyswietlaj_zabytkowe_koscioly) {
        this.wyswietlaj_zabytkowe_koscioly = wyswietlaj_zabytkowe_koscioly;
    }

    public long getNotyfikacja() {
        return notyfikacja;
    }

    public void setNotyfikacja(long notyfikacja) {
        this.notyfikacja = notyfikacja;
    }
}
