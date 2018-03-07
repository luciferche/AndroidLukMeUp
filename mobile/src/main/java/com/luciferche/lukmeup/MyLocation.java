package com.luciferche.lukmeup;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by luciferche on 2/20/18.
 */

public class MyLocation {
    public Double lat;
    public Double lng;

    public MyLocation() {

    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public LatLng getLatLng() {
        return new LatLng(this.getLat(), this.getLng());
    }
}
