package com.example.fixed_guess_who;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;

public class InfoActivity extends AppCompatActivity {

    private FrameLayout container;
    private LinearLayout layoutSettings, layoutInstructions;
    private ImageButton imgSettings, imgInstructions;
    private TextView txtSettings, txtInstructions;


    private final int COLOR_ACTIVE = Color.WHITE;
    private final int COLOR_INACTIVE = Color.parseColor("#8C7618");
    private final int BG_ACTIVE = Color.parseColor("#6B552D");
    private final int BG_INACTIVE = Color.TRANSPARENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        initViews();

        findViewById(R.id.btn_back_custom).setOnClickListener(v -> finish());

        layoutSettings.setOnClickListener(v -> {
            showSettings();
            updateUI(false);
        });

        layoutInstructions.setOnClickListener(v -> {
            showInstructions();
            updateUI(true);
        });

        // בדיקה מאיזה מסך הגענו כדי להציג את התוכן המתאים
        String startPage = getIntent().getStringExtra("START_PAGE");
        if ("settings".equals(startPage)) {
            showSettings();
            updateUI(false);
        } else {
            showInstructions();
            updateUI(true);
        }
    }

    private void initViews() {
        container = findViewById(R.id.content_container);
        layoutSettings = findViewById(R.id.layout_nav_settings);
        layoutInstructions = findViewById(R.id.layout_nav_instructions);
        imgSettings = findViewById(R.id.img_settings);
        imgInstructions = findViewById(R.id.img_instructions);
        txtSettings = findViewById(R.id.txt_settings);
        txtInstructions = findViewById(R.id.txt_instructions);
    }


    private void updateUI(boolean isInstructionsActive) {
        if (isInstructionsActive) {
            setGroupColor(layoutInstructions, imgInstructions, txtInstructions, COLOR_ACTIVE, BG_ACTIVE);
            setGroupColor(layoutSettings, imgSettings, txtSettings, COLOR_INACTIVE, BG_INACTIVE);
        } else {
            setGroupColor(layoutInstructions, imgInstructions, txtInstructions, COLOR_INACTIVE, BG_INACTIVE);
            setGroupColor(layoutSettings, imgSettings, txtSettings, COLOR_ACTIVE, BG_ACTIVE);
        }
    }

    private void setGroupColor(LinearLayout layout, ImageButton img, TextView txt, int color, int bgColor) {
        layout.setBackgroundColor(bgColor);
        txt.setTextColor(color);
        img.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private void showInstructions() {
        container.removeAllViews();
        getLayoutInflater().inflate(R.layout.view_instructions, container);
    }

    private void showSettings() {
        container.removeAllViews();
        getLayoutInflater().inflate(R.layout.view_settings, container);

        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);

        setupCustomSwitch(R.id.switch_music, "music_on", prefs, isChecked -> {
            if (isChecked) {
                MusicManager.playBackgroundMusic(this);
            } else {
                MusicManager.pauseBackgroundMusic();
            }
            saveSettingToFirebase("musicOn", isChecked);
        });
        setupCustomSwitch(R.id.switch_sfx, "sfx_on", prefs, isChecked -> {
            saveSettingToFirebase("sfxOn", isChecked);
        });



        Button btnReset = findViewById(R.id.btn_reset_settings);
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("איפוס הגדרות")
                        .setMessage("האם אתה בטוח שברצונך לאפס את כל ההגדרות?")
                        .setPositiveButton("כן", (dialog, which) -> resetSettings())
                        .setNegativeButton("לא", null)
                        .show();
            });
        }

    }

    private void setupCustomSwitch(int viewId, String prefKey, SharedPreferences prefs, OnSwitchChangedListener listener) {
        androidx.appcompat.widget.SwitchCompat sw = findViewById(viewId);
        if (sw == null) return;

        boolean defaultValue = !prefKey.equals("music_on");
        sw.setChecked(prefs.getBoolean(prefKey, defaultValue));
        updateSwitchColor(sw);

        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(prefKey, isChecked).apply();
            updateSwitchColor(sw);
            vibrate(500);
            if (listener != null) {
                listener.onChanged(isChecked);
            }
        });
    }

    private void saveSettingToFirebase(String key, boolean value) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            userRef.child(key).setValue(value);
        }
    }

    private void updateSwitchColor(androidx.appcompat.widget.SwitchCompat sw) {
        int thumbColor = sw.isChecked() ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336");
        int trackColor = sw.isChecked() ? Color.parseColor("#A5D6A7") : Color.parseColor("#EF9A9A");

        sw.getThumbDrawable().setColorFilter(thumbColor, PorterDuff.Mode.SRC_IN);
        sw.getTrackDrawable().setColorFilter(trackColor, PorterDuff.Mode.SRC_IN);
    }

    private void resetSettings() {
        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);
        prefs.edit().clear().apply();
        MusicManager.pauseBackgroundMusic();
        androidx.appcompat.widget.SwitchCompat switchMusic = findViewById(R.id.switch_music);
        androidx.appcompat.widget.SwitchCompat switchSfx = findViewById(R.id.switch_sfx);


        if (switchMusic != null) {
            switchMusic.setChecked(false);
            updateSwitchColor(switchMusic);
        }
        if (switchSfx != null) {
            switchSfx.setChecked(false);
            updateSwitchColor(switchSfx);
        }

        saveSettingToFirebase("musicOn", false);
        saveSettingToFirebase("sfxOn", false);
 
        Toast.makeText(this, "ההגדרות אופסו", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                int[] outLocation = new int[2];
                v.getLocationOnScreen(outLocation);
                float x = event.getRawX();
                float y = event.getRawY();
                if (x < outLocation[0] || x > outLocation[0] + v.getWidth() || y < outLocation[1] || y > outLocation[1] + v.getHeight()) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // מעלה את המונה ומנגן מוזיקה
        MusicManager.onActivityStarted(this);
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
    protected void onStop() {
        super.onStop();
        // מוריד את המונה (ועוצר רק אם אין מסכים מורשים אחרים)
        MusicManager.onActivityStopped();
    }

    interface OnSwitchChangedListener {
        void onChanged(boolean isChecked);
    }
}