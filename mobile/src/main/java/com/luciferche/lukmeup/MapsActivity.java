package com.luciferche.lukmeup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;

    private DatabaseReference mDatabase;

    private List<MarkerOptions> activeDrivers;
    private FirebaseAuth mAuth;

    private LinearLayout driverBtnsContainer;
    private HorizontalScrollView scrollview;
    private Spinner driverSpinner;
    private Context mContext;
    private Map<String, Marker> mMarkerMap = new WeakHashMap<>();
    private ArrayAdapter<String> driverArraysAdapter;
    private Bitmap restaurantBitmap;
    private Bitmap driverBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        driverBtnsContainer = (LinearLayout)findViewById(R.id.drivers_buttons_container);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.mipmap.icon_restaurant_1);
        Bitmap b=bitmapdraw.getBitmap();
        restaurantBitmap = Bitmap.createScaledBitmap(b, 60, 60, false);

        bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.mipmap.icon_delivery_50);
        b=bitmapdraw.getBitmap();
        driverBitmap = Bitmap.createScaledBitmap(b, 50, 50, false);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        activeDrivers = new ArrayList<MarkerOptions>();
        mContext = this;

        scrollview = (HorizontalScrollView)findViewById(R.id.scroll_view_drivers);
        driverSpinner = (Spinner)findViewById(R.id.spinner_drivers);
        List<String> driversList = new ArrayList<String>();
        driversList.add(getString(R.string.spinner_drivers_label_all));
        driverArraysAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, driversList );

        driverSpinner.setAdapter(driverArraysAdapter);
        driverSpinner.setSelection(0);
        driverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "ON Item click " + i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "ON Item click NOTHING ");
            }

        });

        Button btnSignout = (Button) findViewById(R.id.btn_sign_out);
        btnSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(mContext, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        getRestaurants();

    }

    private void getRestaurants() {
        DatabaseReference databaseReference = mDatabase.child("restaurants");

        if(databaseReference != null) {
//            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    activeDrivers = new ArrayList<>();

                    for(DataSnapshot singleSnapshot : snapshot.getChildren()){
                        Restaurant restaurant = singleSnapshot.getValue(Restaurant.class);
                        if(restaurant.location != null) {
//                            activeDrivers.add(user);
                            LatLng newLocation = new LatLng(restaurant.location.lat, restaurant.location.lng);
                            Log.i(TAG, "New location " + newLocation.toString());
                            MarkerOptions mo = new MarkerOptions()
                                    .position(newLocation)
                                    .title("RESTORAN - " + restaurant.name)
                                    .anchor(1f, 1f)
                                    .icon(BitmapDescriptorFactory.fromBitmap(restaurantBitmap));
//                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_restaurant_2));
//                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_restaurant));

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

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.e(TAG, "DA l' je moguce");
        UserMarker userMarker = (UserMarker)marker.getTag();
        if(userMarker != null && userMarker.type.equalsIgnoreCase(getString(R.string.user_type_driver))) {
            startDriverHistoryIntent(userMarker.uid);
        } else {
            Log.e(TAG, "Show history for restaurants not yet implemented");
        }
    }

    private void startDriverHistoryIntent(String uid) {
        Intent historyIntent = new Intent(this, MapHistoryActivity.class);
        historyIntent.putExtra("driverUid", uid);
        historyIntent.putExtra("driverName", "luka");
        startActivity(historyIntent);
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

        DatabaseReference databaseReference = mDatabase.child("users");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                User user = dataSnapshot.getValue(User.class);
                user.setFuid(dataSnapshot.getKey());
                if(user.type.equalsIgnoreCase(getString(R.string.user_type_driver))) {

                    addMarker(user);
                    addButton(user);
//                    activeDrivers.add(mo);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.e(TAG, "child changed " + dataSnapshot.getValue(User.class));
                User user = dataSnapshot.getValue(User.class);
                user.setFuid(dataSnapshot.getKey());
                Marker previousMarker = mMarkerMap.get(user.getFuid());

                if(previousMarker != null) {
                    previousMarker.remove();
                }
                addMarker(user);

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
        });

        LatLng beograd = new LatLng(44.8076766, 20.4454173);
        mMap.setMinZoomPreference(10.0f);
//        mMap.addMarker(new MarkerOptions().position(beograd).title("Marker in Beograd"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(beograd));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(13);
        mMap.animateCamera(zoom);

        // Setting a custom info window adapter for the google map
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker marker) {

                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.view_marker_info_window, null);

                // Getting the position from the marker
                LatLng latLng = marker.getPosition();

                // Getting reference to the TextView to set latitude
                TextView tvName = (TextView) v.findViewById(R.id.tv_name);

                // Getting reference to the TextView to set longitude
                TextView tvLastLocationTime = (TextView) v.findViewById(R.id.tv_last_location_date);

              /*  Button btnShowHistory = (Button) v.findViewById(R.id.btn_show_history);
                btnShowHistory.setTag(marker.getTag());
                btnShowHistory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UserMarker userMarker = (UserMarker) view.getTag();
                        startDriverHistoryIntent(userMarker.uid);

                    }
                });
*/
                String time = marker.getSnippet();
                if(time != null)    {
                    tvLastLocationTime.setText(time);
                }
                tvName.setText(marker.getTitle());

                // Returning the view containing InfoWindow contents
                return v;

            }
        });

        // Set a listener for info window events.
        mMap.setOnInfoWindowClickListener(this);

    }

    private void addButton(User user) {
        Log.e(TAG, "childcount --- " +user.name);
        Button userBtn = new Button(mContext);
        userBtn.setText(user.name);
        LinearLayout.LayoutParams layoutParams = new  LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(5, 3, 0, 0); // left, top, right, bottom
        userBtn.setTag(user.getFuid());
//        userBtn.setId(driverBtnsContainer.getChildCount()+100001);
//        userBtn.setLayoutParams(layoutParams);
        userBtn.setTextSize(10);
//        userBtn.setBackgroundColor(getResources().getColor(R.color.btn_primary));
        userBtn.setHeight(50);
        userBtn.setWidth(150);
        userBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDriver((String) view.getTag());
            }
        });
        driverBtnsContainer.addView(userBtn, layoutParams);
//        scrollview.addView(userBtn,layoutParams);
        driverArraysAdapter.add(user.name);
    }

    private void showDriver(String uid) {
        Marker marker = mMarkerMap.get(uid);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(13);
        mMap.animateCamera(zoom);

    }


    private void addMarker(User user) {
        LatLng newLocation = new LatLng(user.lastLocation.lat, user.lastLocation.lng);
        String time="";
        if(user.lastLocationTime != null)
        {
            time= Constants.DATE_FORMATS.LOCAL_TIME_FORMAT.format(new Date(user.lastLocationTime));
        }
        MarkerOptions mo = new MarkerOptions()
                .position(newLocation)
                .title(user.name)
                .snippet(time)
                .anchor(0.5f, 1f)
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.createUserMarker(getBaseContext(),user.name)));
//                .icon(BitmapDescriptorFactory.fromBitmap(driverBitmap));
//                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_delivery_small));
//                .icon(BitmapDescriptorFactory.fromBitmap(driverBitmap));
        Marker marker = mMap.addMarker(mo);
        marker.setTag(new UserMarker(user.getFuid(), user.type));
        mMarkerMap.put(user.getFuid(), marker);
    }

    private class UserMarker {
        public String uid;
        public String type;

        public UserMarker(String fuid, String type) {
            this.uid = fuid;
            this.type = type;
        }
    }

}
