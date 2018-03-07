package com.luciferche.lukmeup;

import android.location.Location;

import java.util.Date;

/**
 * Created by luciferche on 2/1/18.
 */

public class User {

    public String name;

    public String email;

    public String address;

    public Location currentLocation;

    public String lastLocationKey;

    public Long lastLocationTime;

    public MyLocation lastLocation;

    public String password;

    public String type;

    public String fuid;

    public User(){
        /*default no-argument constructor needed*/
    }
    public User(String email, String name, String type) {
        this.email = email;
        this.name = name;
        this.type = type;
    }

    public String getFuid() {
        return fuid;
    }

    public void setFuid(String fuid) {
        this.fuid = fuid;
    }
}
