package com.example.san.googlemapsapi.ui.main;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by San on 1/29/2018.
 */

public interface IMapsPst {
    void getDataDirection(LatLng origin, LatLng dest,String mode);
}
