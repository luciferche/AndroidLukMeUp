package com.luciferche.lukmeup;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by luciferche on 2/5/18.
 */

public class LocationUtils {
    public static final String TAG = "LocationUtils";
    public final static String KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested";
    public final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    public final static String CHANNEL_ID = "channel_01";
    public static final int EARTH_RADIUS = 6378137;  // Earth’s mean radius in meter

    private static DatabaseReference mFirebaseRef = null;
    private static String fUid = null;

    public static void setRequestingLocationUpdates(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply();
    }

    public static boolean getRequestingLocationUpdates(Context context) {
        Log.i("LocationUtils","GET REQUESTING LOCATION UPDATES -- ");
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false);
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    public static void sendNotification(Context context, String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, DriverActivity.class);

        notificationIntent.putExtra("from_notification", true);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(DriverActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Define the notification settings.
        builder.setSmallIcon(com.luciferche.lukmeup.R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        com.luciferche.lukmeup.R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle("Location update")
                .setContentText(notificationDetails)
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(false);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(com.luciferche.lukmeup.R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);

            // Channel ID
            builder.setChannelId(CHANNEL_ID);
        }

        // Issue the notification
        mNotificationManager.notify(Constants.NOTIFICATION_ID.NOTIFICATION_FROM_INTENT, builder.build());
    }

    public static void removeNotification(Context context) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        mNotificationManager.cancel(Constants.NOTIFICATION_ID.NOTIFICATION_FROM_INTENT);
    }


    /**
     * Returns the title for reporting about a list of {@link Location} objects.
     *
     * @param context The {@link Context}.
     */
    public static String getLocationResultTitle(Context context, List<Location> locations) {
        String numLocationsReported = context.getResources().getQuantityString(
                com.luciferche.lukmeup.R.plurals.num_locations_reported, locations.size(), locations.size());
        return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(new Date());
    }

    /**
     * Returns te text for reporting about a list of  {@link Location} objects.
     *
     * @param locations List of {@link Location}s.
     */
    private static String getLocationResultText(Context context, List<Location> locations) {
        if (locations.isEmpty()) {
            return context.getString(com.luciferche.lukmeup.R.string.unknown_location);
        }
        StringBuilder sb = new StringBuilder();
        for (Location location : locations) {
            sb.append("(");
            sb.append(location.getLatitude());
            sb.append(", ");
            sb.append(location.getLongitude());
            sb.append(")");
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void setLocationUpdatesResult(Context context, List<Location> locations) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle(context, locations)
                        + "\n" + getLocationResultText(context, locations))
                .apply();
    }

    public static String getLocationUpdatesResult(Context context) {
        Log.i("LocationUtils","getLocationUpdatesResult -- " + PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, ""));

        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }

    private class LocationModel {
        public double latitude;
        public double longitude;


    }

    public static void saveLocationToDb(Context context, Location lastLocation) {
        Log.e(TAG, "called save");
        if(mFirebaseRef == null) {
            mFirebaseRef= FirebaseDatabase.getInstance().getReference();
            fUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        try {
            if(fUid == null) {
                Log.e(TAG,"no fuid");
            }

            Map<String, Double> locationMap = new HashMap<>();
            locationMap.put("lat", lastLocation.getLatitude());
            locationMap.put("lng", lastLocation.getLongitude());
            long locationTime = lastLocation.getTime();
            DatabaseReference locationReference = mFirebaseRef.child("locations/" + fUid + "/" + locationTime);
            String locationKey = locationReference.push().getKey();
            locationReference.setValue(locationMap);
//            mFirebaseRef.child("locations/" + fUid + "/" + lastLocation.getTime()).push().setValue(locationMap);
            mFirebaseRef.child("users/" + fUid + "/lastLocation").setValue(locationMap);
            mFirebaseRef.child("users/" + fUid + "/lastLocationKey").setValue(locationKey);
            mFirebaseRef.child("users/" + fUid + "/lastLocationTime").setValue(locationTime);
        } catch(Exception e) {
            Log.e(TAG, "Posting to Firebase failed: " + e.toString());
            Toast.makeText(context,"Failed to send location data.", Toast.LENGTH_LONG).show();
        }
    }

    public static void saveLocationToDb(Context context, List<Location> locations) {
       saveLocationToDb(context, locations.get(0));
    }
//
//    rad: function (x) {
//        return x * Math.PI / 180
//    },
    public static double getDistance(MyLocation pointFrom, MyLocation pointTo) {
//        EARTH_RADIUS
        double distanceLat = Math.toRadians(pointTo.getLat() - pointFrom.getLat());
        double distanceLong = Math.toRadians(pointTo.getLng() - pointFrom.getLng());
        double a = Math.sin(distanceLat / 2) * Math.sin(distanceLat / 2) +
                Math.cos(Math.toRadians(pointFrom.getLat())) * Math.cos(Math.toRadians(pointTo.getLat())) *
                        Math.sin(distanceLong / 2) * Math.sin(distanceLong / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double distance = EARTH_RADIUS * c;
        return distance;
    }
//    getDistance: function (pointFrom, pointTo) {
//        var R = 6378137 // Earth’s mean radius in meter
//        var dLat = this.rad(pointTo.lat() - pointFrom.lat())
//        var dLong = this.rad(pointTo.lng() - pointFrom.lng())
//        var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
//                Math.cos(this.rad(pointFrom.lat())) * Math.cos(this.rad(pointTo.lat())) *
//                        Math.sin(dLong / 2) * Math.sin(dLong / 2)
//        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
//        var d = R * c
//        return d // returns the distance in meter
//    }


//    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";
//
//    /**
//     * Returns true if requesting location updates, otherwise returns false.
//     *
//     * @param context The {@link Context}.
//     */
//    static boolean requestingLocationUpdates(Context context) {
//        return PreferenceManager.getDefaultSharedPreferences(context)
//                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
//    }
//
//    /**
//     * Stores the location updates state in SharedPreferences.
//     * @param requestingLocationUpdates The location updates state.
//     */
//    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
//        PreferenceManager.getDefaultSharedPreferences(context)
//                .edit()
//                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
//                .apply();
//    }
//
//    /**
//     * Returns the {@code location} object as a human readable string.
//     * @param location  The {@link Location}.
//     */
//    static String getLocationText(Location location) {
//        return location == null ? "Unknown location" :
//                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
//    }
//
//    static String getLocationTitle(Context context) {
//        return context.getString(R.string.location_updated,
//                DateFormat.getDateTimeInstance().format(new Date()));
//    }

}
