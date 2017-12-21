package com.example.mateusz.citytourapp.scheduler;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Luki on 2017-12-20.
 */

public class AppTaskJob extends AsyncTask<Void, Void, String>{

    @Override
    protected String doInBackground(Void... voids) {
        return "Job wykonany...";
    }
}
