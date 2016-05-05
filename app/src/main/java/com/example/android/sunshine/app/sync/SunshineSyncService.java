package com.example.android.sunshine.app.sync;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Intent service used to perform background syncs. Typically invoked via repeating alarm.
 * Alarm can be scheduled by calling ScheduleAlarm().
 *
 * Sync logic is stored in {@link SunshineSyncEngine}. This class just handles scheduling and
 * management of the background process.
 */
public class SunshineSyncService extends IntentService {
    private static final Object sSyncAdapterLock = new Object();
    private static final String TAG = "SunshineSyncService";
    private static SunshineSyncEngine sSunshineSyncEngine = null;

    public SunshineSyncService() {
        super("SunshineSyncService");
    }

    public SunshineSyncService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        Log.d("SunshineSyncService", "onCreate - SunshineSyncService");
        super.onCreate();
        synchronized (sSyncAdapterLock) {
            if (sSunshineSyncEngine == null) {
                sSunshineSyncEngine = new SunshineSyncEngine(getApplicationContext());
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Sync intent received. Fetching data from network.");
        sSunshineSyncEngine.performUpdate();
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Immediately start a SunshineSyncService instance in the background, to perform a network
     * sync.
     *
     * @param context
     */
    public static void SyncImmediately(Context context) {
        Log.i(TAG, "Immediate sync requested. Sending sync intent.");
        Intent startServiceIntent = new Intent(context, SunshineSyncService.class);

        context.startService(startServiceIntent);
    }

    /**
     * Schedule a repeating alarm, to periodically wake the device up and perform a background
     * data sync.
     * @param context
     */
    public static void ScheduleAlarm(Context context) {
        Log.i(TAG, "Scheduling alarm, interval: " + AlarmManager.INTERVAL_HALF_HOUR / 60000 + " min");
        Intent intent = new Intent(context, SyncAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis(); // Perform initial sync immediately
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Perform subsequent syncs every 30 minutes
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_HALF_HOUR, pIntent);
    }

    /**
     * Broadcast receiver which listens for repeating alarms created by ScheduleAlarm().
     *
     * When fired, this receiver will start the {@link SunshineSyncService} intent service to
     * perform a network sync in the background.
     */
    public static class SyncAlarmReceiver extends BroadcastReceiver {
        private static final String TAG = "SyncAlarmReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Broadcast received, sending sync intent to SunshineSyncService");
            Intent i = new Intent(context, SunshineSyncService.class);
            context.startService(i);
        }
    }

    /**
     * Broadcast receiver for BOOT_COMPLETED event.
     *
     * Upon receipt, schedules a repeating alarm to trigger background data syncs.
     */
    public static class BootBroadcastReceiver extends WakefulBroadcastReceiver {
        private static final String TAG = "BootBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received BOOT_COMPLETED broadcast. Scheduling alarm.");
            ScheduleAlarm(context);
        }
    }
}
