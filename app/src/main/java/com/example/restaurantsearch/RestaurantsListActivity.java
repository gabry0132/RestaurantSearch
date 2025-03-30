package com.example.restaurantsearch;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import androidx.transition.AutoTransition;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;

import java.util.ArrayList;

public class RestaurantsListActivity extends AppCompatActivity {

    private boolean searchParamsExpanded;               //検索条件を表示・非表示の切り替えに使います。
    private RecyclerView recView;                       //Webで言うとDivです。一つ一つのお店を格納するレイアウト要素。
    private RelativeLayout relLayoutObfuscationPanel, relLayoutCollapsedSearchParams, relLayoutExpandedSearchParams, relLayoutCoordinates;
    private Button btnSearch, btnOpenMaps, btnToPreviousPage, btnToNextPage;
    private ApiCallsHelper apiCallsHelper;              //データ取得のために使います。
    private String apiKey;                              //環境変数の読み込みが失敗したため、直接プログラムに書きます。
    public static boolean currentlyLoading;             //グローバル変数がよくないですが、一回しか呼ぶことがないのでわざわざSingleton Class作らなくてもいいと思います。
                                                        //RestaurantsListActivityのオブジェクトをRecyclerViewAdapterクラスに提供するのがもったいないのでpublicにすることにしました。

    public static int maxPageNumber;                    //本クエリの結果のページ数。ApiClassHelperもアクセスできるようにパブリックにします。

    public static final int MAX_RESULTS_PER_PAGE = 15;  //ページごとに何件表示するか。APIドキュメンテーション（https://webservice.recruit.co.jp/doc/hotpepper/reference.html）によると「最小1、最大100」です。
    private int currentResultsPage;                     //結果画面の何ページ目

    //UIのView
    private EditText edtTxtRestaurantName, edtTxtLatitude, edtTxtLongitude;
    private TextView txtPageNumber, txtFromDistanceContinuationText;
    private Spinner spnLocation, spnDistance;
    private Location locationCurrentSearch;                //検索の際に使う位置情報。
    private static final int EARTH_RADIUS = 6371;          //検索位置からお店までの距離のために使う。Kmです。

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
        locationCurrentSearch = null;   //APIのURLを設定する際に求めます。現在地で検索しないと代入されません。
        recView = findViewById(R.id.recView);
        btnSearch = findViewById(R.id.btnSearch);
        btnOpenMaps = findViewById(R.id.btnOpenMaps);
        spnLocation = findViewById(R.id.spnLocation);
        spnDistance = findViewById(R.id.spnDistance);
        btnToNextPage = findViewById(R.id.btnNextPage);
        txtPageNumber = findViewById(R.id.txtPageNumber);
        edtTxtLatitude = findViewById(R.id.edtTxtLatitude);
        edtTxtLongitude = findViewById(R.id.edtTxtLongitude);
        btnToPreviousPage = findViewById(R.id.btnPreviousPage);
        edtTxtRestaurantName = findViewById(R.id.edtTxtRestaurantName);
        relLayoutCoordinates = findViewById(R.id.relLayoutCoordinates);
        relLayoutObfuscationPanel = findViewById(R.id.relLayoutObfuscationPanel);
        relLayoutExpandedSearchParams = findViewById(R.id.relLayoutExpandedSearchParams);
        relLayoutCollapsedSearchParams = findViewById(R.id.relLayoutCollapsedSearchParams);
        txtFromDistanceContinuationText = findViewById(R.id.txtFromDistanceContinuationText);

        //MainActivityと同じようにSpinnerにEvent Listenerを追加します。
        spnLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //MainActivityと本画面の検索項目・レイアウトを一致するつもりではないので同じメソッドでも変わる可能性があるため、メソッドを共有するのではなく本ページにコピーします。
                handleLocationSpinnerSelectionChanged((Spinner) adapterView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //前のページからデータを取得します。
        Intent intent = getIntent();
        edtTxtRestaurantName.setText(intent.getStringExtra("restaurantName"));
        spnLocation.setSelection(intent.getIntExtra("spnLocationIndex", 1));    //EventListenerですぐに設定されません。この後直接呼びます。
        edtTxtLatitude.setText(intent.getStringExtra("latitude"));      //空文字が送信される場合もあります。
        edtTxtLongitude.setText(intent.getStringExtra("longitude"));    //空文字が送信される場合もあります。
        spnDistance.setSelection(intent.getIntExtra("spnDistanceIndex", 1));

        //検索処理の準備と実行を行います。
        //画面内のボタンでも呼べるようにしたいのでViewのパラメータが必要となります。
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

        //設定中に問題が発生した場合はページを閉じます。
        //Toastとかでユーザーにエラーを知らせるのがこの時点ではなく、発生した瞬間です。そうするとどんな問題なのか具体的にお知らせできます。
        if(url == null){
            Intent returnIntent = new Intent(this, MainActivity.class);
            startActivity(returnIntent);
            finish();
        }

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
        btnOpenMaps.setEnabled(false);
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
        btnOpenMaps.setEnabled(true);
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

                    //検索の位置から店ごととの距離を計算します。位置情報がなければスキップします。
                    //APIの結果が既に近い順に並べていますが、具体的にどれくらい遠いか表示したいです。
                    if(locationCurrentSearch != null){
                        setDistanceOnAllShops(shopsList);
                    }

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

    private void setDistanceOnAllShops(ArrayList<Shop> shopsList) {
        for (Shop shop :
                shopsList) {
            int distance = determineShopDistanceFromSearch(shop);
            shop.setDistanceFromSearchCoordinates(distance + "");
        }
    }

    private int determineShopDistanceFromSearch(Shop shop) {
        //lat、lngがDoubleですが　「equirectangular approximation」（等角近似）を利用して計算しますのでRadian形式に変換します（double型が変りません）。
        double latSearchRad = Math.toRadians(locationCurrentSearch.getLatitude());
        double lngSearchRad = Math.toRadians(locationCurrentSearch.getLongitude());
        double latShopRad = Math.toRadians(Double.parseDouble(shop.getLatitude()));
        double lngShopRad = Math.toRadians(Double.parseDouble(shop.getLongitude()));

        //世界中の計算ではない（APIにより最大検索距離3000m）ですので Harvestineとか Vinecentyとか、複雑な数式を使わなくていいです。
        //最も簡単な数式で距離を計算します。参考資料：https://www.baeldung.com/java-find-distance-between-points　（2番：Equirectangular Distance Approximation に注目）。
        //Google Mapsが表示する徒歩距離等ではなく、マンハッタン距離の形になります。道の案内のためではなく、どれぐらい遠いかイメージするための値になりますのでこのままでいいとします。
        //本当に道の案内も含めて、具体的に徒歩で何分とか、そういう情報がGoogle Maps Apiで取得できるだろうけど今回は対象外だと判断させていただきます。
        double x = (lngShopRad - lngSearchRad) * Math.cos((latSearchRad + latShopRad) / 2);
        double y = latShopRad - latSearchRad;
        double distance = Math.sqrt(x * x + y * y) * EARTH_RADIUS;

        //テスト用
        //Log.d("distance","distance to " + shop.getLatitude() + "," + shop.getLongitude() + " = " + distance + " and it returns " + ((int) Math.round(distance * 1000)));

        //Km -> m
        return (int) Math.round(distance * 1000);
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

        locationCurrentSearch = getSearchLocation();

        if(locationCurrentSearch == null) return null;  //前のメソッドからのToastでユーザーに説明しています。

        //BufferingしているのでBufferにしましたがコードの構成から見るとStringの方が分かりやすいかもしれません。
        StringBuffer url = new StringBuffer();
        url.append("https://webservice.recruit.co.jp/hotpepper/gourmet/v1/?key=" + apiKey);

        //緯度の設定
        url.append("&lat=" + locationCurrentSearch.getLatitude());

        //経度の設定
        url.append("&lng=" + locationCurrentSearch.getLongitude());

        //検索範囲の設定。APIの順番で保存していますのでindexがあれば一致します。ただ、API側で1から始まるので値+1します。 参考：https://webservice.recruit.co.jp/doc/hotpepper/reference.html 、「range」に注目
        url.append("&range=" + (spnDistance.getSelectedItemPosition() + 1));

        //追加の任意項目があればここで設定します。
        if(!edtTxtRestaurantName.getText().toString().isEmpty()){
            url.append("&name_any=" + edtTxtRestaurantName.getText().toString().trim());
        }

        url.append("&start=" + (MAX_RESULTS_PER_PAGE * (currentResultsPage - 1) + 1));
        url.append("&count=" + MAX_RESULTS_PER_PAGE);
        url.append("&format=json");

        Log.d("url",url.toString());

        return url.toString();
    }

    private Location getSearchLocation() {

        //本当は前の画面に聞いていますが、使う前にもう一度確認しないとアンドロイドが落ち着かないみたいので、絶対OKもらうようにします。
        while (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            LocationPermissions.askLocationPermissions(this);
        }

        //LocationManagerで作られたLocationが必ず正しい必須項目を持っています。参考：https://developer.android.com/reference/android/location/Location
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //GPS_PROVIDERの代わりにNETWORK_PROVIDERもありますがGPSの方が正確です。
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(location == null){
            //getLastKnownLocationを使っていますので新しい携帯とかそういうサービスが使ったことがないユーザーだったらnullの可能性があります。
            //本番であればonLocationChanged()イベントリスナーを立てて、ユーザーにちょっと移動することを依頼しますが、今回そこまでやらないと思います。テストするのはパソコンのエミュレータだろうし、簡単に移動することができません。
            //今回だけ位置情報がなければユーザーに作ってもらうことにしました。
            Toast.makeText(this, "位置情報がありません。Google Maps等を一旦開いてから戻ってください。", Toast.LENGTH_LONG).show();

            //携帯にGoogle Mapsアプリケーションがあれば私が開きます。
            MapsOpener.openMapsApp(this, 35.71014978852772, 139.81068430321494); //東京スカイツリーです。

        }

        /*

        ここから、ユーザーが入力した座標があれば取得したものを上書きします。
        このアプリケーションではlocation型オブジェクトは座標の共有にしか使いませんが、現在地で検索していない場合にも位置情報を取得するのが必須です。
        そうしないとlocationCurrentSearchの代入がスキップされて、代わりに偽物の空locationにする必要があります。locationCurrentSearch変数にはあってはならないことです。

        空のlocation型オブジェクトに座標を設定する悪い例：
         Location location = new Location("");  ＜－ providerがない
         location.setLatitude(Double.parseDouble(edtTxtLatitude.getText().toString()));
         location.setLongitude(Double.parseDouble((edtTxtLongitude.getText().toString())));

        将来的にこのlocationCurrentStateを使うのであれば、普通の状態ではないlocation型オブジェクトを使うことになるので危ないです。
        現在地が求められていない時にも取得します。それで、問題ないlocationオブジェクトがあるはず(LocationManager OK -> System Services OK)だからそれに座標を上書きします。

        !　座標以外のためにlocationCurrentSearchを使うなら要注意です !
        https://developer.android.com/reference/android/location/Location
        !　LocationManager経由で作られなかったLocation型オブジェクトがエラーを発生する原因となりえます　!

         */

        //ユーザーが「地域を指定」を選んで、座標を入力した場合
        if(spnLocation.getSelectedItemPosition() == 1 && location != null){
            location.setLatitude(Double.parseDouble(edtTxtLatitude.getText().toString()));
            location.setLongitude(Double.parseDouble((edtTxtLongitude.getText().toString())));
        }

        //最悪の場合はnullを返します。
        return location;

    }

    //検索条件を表示・非表示の切り替え
    public void revertSearchParamsMenuState(View view){
        //まだロード中だったらクリックさせません。
        if(currentlyLoading) return;

        //現在の状況を切り替えます
        if(searchParamsExpanded){
            relLayoutCollapsedSearchParams.setVisibility(View.VISIBLE);
            relLayoutExpandedSearchParams.setVisibility(View.GONE);

            searchParamsExpanded = false;
        } else {
            relLayoutExpandedSearchParams.setVisibility(View.VISIBLE);
            relLayoutCollapsedSearchParams.setVisibility(View.GONE);

            //表示するときにspnLocationの選択による座標も表示したいのでもう一度そのチェックを実行します。
            handleLocationSpinnerSelectionChanged(spnLocation);

            searchParamsExpanded = true;
        }
    }

    private void handleLocationSpinnerSelectionChanged(Spinner spinner){
        //SelectionChangedなので絶対ユーザーが見える時に実行されるコードになるはずのですが、念のためにチェックを入れます。
        if(currentlyLoading) return;

        //アニメーションを付けます。
        TransitionManager.beginDelayedTransition(relLayoutExpandedSearchParams, new ChangeBounds().setDuration(100));

        if(spinner.getSelectedItemPosition() == 1){ //「指定」選択を選んだ場合

            //座標入力パネルを表示。見えるようになったと言っても、外側のRelLayoutが見れない限り表示できません。外側のRelLayoutが見れるまでの準備だとします。
            relLayoutCoordinates.setVisibility(View.VISIBLE);
            //「から」メッセージを座標の隣に移動します（本当は二つの非表示の切り替えです）
            txtFromDistanceContinuationText.setVisibility(View.GONE);

        } else {  //現在地を選んだ場合

            //座標入力パネルを非表示
            relLayoutCoordinates.setVisibility(View.GONE);
            //「から」メッセージを上に戻します。
            txtFromDistanceContinuationText.setVisibility(View.VISIBLE);
        }
    }

    public void openMapsApp(View view){
        MapsOpener.openMapsApp(this);
    }

}