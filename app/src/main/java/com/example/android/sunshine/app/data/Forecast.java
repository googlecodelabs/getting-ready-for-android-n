package com.example.android.sunshine.app.data;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;

public class Forecast implements Parcelable {
    private static final int MILLISECONDS_IN_A_DAY = 1000 * 60 * 60 * 24;

    public int mWeatherId;
    public double mHigh;
    public double mLow;
    public String mDescription;
    public int mDaysSinceEpoch;
    public long mMillisecondsSincEpoch;
    public String mNotificationContentText;
    public String mNotificationTitle;

    private Forecast() { }

    // Auto-generated constructor.
    protected Forecast(Parcel in) {
        mWeatherId = in.readInt();
        mHigh = in.readDouble();
        mLow = in.readDouble();
        mDescription = in.readString();
        mDaysSinceEpoch = in.readInt();
        mMillisecondsSincEpoch = in.readLong();
        mNotificationContentText = in.readString();
        mNotificationTitle = in.readString();
    }

    // Auto-generated creator.
    public static final Creator<Forecast> CREATOR = new Creator<Forecast>() {
        @Override
        public Forecast createFromParcel(Parcel in) {
            return new Forecast(in);
        }

        @Override
        public Forecast[] newArray(int size) {
            return new Forecast[size];
        }
    };

    public static Forecast fromCursorRow(Context context, Cursor cursor) {
        Forecast forecast = new Forecast();
        // NOTE: The constant lookup of the column index for each field in this query is not optimal
        // If this were being done for more than a few rows you would want to cache the lookups.
        forecast.mWeatherId = cursor.getInt(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
        forecast.mHigh = cursor.getDouble(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
        forecast.mLow = cursor.getDouble(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
        forecast.mDescription = cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
        String date = cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE));
        forecast.mMillisecondsSincEpoch = Long.parseLong(date);

        // Generate an ID to uniquely identify the notifications.
        forecast.mDaysSinceEpoch = (int)(Long.parseLong(date) / MILLISECONDS_IN_A_DAY);

        // Define the text of the forecast.
        forecast.mNotificationContentText = String.format(context.getString(
                R.string.format_notification),
                forecast.mDescription,
                Utility.formatTemperature(context, forecast.mHigh),
                Utility.formatTemperature(context, forecast.mLow));

        forecast.mNotificationTitle = context.getString(R.string.app_name);

        return forecast;
    }

    // Auto-generated method.
    @Override
    public int describeContents() {
        return 0; // Always return zero unless you're using File descriptors.
    }

    // Auto-generated method.
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mWeatherId);
        parcel.writeDouble(mHigh);
        parcel.writeDouble(mLow);
        parcel.writeString(mDescription);
        parcel.writeInt(mDaysSinceEpoch);
        parcel.writeLong(mMillisecondsSincEpoch);
        parcel.writeString(mNotificationContentText);
        parcel.writeString(mNotificationTitle);
    }
}
