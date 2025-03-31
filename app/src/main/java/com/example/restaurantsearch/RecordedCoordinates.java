package com.example.restaurantsearch;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

public class RecordedCoordinates {
    private ArrayList<HashMap<String,String>> recordedCoordinatesList;

    public RecordedCoordinates(Context context) {
        //全然動的な作り方になっていないんですが、今回の目的と関係ないのでこれでいいとします。
        recordedCoordinatesList = new ArrayList<>();
        HashMap<String,String> tokyo = new HashMap<>();
        HashMap<String,String> ueno = new HashMap<>();
        HashMap<String,String> shibuya = new HashMap<>();
        HashMap<String,String> shinjuku = new HashMap<>();
        HashMap<String,String> nagoya = new HashMap<>();
        HashMap<String,String> osaka = new HashMap<>();
        HashMap<String,String> namba = new HashMap<>();

        tokyo.put("name","東京駅");
        tokyo.put("coordinates", context.getString(R.string.tokyo_station_coordinates));
        ueno.put("name","上野駅");
        ueno.put("coordinates",context.getString(R.string.ueno_station_coordinates));
        shibuya.put("name","渋谷交差点");
        shibuya.put("coordinates",context.getString(R.string.shibuya_crossing_coordinates));
        shinjuku.put("name","新宿駅");
        shinjuku.put("coordinates",context.getString(R.string.shinjuku_station_coordinates));
        nagoya.put("name","名古屋駅");
        nagoya.put("coordinates",context.getString(R.string.nagoya_station_coordinates));
        osaka.put("name","大阪駅（梅田）");
        osaka.put("coordinates",context.getString(R.string.osaka_station_coordinates));
        namba.put("name","難波駅");
        namba.put("coordinates",context.getString(R.string.namba_station_coordinates));

        recordedCoordinatesList.add(tokyo);
        recordedCoordinatesList.add(ueno);
        recordedCoordinatesList.add(shibuya);
        recordedCoordinatesList.add(shinjuku);
        recordedCoordinatesList.add(nagoya);
        recordedCoordinatesList.add(osaka);
        recordedCoordinatesList.add(namba);
    }

    public ArrayList<HashMap<String, String>> getRecordedCoordinatesList() {
        return recordedCoordinatesList;
    }
}
