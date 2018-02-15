package com.example.mateusz.citytourapp.tweeter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.tweetcomposer.ComposerActivity;

/**
 * Created by Luki on 2017-12-29.
 */

public class TwitterHelper {

    private TwitterSession m_aSession;

    public void tweet(Context aContext, String sMessage, String hashTag) {
        if(this.m_aSession != null)
        {
            final Intent aIntent = new ComposerActivity.Builder(aContext)
                    .session(this.m_aSession)
                    .text(sMessage)
                    .hashtags("#" + hashTag)
                    .createIntent();
            aContext.startActivity(aIntent);
        }
        else{
            Log.d("TWITTER", "No tweeter session!");
            Toast.makeText(aContext, "First log in to tweeter!", Toast.LENGTH_SHORT).show();
        }
    }

    public TwitterSession getM_aSession() {
        return m_aSession;
    }

    public void setM_aSession(TwitterSession m_aSession) {
        this.m_aSession = m_aSession;
    }
}
