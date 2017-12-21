package com.example.mateusz.citytourapp.scheduler;

import android.util.Log;
import android.widget.Toast;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by Luki on 2017-12-20.
 */

public class MJobScheduler extends JobService {
    private AppTaskJob m_aAppTaskJob;

    @Override
    public boolean onStartJob(final JobParameters aJobParameters) {
        Log.e("SCHEDULER", "START JOB");
        m_aAppTaskJob = new AppTaskJob(){
            @Override
          protected void onPostExecute(String sText){
              Toast.makeText(getApplicationContext(), sText, Toast.LENGTH_LONG).show();
              jobFinished(aJobParameters,false);
          }
        };
        m_aAppTaskJob.execute();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters aJobParameters) {
        m_aAppTaskJob.cancel(true);
        return false;
    }

}
