package com.example.mateusz.citytourapp.ImagesSet;

/**
 * Created by Mateusz on 14.02.2018.
 */

public class Photo {

    public String photoName;
    public String photoPoster;
    public float photoRating;

    public Photo(){

    }
    public Photo(String photoName,String photoPoster,float photoRating){
        this.photoName = photoName;
        this.photoPoster = photoPoster;
        this.photoRating  = photoRating;
    }

    public String getphotoName() {
        return photoName;
    }

    public void setphotoName(String photoName) {
        this.photoName = photoName;
    }

    public String getphotoPoster() {
        return photoPoster;
    }

    public void setphotoPoster(String photoPoster) {
        this.photoPoster = photoPoster;
    }

    public float getphotoRating() {
        return photoRating;
    }

    public void setphotoRating(float photoRating) {
        this.photoRating = photoRating;
    }
}
