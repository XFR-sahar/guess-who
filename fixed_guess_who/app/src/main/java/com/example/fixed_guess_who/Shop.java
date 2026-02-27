package com.example.fixed_guess_who;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Shop extends AppCompatActivity {

    private TextView tvCoinsBottom;
    private Button btnFinish;
    
    // Category Shop views
    private Button btnBuyMarvel, btnBuyAnime;
    
    // Powerup Shop views
    private Button btnBuyHint, btnBuyPeek;
    private TextView tvHintCount, tvPeekCount;
    
    private DatabaseReference mUserRef;
    private String shopType;
    private Map<String, Integer> powerups = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        shopType = getIntent().getStringExtra("SHOP_TYPE");
        if (shopType == null) shopType = "categories";

        if (shopType.equals("powerups")) {
            setContentView(R.layout.activity_shop_powerups);
            btnBuyHint = findViewById(R.id.btnBuyHint);
            btnBuyPeek = findViewById(R.id.btnBuyPeek);
            tvHintCount = findViewById(R.id.tvHintCount);
            tvPeekCount = findViewById(R.id.tvPeekCount);
            
            btnBuyHint.setOnClickListener(v -> handlePowerupPurchase("hint", 10));
            btnBuyPeek.setOnClickListener(v -> handlePowerupPurchase("peek", 10));
        } else {
            setContentView(R.layout.activity_shop);
            btnBuyMarvel = findViewById(R.id.btnBuyMarvel);
            btnBuyAnime = findViewById(R.id.btnBuyAnime);
            
            btnBuyMarvel.setOnClickListener(v -> handleCategoryPurchase("marvel", 100));
            btnBuyAnime.setOnClickListener(v -> handleCategoryPurchase("anime", 5));
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        tvCoinsBottom = findViewById(R.id.tvCoinsBottom);
        btnFinish = findViewById(R.id.btnFinish);

        UnlockManager.getInstance().setOnDataLoadedListener(() -> {
            runOnUiThread(this::updateUI);
        });

        btnFinish.setOnClickListener(v -> {
            MusicManager.sound_login(this);
            finish();
        });
        listenToPowerups();
    }

    private void listenToPowerups() {
        mUserRef.child("powerups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                powerups.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    powerups.put(ds.getKey(), ds.getValue(Integer.class));
                }
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void handleCategoryPurchase(String category, int price) {
        boolean isUnlocked = category.equals("marvel") ?
                UnlockManager.getInstance().isMarvelUnlocked() :
                UnlockManager.getInstance().isAnimeUnlocked();

        if (isUnlocked) {
            Toast.makeText(this, "כבר נרכש", Toast.LENGTH_SHORT).show();
            return;
        }

        if (MoneyManager.getInstance().subtractMoney(price)) {
            if (category.equals("marvel")) {
                UnlockManager.getInstance().unlockMarvel();
            } else {
                UnlockManager.getInstance().unlockAnime();
            }

            updateUI();
            MusicManager.sound_login(this);
            vibrate(500);
            Toast.makeText(this, "רכשת את קטגוריית " + category + "!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "אין מספיק מטבעות!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePowerupPurchase(String type, int price) {
        if (MoneyManager.getInstance().subtractMoney(price)) {
            int current = powerups.getOrDefault(type, 0);
            mUserRef.child("powerups").child(type).setValue(current + 1);
            
            MusicManager.sound_login(this);
            vibrate(500);
            Toast.makeText(this, "רכשת Power-up: " + type + "!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "אין מספיק מטבעות!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        int coins = MoneyManager.getInstance().getMoney();
        tvCoinsBottom.setText("money: " + coins + " 💰");

        if (shopType.equals("categories")) {
            if (UnlockManager.getInstance().isMarvelUnlocked()) {
                btnBuyMarvel.setEnabled(false);
                btnBuyMarvel.setText("נרכש");
            } else {
                btnBuyMarvel.setEnabled(true);
                btnBuyMarvel.setText("מארוול");
            }

            if (UnlockManager.getInstance().isAnimeUnlocked()) {
                btnBuyAnime.setEnabled(false);
                btnBuyAnime.setText("נרכש");
            } else {
                btnBuyAnime.setEnabled(true);
                btnBuyAnime.setText("אנימה");
            }
        } else {
            tvHintCount.setText("יש לך: " + powerups.getOrDefault("hint", 0));
            tvPeekCount.setText("יש לך: " + powerups.getOrDefault("peek", 0));
        }
    }

    private void vibrate(long duration) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(duration);
            }
        }
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