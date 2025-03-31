package com.example.restaurantsearch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText edtTxtRestaurantName, edtTxtLatitude, edtTxtLongitude;
    private Spinner spnLocation, spnDistance, spnGenre, spnBudget;
    private TextView txtSpinnerContinuationText, txtNameDisablingBudget;
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
        initializeUIComponents();

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

        //店舗名で検索すると予算が無視されます。ユーザーに分かりやすくするために、店舗名に入力すれば自動的に予算を選択させません。
        edtTxtRestaurantName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handleRestaurantNameTextChanged(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        //GPSの認可を求めます。実際に使うのは次のページですが、この時点で聞くとアプリケーション開いた瞬間に聞かれます。
        LocationPermissions.askLocationPermissions(this);
    }

    private void initializeUIComponents() {
        spnLocation = findViewById(R.id.spnLocation);
        spnDistance = findViewById(R.id.spnDistance);
        spnGenre = findViewById(R.id.spnShopGenre);
        spnBudget = findViewById(R.id.spnBudget);
        //本当はGoogle Maps APIを使って、ユーザーに地図を見て場所を選んでもらいたかったのですが、Google Maps APIは有料になりました。API KEYをちゃんと隠せるようになるまで有料APIはやめたほうがいいと思います。
        //代わりに地図を開いて、自分で取ってもらいます。わざわざ座標を探しに行く人がいないと思うんですが、この状況から元々作りたかった地図バージョンへの移動が簡単なので将来のために機能をカットしないことにしました。
        edtTxtLatitude = findViewById(R.id.edtTxtLatitude);
        edtTxtLongitude = findViewById(R.id.edtTxtLongitude);
        relLayoutCoordinates = findViewById(R.id.relLayoutCoordinates);
        edtTxtRestaurantName = findViewById(R.id.edtTxtRestaurantName);
        txtSpinnerContinuationText = findViewById(R.id.txtSpinnerContinuationText);
        txtNameDisablingBudget = findViewById(R.id.txtNameDisablingBudget);
    }

    private void handleRestaurantNameTextChanged(CharSequence charSequence) {
        if(!charSequence.toString().isEmpty()){
            spnBudget.setEnabled(false);
            //未選択に戻します。そうすると直接urlに書きません。
            spnBudget.setSelection(0);
            txtNameDisablingBudget.setVisibility(View.VISIBLE);
        } else {
            spnBudget.setEnabled(true);
            txtNameDisablingBudget.setVisibility(View.GONE);
        }
    }

    private void handleLocationSpinnerSelectionChanged(Spinner spinner){

        if(spinner.getSelectedItemPosition() == 1){ //「指定」選択を選んだ場合

            //アニメーションも付けます。
            TransitionManager.beginDelayedTransition(relLayoutCoordinates);

            //座標入力パネルを表示
            relLayoutCoordinates.setVisibility(View.VISIBLE);
            //「から」メッセージを座標の隣に移動します（本当は二つの非表示の切り替えです）
            txtSpinnerContinuationText.setVisibility(View.GONE);

        } else {

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
        int spnGenreIndex = spnGenre.getSelectedItemPosition();
        int spnBudgetIndex = spnBudget.getSelectedItemPosition();

        //全入力項目を.trim()してから入力チェックを行います。
        trimAllTextInputs();
        if(!checkInputsValidity()){
            return;
        }

        Intent intent = new Intent(this, RestaurantsListActivity.class);
        intent.putExtra("spnLocationIndex", spnLocationIndex);
        intent.putExtra("latitude", edtTxtLatitude.getText().toString());
        intent.putExtra("longitude", edtTxtLongitude.getText().toString());
        intent.putExtra("spnDistanceIndex", spnDistanceIndex);
        intent.putExtra("restaurantName", restaurantName);
        intent.putExtra("spnGenreIndex", spnGenreIndex);
        intent.putExtra("spnBudgetIndex", spnBudgetIndex);

        startActivity(intent);

    }

    private boolean checkInputsValidity() {

        //「地域を指定」を選択した場合の座標入力チェック
        if(spnLocation.getSelectedItemPosition() == 1){

            String latitudeText = edtTxtLatitude.getText().toString();
            TextView txtLatitudeError = findViewById(R.id.txtLatitudeError);
            List<Character> allowedCoordinatesCharacters = List.of('0','1','2','3','4','5','6','7','8','9','.');

            //緯度　未入力チェック
            if(latitudeText.isEmpty()){
                txtLatitudeError.setText(getString(R.string.text_missing_alert));
                txtLatitudeError.setVisibility(View.VISIBLE);
                return false;
            }

            //緯度　値チェック
            for (int i = 0; i < latitudeText.length(); i++) {
                if(!allowedCoordinatesCharacters.contains(latitudeText.charAt(i))){
                    txtLatitudeError.setText(getString(R.string.text_incorrect_character_alert, latitudeText.charAt(i)));
                    txtLatitudeError.setVisibility(View.VISIBLE);
                    return false;
                }
            }

            //全ての緯度チェックが大丈夫だったらエラーメッセージを非表示
            txtLatitudeError.setVisibility(View.GONE);

            String longitudeText = edtTxtLongitude.getText().toString();
            TextView txtLongitudeError = findViewById(R.id.txtLongitudeError);

            //経度　未入力チェック
            if(longitudeText.isEmpty()){
                txtLongitudeError.setText(getString(R.string.text_missing_alert));
                txtLongitudeError.setVisibility(View.VISIBLE);
                return false;
            }

            //経度　値チェック
            for (int i = 0; i < longitudeText.length(); i++) {
                if(!allowedCoordinatesCharacters.contains(longitudeText.charAt(i))){
                    txtLongitudeError.setText(getString(R.string.text_incorrect_character_alert, longitudeText.charAt(i)));
                    txtLongitudeError.setVisibility(View.VISIBLE);
                    return false;
                }
            }

            //全ての経度チェックが大丈夫だったらエラーメッセージを非表示
            txtLongitudeError.setVisibility(View.GONE);

        }

        return true;

    }

    private void trimAllTextInputs() {
        edtTxtLatitude.setText(edtTxtLatitude.getText().toString().trim());
        edtTxtLongitude.setText(edtTxtLongitude.getText().toString().trim());
        edtTxtRestaurantName.setText(edtTxtRestaurantName.getText().toString().trim());
    }
}