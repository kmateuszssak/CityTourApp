package com.example.mateusz.citytourapp.scheduler;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Luki on 2017-12-26.
 */

public class JobSchedulerService extends JobService {
    private BackgroundTask m_aTask;

    @Override
    public boolean onStartJob(final JobParameters aJobParameters) {
        Log.e("SCHEDULER", "START JOB");
        m_aTask = new BackgroundTask(getApplicationContext())
        {
            @Override
            protected void onPostExecute(String sText){
                Toast.makeText(getApplicationContext(), sText, Toast.LENGTH_LONG).show();
                jobFinished(aJobParameters,false);
            }
        };
        m_aTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters aJobParameters) {
        m_aTask.cancel(true);
        return false;
    }
}
