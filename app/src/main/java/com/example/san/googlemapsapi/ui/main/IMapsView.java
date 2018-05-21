package com.example.san.googlemapsapi.ui.main;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by san on 26/01/2018.
 */

public interface IMapsView {
    void onSuccess(List<LatLng> latLngs,String s,String s2);
    void onError(String err_msg);
}
