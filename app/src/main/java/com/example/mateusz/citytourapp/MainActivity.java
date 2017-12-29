package com.example.mateusz.citytourapp;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mateusz.citytourapp.scheduler.JobSchedulerService;

import com.example.mateusz.citytourapp.rest.RestClient;
import com.example.mateusz.citytourapp.tweeter.TwitterHelper;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class MainActivity extends AppCompatActivity {

    private TwitterLoginButton m_aLoginButton;
    private TwitterHelper m_aTwitterObject;

    private static final String JOB_TAG = "scheduled_job";
    private boolean m_bFlag = false;
    private JobScheduler m_aScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(R.layout.activity_main);

        m_aTwitterObject = new TwitterHelper();

        final Button aButton = (Button) findViewById(R.id.button);
        aButton.setOnClickListener(new View.OnClickListener() {
        //Inicjalizacja listenera button
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                testRest();
            }
        });

        final Button aButton2 = (Button) findViewById(R.id.button2);
        aButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                m_aTwitterObject.tweet(MainActivity.this, "");
            }
        });

        final Button buttonMaps = (Button) findViewById(R.id.buttonMaps);
        buttonMaps.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });

        m_aLoginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        m_aLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                m_aTwitterObject.setM_aSession(TwitterCore.getInstance().getSessionManager().getActiveSession());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        m_aLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    private void testRest() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                RestClient.getJSON(null, "https://en.wikipedia.org/wiki/Minisite", null);
            }
        });
    }

    public void schedule(View aView){
        if(!m_bFlag)
        {
            //Tworzymy Joba, który będzie wykonywany w tle przez serwis
            ComponentName aServiceName = new ComponentName(this, JobSchedulerService.class);
            JobInfo aJobInfo = new JobInfo.Builder(1, aServiceName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPeriodic(10000)
                    .setPersisted(true)
                    .build();

            this.m_aScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            int nResult = m_aScheduler.schedule(aJobInfo);
            if (nResult == JobScheduler.RESULT_SUCCESS) {
                Toast.makeText(this, "Job został schedulowany...", Toast.LENGTH_SHORT).show();
            }

            m_bFlag = true;
        }
        else
        {
            this.m_aScheduler.cancelAll();
            Toast.makeText(this, "Job został zatrzymany...", Toast.LENGTH_SHORT).show();
            m_bFlag = false;
        }
    }
}
