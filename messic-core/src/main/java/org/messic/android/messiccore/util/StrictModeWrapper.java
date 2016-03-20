package org.messic.android.messiccore.util;

import android.os.StrictMode;

public class StrictModeWrapper {
    static {
        try {
            Class.forName("android.os.StrictMode", true, Thread.currentThread().getContextClassLoader());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void checkAvailable() {
    }

    public static void enableDefaults() {
        StrictMode.enableDefaults();
    }

    public static void setStrictMode() {
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