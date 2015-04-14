package com.mz.sticker.exception;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import android.os.Debug;

public class AppUncaughtExceptionHandler implements UncaughtExceptionHandler {

    private static final String HPROF_DUMP_BASENAME = "MemoryLeak.dalvik-hprof";
    private String dataDir;

    public AppUncaughtExceptionHandler(String dataDir) {
        this.dataDir = dataDir;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        String absPath = new File(dataDir, HPROF_DUMP_BASENAME).getAbsolutePath();
        if(ex.getClass().equals(OutOfMemoryError.class)) {
            try {
                Debug.dumpHprofData(absPath);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        ex.printStackTrace();
    }

}
