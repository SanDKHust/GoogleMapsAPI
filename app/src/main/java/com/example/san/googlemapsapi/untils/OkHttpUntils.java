package com.example.san.googlemapsapi.untils;

import com.google.android.gms.maps.model.LatLng;

import static com.example.san.googlemapsapi.untils.Const.Map.API_KEY_DIRECTION;
import static com.example.san.googlemapsapi.untils.Const.Map.URL;

/**
 * Created by san on 26/01/2018.
 */

public class OkHttpUntils {
    public static String makeURL(LatLng origin, LatLng dest){
        StringBuilder stringBuilder = new StringBuilder(URL);
        stringBuilder.append("origin="+origin.latitude+","+origin.longitude+"&");
        stringBuilder.append("destination="+dest.latitude+","+dest.longitude+"&");
        stringBuilder.append("sensor=false&mode=driving&key="+API_KEY_DIRECTION);
        return stringBuilder.toString();
    };
}
