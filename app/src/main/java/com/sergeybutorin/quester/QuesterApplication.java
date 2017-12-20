package com.sergeybutorin.quester;

import android.app.Application;
import android.os.StrictMode;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.sergeybutorin.quester.utils.QuesterDbHelper;
import com.sergeybutorin.quester.utils.SPHelper;

import io.fabric.sdk.android.Fabric;

/**
 * Created by sergeybutorin on 18/12/2017.
 */

public class QuesterApplication extends Application {
    private static QuesterDbHelper dbHelper;
    private static SPHelper spHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = QuesterDbHelper.getInstance(getApplicationContext());
        spHelper = SPHelper.getInstance(getApplicationContext());

        configureCrashReporting();
        setStrictMode();
    }

    public static synchronized QuesterDbHelper getDb() {
        if (dbHelper == null) {
            throw new NullPointerException("DbHelper was not initialized!");
        }
        return dbHelper;
    }

    public static synchronized SPHelper getSp() {
        if (spHelper == null) {
            throw new NullPointerException("SpHelper was not initialized!");
        }
        return spHelper;
    }

    private void configureCrashReporting() {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());
    }

    private void setStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }
}
