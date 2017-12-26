package com.example.mateusz.citytourapp;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mateusz.citytourapp.scheduler.JobSchedulerService;

public class MainActivity extends AppCompatActivity {

    private static final String JOB_TAG = "scheduled_job";
    private boolean m_bFlag = false;
    private JobScheduler m_aScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
