package com.example.mateusz.citytourapp.tweeter;

/**
 * Created by Mateusz on 10.02.2018.
 */

public class DataStoreClass {
    private static TwitterHelper globalTwitterHelper = new TwitterHelper();

    public static TwitterHelper getGlobalTwitterHelper() {
        return globalTwitterHelper;
    }

    private static void setGlobalTwitterHelper(TwitterHelper newGlobalVariable) {
        globalTwitterHelper = newGlobalVariable;
    }
}
