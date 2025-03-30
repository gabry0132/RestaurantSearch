package com.example.restaurantsearch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.transition.TransitionManager;

public class MainActivity extends AppCompatActivity {

    private EditText edtTxtRestaurantName, edtTxtLatitude, edtTxtLongitude;
    private Spinner spnLocation, spnDistance;
    private TextView txtSpinnerContinuationText, txtCoordinatesContinuationText;
    private RelativeLayout relLayoutCoordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //UI変数を代入します。
        spnLocation = findViewById(R.id.spnLocation);
        spnDistance = findViewById(R.id.spnDistance);
        //本当はGoogle Maps APIを使って、ユーザーに地図を見て場所を選んでもらいたかったのですが、Google Maps APIは有料になりました。API KEYをちゃんと隠せるようになるまで有料APIはやめたほうがいいと思います。
        //代わりに地図を開いて、自分で取ってもらいます。わざわざ座標を探しに行く人がいないと思うんですが、この状況から元々作りたかった地図バージョンへの移動が簡単なので将来のために機能をカットしないことにしました。
        edtTxtLatitude = findViewById(R.id.edtTxtLatitude);
        edtTxtLongitude = findViewById(R.id.edtTxtLongitude);
        relLayoutCoordinates = findViewById(R.id.relLayoutCoordinates);
        edtTxtRestaurantName = findViewById(R.id.edtTxtRestaurantName);
        txtSpinnerContinuationText = findViewById(R.id.txtSpinnerContinuationText);
        txtCoordinatesContinuationText = findViewById(R.id.txtCoordinatesContinuationText);

        //GUIの属性リストになさそうなのでここでEvent Listenerを追加します。
        spnLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                handleLocationSpinnerSelectionChanged((Spinner) adapterView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //GPSの認可を求めます。実際に使うのは次のページですが、この時点で聞くとアプリケーション開いた瞬間に聞かれます。
        LocationPermissions.askLocationPermissions(this);
    }

    private void handleLocationSpinnerSelectionChanged(Spinner spinner){

        if(spinner.getSelectedItemPosition() == 1){ //「指定」選択を選んだ場合

            //アニメーションも付けます。
            TransitionManager.beginDelayedTransition(relLayoutCoordinates);

            //座標入力パネルを表示
            relLayoutCoordinates.setVisibility(View.VISIBLE);
            //「から」メッセージを座標の隣に移動します（本当は二つの非表示の切り替えです）
            txtSpinnerContinuationText.setVisibility(View.GONE);

        } else {  //現在地を選んだ場合

            //座標入力パネルを非表示
            relLayoutCoordinates.setVisibility(View.GONE);

            //「から」メッセージを上に戻します。
            txtSpinnerContinuationText.setVisibility(View.VISIBLE);
        }
    }

    public void openWebPage(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://webservice.recruit.co.jp/"));
        startActivity(browserIntent);
    }

    public void openMapsApp(View view){
        MapsOpener.openMapsApp(this);
    }

    public void invokeNextPageAndSearch(View view){

        String restaurantName = edtTxtRestaurantName.getText().toString().trim();
        //両方の画面のスピナーが同じソースを見ていますのでindexだけあれば良い。
        int spnLocationIndex = spnLocation.getSelectedItemPosition();
        int spnDistanceIndex = spnDistance.getSelectedItemPosition();

        //TODO: check for inputs, add new TextView to inform the user when the input is necessary or incorrect and display that TextView whenever there is a problem with the inputs

        Intent intent = new Intent(this, RestaurantsListActivity.class);
        intent.putExtra("restaurantName", restaurantName);
        intent.putExtra("spnLocationIndex", spnLocationIndex);
        intent.putExtra("latitude", edtTxtLatitude.getText().toString());
        intent.putExtra("longitude", edtTxtLongitude.getText().toString());
        intent.putExtra("spnDistanceIndex", spnDistanceIndex);
        startActivity(intent);

    }
}