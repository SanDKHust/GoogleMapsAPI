package com.example.san.googlemapsapi.untils;

import static com.example.san.googlemapsapi.untils.Const.Map.KEY;
import static com.example.san.googlemapsapi.untils.Const.Map.URL;

/**
 * Created by san on 26/01/2018.
 */

public class OkHttpUntils {
    public static String getURL(String query){
        StringBuilder stringBuilder = new StringBuilder(URL);
        stringBuilder.append(query);
        stringBuilder.append("&key=");
        stringBuilder.append(KEY);
        return stringBuilder.toString();
    };
}
