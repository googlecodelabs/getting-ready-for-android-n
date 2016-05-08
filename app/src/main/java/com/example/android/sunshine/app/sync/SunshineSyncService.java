package com.example.android.sunshine.app.sync;

import android.app.IntentService;
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
}
