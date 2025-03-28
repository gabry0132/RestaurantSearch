package com.example.restaurantsearch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import java.util.ArrayList;

public class RestaurantsListActivity extends AppCompatActivity {

    private boolean searchParamsExpanded;   //検索条件を表示・非表示の切り替えに使います。
    private RecyclerView recView;           //Webで言うとDivです。一つ一つのお店を格納するレイアウト要素。
    private RelativeLayout relLayoutObfuscationPanel;
    private Button searchButton;
    private ApiCallsHelper apiCallsHelper;  //データ取得のために使います。
    private String API_KEY;                 //環境変数の読み込みが失敗したため、直接プログラムに書きます。
    public static boolean currentlyLoading; //グローバル変数がよくないですが、一回しか呼ぶことがないのでわざわざSingleton Class作らなくてもいいと思います。
                                            //RestaurantsListActivityのオブジェクトをRecyclerViewAdapterクラスに提供するのがもったいないのでpublicにすることにしました。

    //UIのView
    private EditText edtTxtRestaurantName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurants_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        API_KEY = this.getResources().getString(R.string.gourmet_api_key);

        //UI変数の代入
        currentlyLoading = true;
        searchParamsExpanded = false;
        recView = findViewById(R.id.recView);
        searchButton = findViewById(R.id.btnSearch);
        edtTxtRestaurantName = findViewById(R.id.edtTxtRestaurantName);
        relLayoutObfuscationPanel = findViewById(R.id.relLayoutObfuscationPanel);

        //前のページからデータを取得します。
        Intent intent = getIntent();
        edtTxtRestaurantName.setText(intent.getStringExtra("restaurantName"));

        //検索処理の準備と実行を行います。
        //画面内のボタンでも呼べるようにしたいので全部が自分のメソッドになります。
        initiateSearch(searchButton);

    }

    public void initiateSearch(View view) {

        startLoadingAnimation();

        //TODO: input check once more. the user should be checked equally as thoroughly every time they search, and from any page

        //TODO: have within the input check a method call that trims every text field

        //URLの設定を行います
        String url = setUrlForSearch();

        //APIの実行に使うオブジェクトの代入
        apiCallsHelper = new ApiCallsHelper(this);

        //実行を開始します
        getData(url);

    }

    private void startLoadingAnimation() {
        relLayoutObfuscationPanel.setVisibility(View.VISIBLE);
        currentlyLoading = true;
    }

    private void terminateLoadingAnimation() {
        relLayoutObfuscationPanel.setVisibility(View.GONE);
        currentlyLoading = false;
    }

    private void getData(String url) {
        //callbackメソッドを利用して、asyncの実行からでもreturnの戻り値をもらえるようにします。
        //このやり方で進まないと、ロジックを別のクラスへ移動することができなくなりますので、コールバックが一つ増えてもこっちの方がふさわしいと判断させていただきます。
        apiCallsHelper.getShopsListFromApi(url, new ApiCallsHelper.VolleyResponseListener() {
            @Override
            public void onResponse(ArrayList<Shop> shopsList) {
                //データがもらえたのでRecyclerViewに設定します。
                ShopsRecyclerViewAdapter adapter = new ShopsRecyclerViewAdapter(RestaurantsListActivity.this);
                adapter.setShops(shopsList);
                recView.setAdapter(adapter);
                //spanCountが１だけだったらLinearLayoutManagerもいけますが将来的に考えればこっちの方がいいです。構成を変更しようと思ったら数値だけ変更して楽です。
                recView.setLayoutManager(new GridLayoutManager(RestaurantsListActivity.this,1));
                terminateLoadingAnimation();
            }

            @Override
            public void onError(String message) {
                Log.d("onErrorFromWithingetData", message);
                //一回失敗しても2回目は成功だというパターン発生し続いているので失敗しても再読み込みしてみます。
                initiateSearch(searchButton);
            }
        });
    }

    private String setUrlForSearch() {
        String url = "https://webservice.recruit.co.jp/hotpepper/gourmet/v1/?key=" + API_KEY;
        if(!edtTxtRestaurantName.getText().toString().isEmpty()){
            url += "&name_any=" + edtTxtRestaurantName.getText().toString().trim();
        }

        //TODO: paging goes here

        url += "&format=json";
        Log.d("url",url);
        return url;
    }

    //検索条件を表示・非表示の切り替え
    public void revertSearchParamsMenuState(View view){
        //まだロード中だったらクリックさせません。
        if(currentlyLoading) return;

        RelativeLayout relLayoutCollapsed = findViewById(R.id.relLayoutCollapsedSearchParams);
        RelativeLayout relLayoutExpanded = findViewById(R.id.relLayoutExpandedSearchParams);
        //現在の状況を切り替えます
        if(searchParamsExpanded){
            TransitionManager.beginDelayedTransition(relLayoutCollapsed);
            relLayoutCollapsed.setVisibility(View.VISIBLE);
            relLayoutExpanded.setVisibility(View.GONE);
            searchParamsExpanded = false;
        } else {
            TransitionManager.beginDelayedTransition(relLayoutExpanded);
            relLayoutExpanded.setVisibility(View.VISIBLE);
            relLayoutCollapsed.setVisibility(View.GONE);
            searchParamsExpanded = true;
        }
    }

}