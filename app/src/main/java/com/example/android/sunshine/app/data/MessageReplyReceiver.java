package com.example.android.sunshine.app.data;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import com.example.android.sunshine.app.BuildConfig;

/**
 * A receiver that gets called when a reply is sent to a given conversationId
 */
public class MessageReplyReceiver extends BroadcastReceiver {

    public static final String EXTRA_REMOTE_REPLY = "extra_remote_reply";
    // public static final String REPLY_ACTION = BuildConfig.APPLICATION_ID + ".ACTION_MESSAGE_REPLY";
    // public static final String EXTRA_FORECAST = "extra_forecast";

    private static final String TAG = MessageReplyReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        /*
        if (REPLY_ACTION.equals(intent.getAction())) {
            Forecast forecast = intent.getParcelableExtra(EXTRA_FORECAST);
            CharSequence reply = getMessageText(intent);
            if (forecast != null) {
                Log.w(TAG, "Got reply (" + reply + ") for Forecast " + forecast);
            }
        }
        */
    }

    /*
    public static Intent getMessageReplyIntent(Forecast forecast) {
        return new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(MessageReplyReceiver.REPLY_ACTION)
                .putExtra(MessageReplyReceiver.EXTRA_FORECAST, forecast);
    }
    */

    /**
     * Get the message text from the intent.
     * Note that you should call {@code RemoteInput#getResultsFromIntent(intent)} to process
     * the RemoteInput.
     */
    private static CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(EXTRA_REMOTE_REPLY);
        }
        return null;
    }
}
