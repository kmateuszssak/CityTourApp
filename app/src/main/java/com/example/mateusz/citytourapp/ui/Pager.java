package com.example.mateusz.citytourapp.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Mateusz on 11.02.2018.
 */

/**
 * Klasa, która tylko zwraca tab'y.
 */
public class Pager extends FragmentStatePagerAdapter {

    //integer to count number of tabs
    int tabCount;

    TabMap tabMap;
    TabDetal tabDetal;

    public Pager(FragmentManager fm, int tabCount) {
        super(fm);
        //Initializing tab count
        this.tabCount = tabCount;

    }

    //Overriding method getItem
    @Override
    public Fragment getItem(int position) {
        //Returning the current tabs
        switch (position) {
            case 0:
                if (tabMap == null)
                    tabMap = new TabMap();

                return tabMap;
            case 1:
                if (tabDetal == null)
                    tabDetal = new TabDetal();

                return tabDetal;
            default:
                return null;
        }
    }

    //Overriden method getCount to get the number of tabs
    @Override
    public int getCount() {
        return tabCount;
    }

    public void refreshDataOnTabDetal() {
        if (tabDetal != null)
            tabDetal.setSelectedFeatureOnPage();
    }
}