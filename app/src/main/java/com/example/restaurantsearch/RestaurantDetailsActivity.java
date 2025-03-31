package com.example.restaurantsearch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class RestaurantDetailsActivity extends AppCompatActivity {

    private Shop shop;

    //UIの変数
    private TextView txtShopName, txtOpeningHours, txtGenre, txtEstimatedPrice, txtAddress, txtAccess, txtHasParking,
            txtCardAvailability, txtSmokingSeats, txtIsBarrierFree, txtHasLunch, txtHasTatami, txtAcceptsPets, txtHasKaraoke;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        //Serializableで送信されたShop型オブジェクトを作り直します。（バイト -> Shop型）
        shop = (Shop) intent.getSerializableExtra("shop");

        //UI変数の代入
        initializeUIComponents();

        setPageValues();
    }


    private void initializeUIComponents() {
        txtShopName = findViewById(R.id.txtShopName);
        txtOpeningHours = findViewById(R.id.txtOpeningHours);
        txtGenre = findViewById(R.id.txtGenre);
        txtEstimatedPrice = findViewById(R.id.txtEstimatedPrice);
        txtAddress = findViewById(R.id.txtAddress);
        txtAccess = findViewById(R.id.txtAccess);
        txtHasParking = findViewById(R.id.txtHasParking);
        txtCardAvailability = findViewById(R.id.txtCardAvailability);
        txtSmokingSeats = findViewById(R.id.txtSmokingSeats);
        txtIsBarrierFree = findViewById(R.id.txtIsBarrierFree);
        txtHasLunch = findViewById(R.id.txtHasLunch);
        txtHasTatami = findViewById(R.id.txtHasTatami);
        txtAcceptsPets = findViewById(R.id.txtAcceptsPets);
        txtHasKaraoke = findViewById(R.id.txtHasKaraoke);
        image = findViewById(R.id.image);
    }

    private void setPageValues() {
        txtShopName.setText(shop.getName());
        txtOpeningHours.setText(shop.getOpeningHours());
        //ジャンルがない時が多いですが、レイアウトが壊れないように対応します。
        if(shop.getGenre().isEmpty()){
            txtGenre.setVisibility(TextView.GONE);
        } else {
            txtGenre.setText(shop.getGenre());
        }
        txtEstimatedPrice.setText(shop.getEstimatedPrice());
        txtAddress.setText(shop.getAddress());
        txtAccess.setText(shop.getAccess());
        txtHasParking.setText(shop.getHasParking());
        txtCardAvailability.setText(shop.getCardAvailability());
        txtSmokingSeats.setText(shop.getSmokingSeats());
        txtIsBarrierFree.setText(shop.getIsBarrierFree());
        txtHasLunch.setText(shop.getHasLunch());
        txtHasTatami.setText(shop.getHasTatami());
        txtAcceptsPets.setText(shop.getAcceptsPets());
        txtHasKaraoke.setText(shop.getHasKaraoke());
        Glide.with(this)
                .asBitmap()
                .load(shop.getImageUrl())
                .into(image);
    }

    public void openMapsApp(View view){
        //最低限のパラメータチェック
        if(shop.getLatitude().isEmpty() || shop.getLongitude().isEmpty() || shop.getName().isEmpty()){
            Toast.makeText(this, "座標の取得処理が失敗しました。", Toast.LENGTH_SHORT).show();
            return;
        }
        MapsOpener.openMapsApp(this,Double.parseDouble(shop.getLatitude()),Double.parseDouble(shop.getLongitude()),shop.getName());
    }

    public void openWebPage(View view){
        //最低限のパラメータチェック
        if(shop.getWebsiteUrl().isEmpty()){
            Toast.makeText(this, "お店のウエブサイトが見つかりませんでした。", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(shop.getWebsiteUrl()));
        startActivity(browserIntent);
    }


}