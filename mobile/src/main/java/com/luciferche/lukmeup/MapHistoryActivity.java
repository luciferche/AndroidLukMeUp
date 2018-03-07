package com.luciferche.lukmeup;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;

public class MapHistoryActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapHistoryActivity.class.getSimpleName();

    private static final int DATE_DIALOG_ID = 12;

    private GoogleMap mMap;
    private DatabaseReference mDatabase;
    private String driverUid;

    private Bitmap iconPointBitmap;
    private Bitmap startPointBitmap;
    private Bitmap endPointBitmap;
    private Bitmap restaurantPointBitmap;

    private Map<Long, Marker> mMarkerMap;
    private PolylineOptions mPath;
    private Polyline mUserLine;

    private EditText etDate;
    private TextView tvDriverTitle;
    private TextView tvDistanceCovered;
    private String driverName;

    private String filterStartAt;
    private String filterEndAt;

    private int mYear,mMonth,mDay;
    private DatabaseReference databaseReference;

    private double distanceToday;
    private Date firstLocationTime;
    private Date lastLocationTime;

    private MyLocation previousLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_history);

        Bundle bundle = getIntent().getExtras();
        if(bundle!= null) {
            driverName = bundle.get("driverName").toString();
            driverUid = bundle.getString("driverUid");
        }
        distanceToday = 0;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mMarkerMap = new WeakHashMap<>();

        //init views and bitmaps for icons
        initUi();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Log.i(TAG, "onCreate started " + driverUid);

    }

    private void initUi() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        filterStartAt = "" + calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        filterEndAt = "" + calendar.getTimeInMillis();

        Log.i(TAG, "Filter startAt " + new Date(Long.valueOf(filterStartAt)).toString());
        Log.i(TAG, "Filter   EndAt " + new Date(Long.valueOf(filterEndAt)).toString());

        tvDriverTitle = (TextView) findViewById(R.id.tv_driver_title);
        tvDistanceCovered = (TextView) findViewById(R.id.tv_distance_covered);

        etDate = (EditText)  findViewById(R.id.et_date);

        etDate.setText(Constants.DATE_FORMATS.LOCAL_DATE_FORMAT.format(new Date()));
        etDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(v.getContext(), mDateSetListener, mYear, mMonth, mDay);
                dialog.getDatePicker().setMaxDate(new Date().getTime());
                dialog.show();
            }
        });

        if(driverName != null) {
            tvDriverTitle.setText(tvDriverTitle.getText() + " - " + driverName);
        }

        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.mipmap.icon_user_avatar_2);
        Bitmap b=bitmapdraw.getBitmap();
        iconPointBitmap = Bitmap.createScaledBitmap(b, 50, 50, false);

        bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.mipmap.icon_green_lantern_50);
        b=bitmapdraw.getBitmap();
        startPointBitmap = Bitmap.createScaledBitmap(b, 50, 50, false);

        bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.mipmap.icon_destination_48);
        b=bitmapdraw.getBitmap();
        endPointBitmap = Bitmap.createScaledBitmap(b, 50, 50, false);

        bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.mipmap.icon_restaurant_1);
        b=bitmapdraw.getBitmap();
        restaurantPointBitmap= Bitmap.createScaledBitmap(b, 50, 50, false);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mPath = new PolylineOptions()
                .width(5)
                .color(Color.RED);
        databaseReference = mDatabase.child("locations").child(driverUid);
        getLocationHistoryFromDb();

        LatLng beograd = new LatLng(44.8076766, 20.4454173);
        mMap.setMinZoomPreference(10.0f);
//        mMap.addMarker(new MarkerOptions().position(beograd).title("Marker in Beograd"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(beograd));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(13);
        mMap.animateCamera(zoom);

        getRestaurants();

    }
    private void getRestaurants() {
        DatabaseReference databaseReference = mDatabase.child("restaurants");

        if(databaseReference != null) {
//            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    for(DataSnapshot singleSnapshot : snapshot.getChildren()){
                        Restaurant restaurant = singleSnapshot.getValue(Restaurant.class);
                        if(restaurant.location != null) {
//                            activeDrivers.add(user);
                            Log.i("RESTAURANT ADDED", restaurant.location.lat + "," + restaurant.location.lng);
                            LatLng newLocation = new LatLng(restaurant.location.lat, restaurant.location.lng);
                            MarkerOptions mo = new MarkerOptions()
                                    .position(newLocation)
                                    .title("RESTORAN - " + restaurant.name)
                                    .anchor(Constants.ICON_ANCHOR.ANCHOR_WIDTH, Constants.ICON_ANCHOR.ANCHOR_HEIGHT)
                                    .icon(BitmapDescriptorFactory.fromBitmap(restaurantPointBitmap));

                            mMap.addMarker(mo);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "getRestaurants readcancelled");
                }
            });
        } else {
            Log.e(TAG, "getRestaurants null");

        }
    }
    private void getLocationHistoryFromDb() {
//        if(databaseReference != null) {
//            databaseReference.removeEventListener(dbLocationEventListener);
//        }
//        Query dbQuery = databaseReference.orderByKey();
        Log.e(TAG, "DB QUERY -filterStartAt-- " + filterStartAt);
        Log.e(TAG, "DB QUERY -filterEndAt-- " + filterEndAt);

        Query dbQuery = databaseReference.orderByKey().startAt(filterStartAt).endAt(filterEndAt);
        Log.e(TAG, "DB QUERY --- " + dbQuery.toString());
//        dbQuery.addChildEventListener(dbLocationEventListener);
        dbQuery.addListenerForSingleValueEvent(dbLocationOnceEventListener);
    }

    private ValueEventListener dbLocationOnceEventListener= new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                MyLocation location= snapshot.getValue(MyLocation.class);

                Date time = new Date(Long.valueOf(snapshot.getKey()));
                Log.i("LOCATION ADDED", location.getLat() + "," + location.getLng());
//                addMarker(location, time);
                addToPath(location, time);
            }
            mUserLine = mMap.addPolyline(mPath);
            tvDistanceCovered.setText(tvDistanceCovered.getText() +
                    " - " +
                    new DecimalFormat(Constants.NUMBER_FORMATS.DISTANCE).format(distanceToday) +
                    " km");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };
/*
    private ChildEventListener dbLocationEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
            MyLocation location= dataSnapshot.getValue(MyLocation.class);

            Date time = new Date(Long.valueOf(dataSnapshot.getKey()));
            Log.i("LOCATION ADDED", location.getLat() + "," + location.getLng());
            addMarker(location, time);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
            Log.e(TAG, "child changed " + dataSnapshot.getValue(User.class));
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.e(TAG, "child REmoved " + dataSnapshot.getValue(User.class));
            User user = dataSnapshot.getValue(User.class);
            user.setFuid(dataSnapshot.getKey());
            Marker previousMarker = mMarkerMap.get(dataSnapshot.getKey());
            previousMarker.remove();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            Log.e(TAG, "CHILD moved " + dataSnapshot.getValue(User.class));
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "database error" + databaseError.getMessage());

        }
    };
    private PolylineOptions addPoint(LatLng location) {
        if(mPath == null) {
            mPath = new PolylineOptions()
                    .add(location)
                    .width(5)
                    .color(Color.RED);
        } else {
            mPath.add(location);
        }
        return mPath;
    }

*/
    private void addToPath(MyLocation location, Date dateTime) {
        //first we set start time only on beginning when it's null - later keep every new datetime in lastlocationtime and the last one written is ours endtime
        if(firstLocationTime == null) {
            firstLocationTime = dateTime;
        }
        lastLocationTime = dateTime;

        if(previousLocation != null) {
            distanceToday += LocationUtils.getDistance(previousLocation, location);
        }
        previousLocation = location;
        mPath.add(new LatLng(location.getLat(), location.getLng()));
    }

    private void addMarker(MyLocation location, Date dateTime) {
        String time = Constants.DATE_FORMATS.LOCAL_TIME_FORMAT.format(dateTime);
        MarkerOptions mo = new MarkerOptions()
                .position(location.getLatLng())
                .title(time)
                .anchor(Constants.ICON_ANCHOR.ANCHOR_WIDTH, Constants.ICON_ANCHOR.ANCHOR_HEIGHT)
//                .snippet(time)
                .icon(BitmapDescriptorFactory.fromBitmap(iconPointBitmap));
//                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_delivery_small));
//                .icon(BitmapDescriptorFactory.fromBitmap(driverBitmap));
        Marker marker = mMap.addMarker(mo);
//        marker.setTag(user.type);
        mMarkerMap.put(dateTime.getTime(), marker);
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "BACK PRESSED");
        // Otherwise defer to system default behavior.
        super.onBackPressed();
    }

    /**
     * Reset filters for date and time, reset distance
     * !!! Remove polyline
     */
    private DatePickerDialog.OnDateSetListener mDateSetListener =new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            firstLocationTime = null;
            lastLocationTime = null;
            distanceToday = 0;

            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            Calendar thisInstance = Calendar.getInstance();
            thisInstance.set(Calendar.YEAR, year);
            thisInstance.set(Calendar.MONTH, monthOfYear);
            thisInstance.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            etDate.setText(Constants.DATE_FORMATS.LOCAL_DATE_FORMAT.format(thisInstance.getTime()));
            filterStartAt = "" + thisInstance.getTimeInMillis();
            thisInstance.add(Calendar.DAY_OF_MONTH, 1);
            filterEndAt = "" + thisInstance.getTimeInMillis();

            tvDistanceCovered.setText(getText(R.string.label_distance_covered) + " - 0 km");

            mUserLine.remove();
        }

    };

}
