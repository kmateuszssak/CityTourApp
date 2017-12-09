package com.example.mateusz.citytourapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                testRest();
            }
        });

        final Button mateuszButton = (Button) findViewById(R.id.mateuszButton);
        mateuszButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MateuszActivity.class));
            }
        });
    }

    private void testRest() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                RestClient.getJSON(null, "https://en.wikipedia.org/wiki/Minisite", null);
            }
        });
    }
}
