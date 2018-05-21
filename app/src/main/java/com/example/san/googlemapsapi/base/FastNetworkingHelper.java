package com.example.san.googlemapsapi.base;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.example.san.googlemapsapi.model.place.DirectionParser;

/**
 * Created by San on 01/28/2018.
 */

public class FastNetworkingHelper<T> {

    public void getDirection(String url, final FastNetworkingListener listener){
        AndroidNetworking.get(url)
                .setTag(this)
                .build()
                .getAsObject(DirectionParser.class, new ParsedRequestListener<DirectionParser>() {
                    @Override
                    public void onResponse(DirectionParser response) {
                        listener.onSuccess(response);
                    }

                    @Override
                    public void onError(ANError anError) {
                        listener.onError(anError.getMessage());
                    }
                });
    }
    public interface FastNetworkingListener{
        void onSuccess(DirectionParser directionParser);
        void onError(String err_msg);
    }
}
