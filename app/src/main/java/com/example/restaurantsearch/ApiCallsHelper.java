package com.example.restaurantsearch;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ApiCallsHelper {

    private Context context;    //Volleyのリクエストキューが使います。
    private ArrayList<Shop> shopsList;
    private int timeoutTimer;   //ミリ秒
    private int maxRetries;     //失敗の場合は何回実行しなおしてみるか


    public ApiCallsHelper(Context context) {
        this.context = context;
        shopsList = new ArrayList<>();
        timeoutTimer = 50000;
        maxRetries = 3;
    }

    //このインターフェースを導入することでasyncの処理が終わったら、成功(onResponse)であっても失敗(onError)であってもMainActivityへ通知することが可能になります。
    public interface VolleyResponseListener {   //there needs to be one per each return, so this generalized name is not ok
        void onResponse(ArrayList<Shop> shopsList);
        void onError(String message);
    }

    //メソッドの名前は「get」ですがasyncで動いていますのでUIスレッドとのやりとりが直接できませんので「void」にします。
    //このメソッドを通じてVolleyResponseListenerが動いて、結果が戻せるのは確かですので名前に「get」を付けました。
    public void getShopsListFromApi(String url, final VolleyResponseListener volleyResponseListener){

        //参考ドキュメンテーション：https://google.github.io/volley/request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    determineMaxPageNumber(response.getJSONObject("results").getString("results_available"));
                    JSONArray shops = response.getJSONObject("results").getJSONArray("shop");
                    fillShopsList(shops);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                //この時点で表示のActivityに戻ります・成功の場合い
                volleyResponseListener.onResponse(shopsList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //この時点で表示のActivityに戻ります・失敗の場合い
//                volleyResponseListener.onError("エラーが発生しました。入力した内容を確認してください。");
                volleyResponseListener.onError(error.getMessage());
            }
        });

        //タイムアウト設定と再実行設定を決めます
        request.setRetryPolicy(new DefaultRetryPolicy(timeoutTimer, maxRetries, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //リクエストキューの代入。onResponseとonErrorResponseより先に実行されます。
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);

    }

    private void determineMaxPageNumber(String resultsAvailable) {
        int maxPageNumber = Integer.parseInt(resultsAvailable) / RestaurantsListActivity.MAX_RESULTS_PER_PAGE;
        if(Integer.parseInt(resultsAvailable) % RestaurantsListActivity.MAX_RESULTS_PER_PAGE != 0){
            maxPageNumber++;
        }
        RestaurantsListActivity.maxPageNumber = maxPageNumber;
    }

    private void fillShopsList(JSONArray shops) {
        try {
            //GoogleのGsonライブラリーを使うのが一般的かもしれませんが今回無視するデータが非常にたくさんですので自分でフィルタリングした方が速いと思います。
            //ですが、APIの結果の構成が対象のクラスともっと近いものだったらGsonの方が効率がよかったと認識しています。
            for (int i = 0; i < shops.length(); i++) {

                //全部一気に書き込んでShopを作ると思ったんですがやはり一つ一つのチェックを行う必要があります。デバッグ中で「sub_genre」がなかったJSONObjectがきました。（name_anyに「おいしい」を設定すれば最初のお店）
                //ですから、ちょっと遅いですが全部確認します。大手企業のJSONが全部同じ形で送信されるはずだと、間違えた判断をしてしまいました。別の関数でチェックと変換を行います。

                shopsList.add(convertToShop(shops.getJSONObject(i)));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Shop convertToShop(JSONObject jsonShop){
        //JSONにデータがなければそのまま空でいいと思います。
        String id, name, genre, address, stationName, latitude, longitude, openingHours, estimatedPrice, access, imageUrl, websiteUrl;

        try{
            id = jsonShop.has("id") ? jsonShop.getString("id") : "";
            name = jsonShop.has("name") ? jsonShop.getString("name") : "";

            //省略のジャンルのこと。「sub_genre」がなくても「genre」にはコードが書いてあることがほとんどのゆですが、わざわざ代わりにそれを探しに行く意味がないです。そのコードも書かれていない可能性もありますから。一回だけチェックさせていただきます。
            genre = "";
            if(jsonShop.has("sub_genre")){
                if(jsonShop.getJSONObject("sub_genre").has("name")){
                    genre = jsonShop.getJSONObject("sub_genre").getString("name");
                }
            }

            address = jsonShop.has("address") ? jsonShop.getString("address") : "";
            stationName = jsonShop.has("station_name") ? jsonShop.getString("station_name") : "";
            latitude = jsonShop.has("lat") ? jsonShop.getString("lat") : "";
            longitude = jsonShop.has("lng") ? jsonShop.getString("lng") : "";
            openingHours = jsonShop.has("open") ? jsonShop.getString("open") : "";

            estimatedPrice = "";
            if(jsonShop.has("budget")){
                if(jsonShop.getJSONObject("budget").has("name")){
                    estimatedPrice = jsonShop.getJSONObject("budget").getString("name");
                }
            }

            access = jsonShop.has("access") ? jsonShop.getString("access") : "";

            //携帯用の最大ファイルです。
            imageUrl = "";
            if(jsonShop.has("photo")){
                if(jsonShop.getJSONObject("photo").has("mobile")){
                    if(jsonShop.getJSONObject("photo").getJSONObject("mobile").has("l")){
                        imageUrl = jsonShop.getJSONObject("photo").getJSONObject("mobile").getString("l");
                    }
                }
            }

            websiteUrl = "";
            if(jsonShop.has("urls")){
                if(jsonShop.getJSONObject("urls").has("pc")){
                    websiteUrl = jsonShop.getJSONObject("urls").getString("pc");
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return new Shop(id,name,genre,address,stationName,latitude,longitude,openingHours,estimatedPrice,access,imageUrl,websiteUrl);
    }

}
