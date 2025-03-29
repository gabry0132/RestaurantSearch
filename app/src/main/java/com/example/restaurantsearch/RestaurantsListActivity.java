package com.example.restaurantsearch;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import java.util.ArrayList;

public class RestaurantsListActivity extends AppCompatActivity {

    private boolean searchParamsExpanded;               //検索条件を表示・非表示の切り替えに使います。
    private RecyclerView recView;                       //Webで言うとDivです。一つ一つのお店を格納するレイアウト要素。
    private RelativeLayout relLayoutObfuscationPanel;
    private Button btnSearch, btnToPreviousPage, btnToNextPage;
    private ApiCallsHelper apiCallsHelper;              //データ取得のために使います。
    private String apiKey;                              //環境変数の読み込みが失敗したため、直接プログラムに書きます。
    public static boolean currentlyLoading;             //グローバル変数がよくないですが、一回しか呼ぶことがないのでわざわざSingleton Class作らなくてもいいと思います。
                                                        //RestaurantsListActivityのオブジェクトをRecyclerViewAdapterクラスに提供するのがもったいないのでpublicにすることにしました。

    public static int maxPageNumber;                    //本クエリの結果のページ数。ApiClassHelperもアクセスできるようにパブリックにします。

    public static final int MAX_RESULTS_PER_PAGE = 15;  //ページごとに何件表示するか。APIドキュメンテーション（https://webservice.recruit.co.jp/doc/hotpepper/reference.html）によると「最小1、最大100」です。
    private int currentResultsPage;                     //結果画面の何ページ目

    //UIのView
    private EditText edtTxtRestaurantName;
    private TextView txtPageNumber;
    private Spinner spnLocation, spnDistance;

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

        apiKey = this.getResources().getString(R.string.gourmet_api_key);

        //UI変数の代入
        maxPageNumber = 1;  //データがもらったら上書きされます。データがない時に１にすると「次のページへ」ボタンが表示されないので１にします。
        currentlyLoading = false;
        searchParamsExpanded = false;
        recView = findViewById(R.id.recView);
        btnSearch = findViewById(R.id.btnSearch);
        spnLocation = findViewById(R.id.spnLocation);
        spnDistance = findViewById(R.id.spnDistance);
        btnToNextPage = findViewById(R.id.btnNextPage);
        txtPageNumber = findViewById(R.id.txtPageNumber);
        btnToPreviousPage = findViewById(R.id.btnPreviousPage);
        edtTxtRestaurantName = findViewById(R.id.edtTxtRestaurantName);
        relLayoutObfuscationPanel = findViewById(R.id.relLayoutObfuscationPanel);

        //前のページからデータを取得します。
        Intent intent = getIntent();
        edtTxtRestaurantName.setText(intent.getStringExtra("restaurantName"));
        spnLocation.setSelection(intent.getIntExtra("spnLocationIndex", 1));
        spnDistance.setSelection(intent.getIntExtra("spnDistanceIndex", 1));

        //検索処理の準備と実行を行います。
        //画面内のボタンでも呼べるようにしたいので全部が自分のメソッドになります。
        initiateSearch(btnSearch);

    }

    public void initiateSearch(View view) {

        startLoadingAnimation();

        //新しい検索の場合はページが１にします。別のページへ移動するというのは「同じクエリですがスタートがずれます」という意味になります。
        if(view.getId() == R.id.btnPreviousPage){
            currentResultsPage--;
        } else if(view.getId() == R.id.btnNextPage) {
            currentResultsPage++;
        } else {
            currentResultsPage = 1;
        }


        //TODO: input check once more. the user should be checked equally as thoroughly every time they search, and from any page

        //TODO: have within the input check a method call that trims every text field

        //URLの設定を行います
        String url = setUrlForSearch();

        //APIの実行に使うオブジェクトの代入
        apiCallsHelper = new ApiCallsHelper(this);

        //実行を開始します
        getData(url);

    }

    private void handlePageMovementButtonsVisibility() {
        //最初のページの場合は「前のページへ」ボタンを非表示
        if(currentResultsPage == 1){
            btnToPreviousPage.setVisibility(View.GONE);
        } else {
            //表示する場合はテキストを設定します。
            btnToPreviousPage.setText(getString(R.string.previous_page_button_text, currentResultsPage - 1));
            btnToPreviousPage.setVisibility(View.VISIBLE);
        }

        //最後のページだったら「次のページへ」ボタンを非表示。データセットの"results_available"で判断しています。
        if(currentResultsPage == maxPageNumber){
            btnToNextPage.setVisibility(View.GONE);
        } else {
            //表示する場合はテキストを設定します。
            btnToNextPage.setText(getString(R.string.next_page_button_text, currentResultsPage + 1));
            btnToNextPage.setVisibility(View.VISIBLE);
        }
    }

    private void startLoadingAnimation() {
        relLayoutObfuscationPanel.setVisibility(View.VISIBLE);
        currentlyLoading = true;
        //ボタンをクリックできないようにします。
        btnSearch.setEnabled(false);
        btnToPreviousPage.setEnabled(false);
        btnToNextPage.setEnabled(false);
        //txtViewにも入力できないようにします。
        edtTxtRestaurantName.setEnabled(false);
    }

    private void terminateLoadingAnimation() {
        relLayoutObfuscationPanel.setVisibility(View.GONE);
        currentlyLoading = false;
        //ボタンをクリックできるように戻します。
        btnSearch.setEnabled(true);
        btnToPreviousPage.setEnabled(true);
        btnToNextPage.setEnabled(true);
        //txtViewにまた入力できなるようにします。
        edtTxtRestaurantName.setEnabled(true);
    }

    private void setCurrentPageNumberText(){
        //ページカウンターに設定。ページ移動のボタンのテキストは表示する際に設定します。
        txtPageNumber.setText(getString(R.string.current_page, currentResultsPage, maxPageNumber));
    }

    private void getData(String url) {
        //callbackメソッドを利用して、asyncの実行からでもreturnの戻り値をもらえるようにします。
        //このやり方で進まないと、ロジックを別のクラスへ移動することができなくなりますので、コールバックが一つ増えてもこっちの方がふさわしいと判断させていただきます。
        apiCallsHelper.getShopsListFromApi(url, new ApiCallsHelper.VolleyResponseListener() {
            @Override
            public void onResponse(ArrayList<Shop> shopsList) {

                //まだデータが揃っていないですが、ロードステータスを解除しないと　成功 ⇔ 失敗　モードの切り替えができません。
                //データを揃えるにはそんなに時間がかからないので大丈夫だとします。古い携帯でも一瞬もかからないはずです。
                terminateLoadingAnimation();

                //結果がなければ伝えます
                if(!shopsList.isEmpty()){

                    //直前ヒットなしの検索から、成功モードに切り替えます
                    handleSuccessfulSearch();

                    //データがもらえたのでRecyclerViewに設定します。
                    ShopsRecyclerViewAdapter adapter = new ShopsRecyclerViewAdapter(RestaurantsListActivity.this);
                    adapter.setShops(shopsList);
                    recView.setAdapter(adapter);
                    //spanCountが１だけだったらLinearLayoutManagerもいけますが将来的に考えればこっちの方がいいです。構成を変更しようと思ったら数値だけ変更して楽です。
                    recView.setLayoutManager(new GridLayoutManager(RestaurantsListActivity.this, 1));

                    //結果の何ページ目を設定する
                    setCurrentPageNumberText();

                    //ボタンの表示設定をもう一回確認
                    handlePageMovementButtonsVisibility();

                } else {

                    //直前に、成功モードした検索だったらヒットなしモードに切り替えます
                    handleNoResultsFromSearch();

                }

            }

            @Override
            public void onError(String message) {
                Log.d("onErrorFromWithinGetData", message);
            }
        });
    }

    private void handleNoResultsFromSearch() {
        //簡単に再検索できるように、検索条件を表示します。
        if(!searchParamsExpanded) revertSearchParamsMenuState(findViewById(R.id.txtExpandSearchParamsIntro));

        //結果表示とページ移動のRelLayoutを非表示。直前の結果が残るからです。
        recView.setVisibility(View.GONE);
        findViewById(R.id.relLayoutPageButtonsHolder).setVisibility(View.GONE);

        //画面上に検索失敗のメッセージを表示します。
        findViewById(R.id.relLayoutSearchFailedMessageHolder).setVisibility(View.VISIBLE);
    }

    private void handleSuccessfulSearch() {
        //簡単に結果を見えるように、検索条件を隠します。
        if(searchParamsExpanded) revertSearchParamsMenuState(findViewById(R.id.txtExpandSearchParamsIntro));

        //結果表示とページ移動のRelLayoutを表示。非表示になるのは、この直前に検索が失敗場合だけですが、今回結果があるため表示します。
        recView.setVisibility(View.VISIBLE);
        findViewById(R.id.relLayoutPageButtonsHolder).setVisibility(View.VISIBLE);

        //画面上に検索失敗のメッセージを非表示します。表示するのは、この直前に検索が失敗場合だけです。
        findViewById(R.id.relLayoutSearchFailedMessageHolder).setVisibility(View.GONE);
    }

    private String setUrlForSearch() {

        //lat,lng の形で求めます。
        String coordinates = getCoordinates();

        if(coordinates == null){
            Toast.makeText(this, "座標が見つかりませんでした。", Toast.LENGTH_LONG).show();
            return null;
        }

        //BufferingしているのでBufferにしましたがコードの構成から見るとStringの方が分かりやすいかもしれません。
        StringBuffer url = new StringBuffer();
        url.append("https://webservice.recruit.co.jp/hotpepper/gourmet/v1/?key=" + apiKey);

        //緯度の設定
        url.append("&lat=" + coordinates.split(",")[0]);

        //経度の設定
        url.append("&lng=" + coordinates.split(",")[1]);

        //検索範囲の設定。APIの順番で保存していますのでindexがあれば一致します。ただ、API側で1から始まるので値+1します。 参考：https://webservice.recruit.co.jp/doc/hotpepper/reference.html 、「range」に注目
        url.append("&range=" + (spnDistance.getSelectedItemPosition() + 1));

        if(!edtTxtRestaurantName.getText().toString().isEmpty()){
            url.append("&name_any=" + edtTxtRestaurantName.getText().toString().trim());
        }

        url.append("&start=" + (MAX_RESULTS_PER_PAGE * (currentResultsPage - 1) + 1));
        url.append("&count=" + MAX_RESULTS_PER_PAGE);
        url.append("&format=json");

        Log.d("url",url.toString());

        return url.toString();
    }

    private String getCoordinates() {

        if(spnLocation.getSelectedItem().toString().equals("現在地")){

            //本当は前の画面に聞いていますが、使う前にもう一度確認しないとアンドロイドが落ち着かないみたいので、絶対OKもらうようにします。
            while (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                LocationPermissions.askLocationPermissions(this);
            }

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            //GPS_PROVIDERの代わりにNETWORK_PROVIDERもありますがGPSの方が正確です。
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if(location == null){
                //getLastKnownLocationを使っていますので新しい携帯とかそういうサービスが使ったことがないユーザーだったらnullの可能性があります。
                //本番であればonLocationChanged()イベントリスナーを立てて、ユーザーにちょっと移動することを依頼しますが、今回そこまでやらないと思います。テストするのはパソコンのエミュレータだろうし、簡単に移動することができません。
                //今回だけ位置情報がなければユーザーに作ってもらうことにしました。
                Toast.makeText(this, "位置情報がありません。Google Maps等を一旦開いてから戻ってください。", Toast.LENGTH_LONG).show();

                //携帯にGoogle Mapsアプリケーションがあれば私が開いてあげます。
                Uri gmmIntentUri = Uri.parse("geo:34.67274672754893, 135.65652437366944");  //東大阪市の大好きな展望台です。
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }

            return location.getLatitude() + "," + location.getLongitude();
        }

        //最悪の場合はエラーになります。
        return null;

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