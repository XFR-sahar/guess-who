package com.example.fixed_guess_who;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ShopSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_shop_selection);

        LinearLayout cardCategories = findViewById(R.id.cardCategories);
        LinearLayout cardPowerups = findViewById(R.id.cardPowerups);
        Button btnBack = findViewById(R.id.btnBack);

        cardCategories.setOnClickListener(v -> {
            MusicManager.sound_login(this);
            Intent intent = new Intent(this, Shop.class);
            intent.putExtra("SHOP_TYPE", "categories");
            startActivity(intent);
        });

        cardPowerups.setOnClickListener(v -> {
            MusicManager.sound_login(this);
            Intent intent = new Intent(this, Shop.class);
            intent.putExtra("SHOP_TYPE", "powerups");
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> {
            MusicManager.sound_login(this);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        MusicManager.onActivityStarted(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MusicManager.onActivityStopped();
    }
}
