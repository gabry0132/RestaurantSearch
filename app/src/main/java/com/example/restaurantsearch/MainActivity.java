package com.example.restaurantsearch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText edtTxtRestaurantName;
    private Spinner spnLocation, spnDistance;

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
        edtTxtRestaurantName = findViewById(R.id.edtTxtRestaurantName);
        spnLocation = findViewById(R.id.spnLocation);
        spnDistance = findViewById(R.id.spnDistance);

        //GPSの認可を求めます。実際に使うのは次のページですが、この時点で聞くとアプリケーション開いた瞬間に聞かれます。
        LocationPermissions.askLocationPermissions(this);
    }

    public void openWebPage(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://webservice.recruit.co.jp/"));
        startActivity(browserIntent);
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
        intent.putExtra("spnDistanceIndex", spnDistanceIndex);
        startActivity(intent);

    }
}