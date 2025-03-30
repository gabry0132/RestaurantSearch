package com.example.restaurantsearch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.ContextCompat;

public class MapsOpener {

    //何回もGoogle Maps等を開くことがあるので地図アプリ専用クラスに移動します。
    public static void openMapsApp(Context context){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //デフォルト位置で地図アプリを開く
        intent.setData(Uri.parse("geo:0,0"));
        //Maps以外に複数の地図アプリがあれば選べられるようにします。
        Intent chooser = Intent.createChooser(intent, "Launch Maps");
        ContextCompat.startActivity(context, chooser, null);
    }

    public static void openMapsApp(Context context, Double latitude, Double longitude){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //デフォルト位置で地図アプリを開く
        intent.setData(Uri.parse("geo:" + latitude + "," + longitude));
        //Maps以外に複数の地図アプリがあれば選べられるようにします。
        Intent chooser = Intent.createChooser(intent, "Launch Maps");
        ContextCompat.startActivity(context, chooser, null);
    }
}
