package com.example.mateusz.citytourapp.tweeter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.twitter.sdk.android.tweetcomposer.TweetUploadService;

/**
 * Created by Luki on 2017-12-09.
 */

public class LoginResultReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context aContext, Intent aIntent) {
        if (TweetUploadService.UPLOAD_SUCCESS.equals(aIntent.getAction())) {
            Log.d("LoginResultReceiver", "Sukces");
            //final Long tweetId = intentExtras.getLong(TweetUploadService.EXTRA_TWEET_ID);
        } else if (TweetUploadService.UPLOAD_FAILURE.equals(aIntent.getAction())) {
            Log.d("LoginResultReceiver", "Failure");
            //final Intent retryIntent = intentExtras.getParcelable(TweetUploadService.EXTRA_RETRY_INTENT);
        } else if (TweetUploadService.TWEET_COMPOSE_CANCEL.equals(aIntent.getAction())) {
            Log.d("LoginResultReceiver", "Compose cancel");
            // cancel
        }
    }
}
