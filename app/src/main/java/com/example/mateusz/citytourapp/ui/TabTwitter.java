package com.example.mateusz.citytourapp.ui;

/**
 * Created by Mateusz on 11.02.2018.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mateusz.citytourapp.MapsActivity;
import com.example.mateusz.citytourapp.R;

/**
 * Created by Mateusz on 11.02.2018.
 */

public class TabTwitter extends Fragment {

    MapsActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if ( getActivity() instanceof MapsActivity){
            activity = (MapsActivity) getActivity();
        }

        return inflater.inflate(R.layout.tab_twitter, container, false);
    }
}
