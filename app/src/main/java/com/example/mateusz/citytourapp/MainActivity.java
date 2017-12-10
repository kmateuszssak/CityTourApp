package com.example.mateusz.citytourapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mateusz.citytourapp.rest.RestClient;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetcomposer.ComposerActivity;

public class MainActivity extends AppCompatActivity {

    private TwitterLoginButton aLoginButton;
    private TwitterSession aSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(R.layout.activity_main);

        final Button aButton = (Button) findViewById(R.id.button);
        aButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                testRest();
            }
        });

        final Button aButton2 = (Button) findViewById(R.id.button2);
        aButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(aSession != null)
                {
                    tweet(aSession, "Sample Tweet");
                }
                else
                {
                   Toast.makeText(MainActivity.this, "First log in to tweeter!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        aLoginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        aLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                aSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
                //TwitterAuthToken authToken = session.getAuthToken();
                //String token = authToken.token;
                //String secret = authToken.secret;
                Log.d("TWITTER", "User successfuly authenticated.");
            }

            @Override
            public void failure(TwitterException aException) {
                Log.d("TWITTER", "There is a problem with authentitacion! Error message: " + aException.getMessage());
            }
        });
    }

    private void tweet(TwitterSession aSession, String sMessage) {
        final Intent aIntent = new ComposerActivity.Builder(this)
                .session(aSession)
                .text(sMessage)
                .hashtags("#twitter")
                .createIntent();
        startActivity(aIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        aLoginButton.onActivityResult(requestCode, resultCode, data);
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
