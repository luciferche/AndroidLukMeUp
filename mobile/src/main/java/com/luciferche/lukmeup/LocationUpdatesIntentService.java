package com.luciferche.lukmeup;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class LocationUpdatesIntentService extends IntentService {

    public static final String ACTION_PROCESS_UPDATES =
            "com.luciferche.lukmeup.action" +
                    ".PROCESS_UPDATES";
    private static final String TAG = LocationUpdatesIntentService.class.getSimpleName();
    public LocationUpdatesIntentService() {
        super(TAG);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "usao");

//        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "izasao");
//        Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();

//        LocationUtils.removeNotification(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(TAG, "onHandleIntent");

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    LocationUtils.setLocationUpdatesResult(this, locations);
                    LocationUtils.saveLocationToDb(this, locations);
//                    LocationUtils.sendNotification(this, LocationUtils.getLocationResultTitle(this, locations));
//                    Log.i(TAG, LocationUtils.getLocationUpdatesResult(this));
                }
            }

        }
    }

}
