package com.example.mateusz.citytourapp.ui;

/**
 * Created by Mateusz on 11.02.2018.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mateusz.citytourapp.R;

/**
 * Created by Mateusz on 11.02.2018.
 */

public class TabDetal extends Fragment {

    //Overriden method onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        return inflater.inflate(R.layout.tab_detal, container, false);
    }
}
