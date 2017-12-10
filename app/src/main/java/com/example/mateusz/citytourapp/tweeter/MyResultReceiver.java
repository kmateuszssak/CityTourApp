package com.example.mateusz.citytourapp.tweeter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.twitter.sdk.android.tweetcomposer.TweetUploadService;

/**
 * Created by Luki on 2017-12-09.
 */

public class MyResultReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TweetUploadService.UPLOAD_SUCCESS.equals(intent.getAction())) {
            Log.d("MyResultReceiver", "Sukces");
            //final Long tweetId = intentExtras.getLong(TweetUploadService.EXTRA_TWEET_ID);
        } else if (TweetUploadService.UPLOAD_FAILURE.equals(intent.getAction())) {
            Log.d("MyResultReceiver", "Failure");
            //final Intent retryIntent = intentExtras.getParcelable(TweetUploadService.EXTRA_RETRY_INTENT);
        } else if (TweetUploadService.TWEET_COMPOSE_CANCEL.equals(intent.getAction())) {
            Log.d("MyResultReceiver", "Compose cancel");
            // cancel
        }
    }
}
