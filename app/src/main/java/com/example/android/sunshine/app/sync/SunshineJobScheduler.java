package com.example.android.sunshine.app.sync;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;

/**
 * Frontend to GcmNetworkManager, responsible for scheduling background jobs in a power-efficient
 * manner.
 *
 * On devices running API 21 (Lollipop) or later, this acts as a facade in front of the system's
 * JobScheduler API. On older devices, this provides equivilent functionality via the Google Play
 * Services runtime.
 *
 * Note that it is NOT necessary to spawn a separate service for task execution. GcmTaskService
 * should, in most cases, be fully self contained.
 */
public class SunshineJobScheduler extends GcmTaskService {
    public static final int MINUTES_AS_SEC = 60;
    public static final int HOURS_AS_SEC = 60*60;
    public static final String TASK_TAG_CHARGING = "sync_charging";
    public static final String TASK_TAG_BATTERY = "sync_battery";
    private static final String TAG = "SunshineJobScheduler";

    // Task specifications
    /**
     * Task specification for running jobs while connected to a charger. In this case, we allow
     * execution to occur frequently, as power usage is not constrained.
     */
    private static final Task CHARGING_TASK = new PeriodicTask.Builder()
            .setService(SunshineJobScheduler.class)
            .setPeriod(30*MINUTES_AS_SEC)
            .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
            .setRequiresCharging(true)
            .setPersisted(true)
            .setUpdateCurrent(true)
            .setTag(TASK_TAG_CHARGING)
            .build();

    /**
     * Task specification for running jobs while on battery. In this case, we execute jobs less
     * frequently to conserve battery life.
     *
     * Note that additional restrictions may be put on this task by the system if the device is
     * in Doze or App Standby mode due to inactivity. However, we should still try to be friendly
     * to the battery even when these aren't in effect.
     */
    private static final Task BATTERY_TASK = new PeriodicTask.Builder()
            .setService(SunshineJobScheduler.class)
            .setPeriod(6 * HOURS_AS_SEC)
            .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
            .setRequiresCharging(false)
            .setPersisted(true)
            .setUpdateCurrent(true)
            .setTag(TASK_TAG_BATTERY)
            .build();

    // In the event Google Play Services is restarted, this method will be called.
    @Override
    public void onInitializeTasks() {
        Log.i(TAG, "onInitializeTasks() called");
        super.onInitializeTasks();
        ScheduleTasks(this);
    }

    /**
     * Method to schedule tasks. Called from either MainActivity.onCreate() for interactive
     * sessions, or onInitializeTasks() in the event Play Services is restarted.
     *
     * Note that since all jobs are flagged as "persisted" in their specifications (above), these
     * will automatically persist across reboots.
     *
     * @param context
     */
    public static void ScheduleTasks(Context context) {
        GoogleApiAvailability googleAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode == ConnectionResult.SUCCESS) {
            Log.i(TAG, "Scheduling tasks");
            GcmNetworkManager gcmNetworkManager = GcmNetworkManager.getInstance(context);

            // Run every 30 minutes while the device is on a charger
            gcmNetworkManager.schedule(CHARGING_TASK);

            // ... And run every 6 hours while the device is on battery power
            gcmNetworkManager.schedule(BATTERY_TASK);
        } else {
            // We display a user actionable error inside MainActivity. We'll just abort and log the
            // error here.
            Log.e(TAG, "Google Play Services is not available, unable to schedule jobs");
        }
    }

    // Actual work is done here
    @Override
    public int onRunTask(TaskParams taskParams) {
        switch (taskParams.getTag()) {
            case TASK_TAG_CHARGING:
            case TASK_TAG_BATTERY:
                Log.i(TAG, "Scheduled sync task executing");
                SunshineSyncEngine syncEngine = new SunshineSyncEngine(getApplicationContext());
                syncEngine.performUpdate();
                return GcmNetworkManager.RESULT_SUCCESS;
            default:
                Log.wtf(TAG, "Unrecognized task tag: " + taskParams.getTag());
                return GcmNetworkManager.RESULT_FAILURE;
        }
    }
}
