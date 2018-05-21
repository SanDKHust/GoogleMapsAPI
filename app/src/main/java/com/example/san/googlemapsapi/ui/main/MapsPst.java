package com.example.san.googlemapsapi.ui.main;

import android.util.Log;

import com.example.san.googlemapsapi.base.FastNetworkingHelper;
import com.example.san.googlemapsapi.model.place.DirectionParser;
import com.example.san.googlemapsapi.model.place.Leg;
import com.example.san.googlemapsapi.model.place.Route;
import com.example.san.googlemapsapi.model.place.StartLocation;
import com.example.san.googlemapsapi.model.place.Step;
import com.example.san.googlemapsapi.untils.OkHttpUntils;
import com.example.san.googlemapsapi.untils.PolylineUntils;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by San on 1/29/2018.
 */

public class MapsPst implements IMapsPst {
    private IMapsView mMapsView;
    private List<LatLng> mPath;

    public MapsPst(IMapsView mMapsView) {
        this.mMapsView = mMapsView;
        mPath = new ArrayList<>();
    }


    @Override
    public void getDataDirection(final LatLng origin, final LatLng dest,String mode) {
        String url = OkHttpUntils.makeURL(origin, dest,mode);
        Log.i("TAG", "getDataDirection: " + url);
        new FastNetworkingHelper().getDirection(url, new FastNetworkingHelper.FastNetworkingListener() {
            @Override
            public void onSuccess(DirectionParser directionParser) {
                if(directionParser.getStatus().trim().equals("OK")){
                    if (mPath.size() > 0) mPath.clear();
                    List<Route> routes = directionParser.getRoutes();
                    if (routes != null && routes.size() > 0) {
                        for(Route route: routes) {
//                    if (route.getLegs() != null && route.getLegs().size() > 0) {
//                        List<Leg> legs = route.getLegs();
//                        for (int i = 0; i < legs.size(); i++) {
//                            Leg leg = legs.get(i);
//                            if (leg.getSteps() != null && leg.getSteps().size() >0) {
//                                List<Step> steps = leg.getSteps();
//                                if (steps.get(0).getStartLocation() != null) {
//                                    mPath.add(new LatLng(steps.get(0).getStartLocation().getLat(), steps.get(0).getStartLocation().getLng()));
//                                }
//                                for (int j = 0; j < steps.size(); j++) {
//                                    Step step = steps.get(j);
//                                    if (step.getEndLocation() != null) {
//                                        mPath.add(new LatLng(step.getEndLocation().getLat(), step.getEndLocation().getLng()));
//                                    }
//                                }
//                            }
//                        }
//                    }
                            List<LatLng> latLngs = PolylineUntils.decodePolyLine(route.getOverviewPolyline().getPoints());
                            for (LatLng latLng : latLngs) {
                                mPath.add(latLng);
                            }
                        }
                    }
                    Leg leg = routes.get(0).getLegs().get(0);
                    mMapsView.onSuccess(mPath,leg.getDistance().getText(),leg.getDuration().getText());
                }else {
                    mMapsView.onError(directionParser.getStatus());
                }
            }

            @Override
            public void onError(String err_msg) {
                onError(err_msg);
            }
        });
    }
}
