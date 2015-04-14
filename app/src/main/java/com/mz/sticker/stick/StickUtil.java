package com.mz.sticker.stick;

import android.app.ProgressDialog;
import android.os.Handler;

import com.mz.sticker.screen.MonitoredActivity;
import com.mz.sticker.screen.MonitoredActivityWithActionBar;

public class StickUtil {

    public static void startBackgroundJob(MonitoredActivityWithActionBar activity,
                                          String title, String message, Runnable job, Handler handler) {
        // Make the progress dialog uncancelable, so that we can gurantee
        // the thread will be done before the activity getting destroyed
        ProgressDialog dialog = ProgressDialog.show(
                activity, title, message, true, false);
        new Thread(new BackgroundJob(activity, job, dialog, handler)).start();
    }

    private static class BackgroundJob extends MonitoredActivityWithActionBar.LifeCycleAdapter implements Runnable {

        private final MonitoredActivityWithActionBar activity;
        private final ProgressDialog dialog;
        private final Runnable job;
        private final Handler handler;
        private final Runnable cleanupRunner = new Runnable() {
            public void run() {
                activity.removeLifeCycleListener(BackgroundJob.this);
                if (dialog.getWindow() != null) dialog.dismiss();
            }
        };

        public BackgroundJob(MonitoredActivityWithActionBar activity, Runnable job,
                             ProgressDialog dialog, Handler handler) {
            this.activity = activity;
            this.dialog = dialog;
            this.job = job;
            this.activity.addLifeCycleListener(this);
            this.handler = handler;
        }

        public void run() {
            try {
                job.run();
            } finally {
                handler.post(cleanupRunner);
            }
        }

        @Override
        public void onActivityDestroyed(MonitoredActivityWithActionBar activity) {
            // We get here only when the onDestroyed being called before
            // the cleanupRunner. So, run it now and remove it from the queue
            cleanupRunner.run();
            handler.removeCallbacks(cleanupRunner);
        }

        @Override
        public void onActivityStopped(MonitoredActivityWithActionBar activity) {
            dialog.hide();
        }

        @Override
        public void onActivityStarted(MonitoredActivityWithActionBar activity) {
            dialog.show();
        }
    }

}
