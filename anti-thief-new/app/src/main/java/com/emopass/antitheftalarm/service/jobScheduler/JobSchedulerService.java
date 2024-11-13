package com.emopass.antitheftalarm.service.jobScheduler;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.emopass.antitheftalarm.service.AntiTheftService;
import com.emopass.antitheftalarm.service.BackgroundManager;


@SuppressLint("SpecifyJobSchedulerIdRange")
public class JobSchedulerService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e("XXX", "JobSchedulerService onStartJob");
        if (BackgroundManager.getInstance(this).canStartService()) {
            BackgroundManager.getInstance(this).startService(AntiTheftService.class);
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e("XXX", "JobSchedulerService onStopJob");
        return false;
    }
}
