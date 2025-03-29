package com.example.restaurantsearch;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class LocationPermissions {

    public static void askLocationPermissions(Context context) {
        //正確な位置が欲しいですが、Android 12以降その求め方が変わって、両方をリクエストしなければならないそうです。（https://developer.android.com/develop/sensors-and-location/location/permissions/runtime）
        int fineLocationPermission = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocationPermission = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION);


        //fine location (必須) チェックして、なければ求めます。
        if(fineLocationPermission != PackageManager.PERMISSION_GRANTED){
            //ユーザーにリクエストする時に配列で依頼します。それでも整理のために別々でやります。
            String[] permissionsRequest = { android.Manifest.permission.ACCESS_FINE_LOCATION };
            //requestCodeパラメータは何でもよさそうです。
            ActivityCompat.requestPermissions((Activity) context, permissionsRequest, 998);
        }

        //coarce location (必須) チェックして、なければ求めます。
        if(coarseLocationPermission != PackageManager.PERMISSION_GRANTED){
            //ユーザーにリクエストする時に配列で依頼します。それでも整理のために別々でやります。
            String[] permissionsRequest = { Manifest.permission.ACCESS_COARSE_LOCATION };
            //requestCodeパラメータは何でもよさそうです。
            ActivityCompat.requestPermissions((Activity) context, permissionsRequest, 999);
        }
    }
}
