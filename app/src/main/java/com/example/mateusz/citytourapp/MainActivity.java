package com.example.mateusz.citytourapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mateusz.citytourapp.scheduler.MJobScheduler;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

public class MainActivity extends AppCompatActivity {

    private static final String JOB_TAG = "scheduled_job";
    private FirebaseJobDispatcher m_aJobDispatcher;
    private boolean m_bFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicjalizacja job dispatchera
        m_aJobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver((this)));

        //Inicjalizacja listenera button
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                testRest();
            }
        });
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
            Job aJob = m_aJobDispatcher.newJobBuilder().
                    setService(MJobScheduler.class).
                    setLifetime(Lifetime.FOREVER).
                    setRecurring(true).
                    setTag(JOB_TAG).
                    setTrigger(Trigger.executionWindow(10,15)).
                    setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL).
                    setReplaceCurrent(false).
                    setConstraints(Constraint.ON_ANY_NETWORK).
                    build();

            m_aJobDispatcher.mustSchedule(aJob);
            Toast.makeText(this, "Job został schedulowany...", Toast.LENGTH_SHORT).show();
            m_bFlag = true;
        }
        else
        {
            m_aJobDispatcher.cancel(JOB_TAG);
            Toast.makeText(this, "Job został zatrzymany...", Toast.LENGTH_SHORT).show();
            m_bFlag = false;
        }
    }
}
