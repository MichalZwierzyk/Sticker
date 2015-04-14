package com.mz.sticker.screen;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import java.util.ArrayList;

public class MonitoredActivityWithActionBar extends ActionBarActivity {

    private final ArrayList<LifeCycleListener> listeners = new ArrayList<LifeCycleListener>();

    public static interface LifeCycleListener {
        public void onActivityCreated(MonitoredActivityWithActionBar activity);
        public void onActivityDestroyed(MonitoredActivityWithActionBar activity);
        public void onActivityStarted(MonitoredActivityWithActionBar activity);
        public void onActivityStopped(MonitoredActivityWithActionBar activity);
    }

    public static class LifeCycleAdapter implements LifeCycleListener {
        public void onActivityCreated(MonitoredActivityWithActionBar activity) {}
        public void onActivityDestroyed(MonitoredActivityWithActionBar activity) {}
        public void onActivityStarted(MonitoredActivityWithActionBar activity) {}
        public void onActivityStopped(MonitoredActivityWithActionBar activity) {}
    }

    public void addLifeCycleListener(LifeCycleListener listener) {
        if (listeners.contains(listener)) return;
        listeners.add(listener);
    }

    public void removeLifeCycleListener(LifeCycleListener listener) {
        listeners.remove(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (LifeCycleListener listener : listeners) {
            listener.onActivityCreated(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (LifeCycleListener listener : listeners) {
            listener.onActivityDestroyed(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        for (LifeCycleListener listener : listeners) {
            listener.onActivityStarted(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (LifeCycleListener listener : listeners) {
            listener.onActivityStopped(this);
        }
    }
}
