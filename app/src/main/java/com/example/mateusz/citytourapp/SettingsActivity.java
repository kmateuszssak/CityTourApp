package com.example.mateusz.citytourapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mateusz.citytourapp.Model.ModelDanych;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Luki on 2018-02-14.
 */

public class SettingsActivity extends AppCompatActivity {

    private FirebaseUser user;
    private DatabaseReference database;
    private ModelDanych dane = null;
    private FirebaseAuth aAuth;
    private FirebaseAuth.AuthStateListener aAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        aAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        aAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser fbUser = firebaseAuth.getCurrentUser();
                if(fbUser != null)
                {
                    Log.i("SettingsActivity", "User zalogowany do firebase");
                }
            }
        };

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dane = pobierzDane(dataSnapshot);
                wyswietlSettingsy(dane);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //error with database
                Toast.makeText(getApplicationContext(), "Error with database! Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    //Wyswietlenie opcji oraz ustawienie ich stanu wg dnayc hz Firebase
    private void wyswietlSettingsy(ModelDanych dane) {
        Button buttonZZ = (Button) findViewById(R.id.button2);
        CheckBox czyZabytkoweKoscioly = (CheckBox) findViewById(R.id.checkBox);
        CheckBox czyZabytki = (CheckBox) findViewById(R.id.checkBox2);
        TextView opcjeLabel = (TextView) findViewById(R.id.textView2);
        TextView opcjePromien = (TextView) findViewById(R.id.textView4);
        TextView ladowanieUstawien = (TextView) findViewById(R.id.textView5);
        EditText promien = (EditText) findViewById(R.id.editText2);
        TextView opcjeNotyfikacja = (TextView) findViewById(R.id.textView6);
        EditText notyfikacja = (EditText) findViewById(R.id.editText3);

        promien.setText(String.valueOf(dane.getPromien()));
        notyfikacja.setText(String.valueOf(dane.getNotyfikacja()));
        if(dane.isWyswietlaj_zabytkowe_koscioly())
        {
            czyZabytkoweKoscioly.setChecked(true);
        }
        if(dane.isWyswietlaj_zabytki())
        {
            czyZabytki.setChecked(true);
        }

        ladowanieUstawien.setVisibility(View.GONE);
        buttonZZ.setVisibility(View.VISIBLE);
        czyZabytkoweKoscioly.setVisibility(View.VISIBLE);
        czyZabytki.setVisibility(View.VISIBLE);
        opcjeLabel.setVisibility(View.VISIBLE);
        opcjePromien.setVisibility(View.VISIBLE);
        promien.setVisibility(View.VISIBLE);
        opcjeNotyfikacja.setVisibility(View.VISIBLE);
        notyfikacja.setVisibility(View.VISIBLE);
    }

    //Pobranie dnaych z firebase
    private ModelDanych pobierzDane(DataSnapshot dataSnapshot) {
        ModelDanych currentData = new ModelDanych();
        currentData.setPromien(dataSnapshot.getValue(ModelDanych.class).getPromien());
        currentData.setNotyfikacja(dataSnapshot.getValue(ModelDanych.class).getNotyfikacja());
        currentData.setWyswietlaj_zabytki(dataSnapshot.getValue(ModelDanych.class).isWyswietlaj_zabytki());
        currentData.setWyswietlaj_zabytkowe_koscioly(dataSnapshot.getValue(ModelDanych.class).isWyswietlaj_zabytkowe_koscioly());
        Toast.makeText(getApplicationContext(), "Promien: " + currentData.getPromien(), Toast.LENGTH_LONG).show();

        return currentData;
    }

    public void zapiszZmiany(View view)
    {
        CheckBox czyZabytkoweKoscioly = (CheckBox) findViewById(R.id.checkBox);
        CheckBox czyZabytki = (CheckBox) findViewById(R.id.checkBox2);
        EditText promien = (EditText) findViewById(R.id.editText2);
        EditText notyfikacja = (EditText) findViewById(R.id.editText2);

        double newPromien = 0;
        try{
            newPromien = Double.parseDouble(promien.getText().toString());
            if(dane.getPromien() != newPromien || newPromien != 0)
            {
                database.child("promien").setValue(newPromien);
            }
            Constans.PROMIEN = newPromien;
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Promien nie jest liczbą!", Toast.LENGTH_LONG).show();
        }

        long newNotyfikacja = 0;
        try{
            newNotyfikacja = Long.valueOf(notyfikacja.getText().toString()).longValue();
            if(dane.getNotyfikacja() != newNotyfikacja || newNotyfikacja != 0)
            {
                database.child("notyfikacja").setValue(newNotyfikacja);
            }
            Constans.CZAS_ODSWIEZANIA = newNotyfikacja;
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Czas notyfikacji nie jest liczbą!", Toast.LENGTH_LONG).show();
        }

        if(dane.isWyswietlaj_zabytki() != czyZabytki.isChecked())
        {
            database.child("wyswietlaj_zabytki").setValue(czyZabytki.isChecked());
        }
        if(dane.isWyswietlaj_zabytkowe_koscioly() != czyZabytkoweKoscioly.isChecked())
        {
            database.child("wyswietlaj_zabytkowe_koscioly").setValue(czyZabytkoweKoscioly.isChecked());
        }

        Constans.czyZabytki = czyZabytki.isChecked();
        Constans.czyZabytkoweKoscioly = czyZabytkoweKoscioly.isChecked();
    }

    public void powrotDoMapy(View view)
    {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.aAuth.addAuthStateListener(aAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(aAuthListener != null)
        {
            aAuth.removeAuthStateListener(aAuthListener);
        }
    }
}
