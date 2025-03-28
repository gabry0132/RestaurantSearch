package com.example.restaurantsearch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText edtTxtRestaurantName;

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

        edtTxtRestaurantName = findViewById(R.id.edtTxtRestaurantName);
    }

    public void openWebPage(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://webservice.recruit.co.jp/"));
        startActivity(browserIntent);
    }

    public void invokeNextPageAndSearch(View view){

        String restaurantName = edtTxtRestaurantName.getText().toString().trim();

        //TODO: check for inputs, add new TextView to inform the user when the input is necessary or incorrect and display that TextView whenever there is a problem with the inputs

        Intent intent = new Intent(this, RestaurantsListActivity.class);
        intent.putExtra("restaurantName", restaurantName);
        startActivity(intent);

    }
}