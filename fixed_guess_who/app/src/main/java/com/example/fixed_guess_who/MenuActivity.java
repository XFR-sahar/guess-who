package com.example.fixed_guess_who;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private Button btnStartGame, shop;
    private TextView tvMoney, tvName, tvStreak;
    private Spinner spinnerSettings;
    private AlertDialog searchingDialog;
    private AlertDialog waitingDialog;
    private AlertDialog onlineCategoryDialog;
    private MultiplayerManager mpManager;

    private int lastMoney = -1;
    private boolean shouldAnimateMoney = false;
    private static final int DEFAULT_MONEY_COLOR = Color.parseColor("#E2BC97");
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private List<String> unlockedCategories = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu);

        Intent intent = getIntent();
        shouldAnimateMoney = intent.getBooleanExtra("FROM_GAME", false);

        if (shouldAnimateMoney) {
            lastMoney = intent.getIntExtra("OLD_MONEY", -1);
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        mUserRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        btnStartGame = findViewById(R.id.btnStartGame);
        shop = findViewById(R.id.shop);
        tvMoney = findViewById(R.id.tvMoney);
        tvName = findViewById(R.id.tvName);
        tvStreak = findViewById(R.id.tvStreak);
        spinnerSettings = findViewById(R.id.spinnerSettings);


        // הגדרת סטטוס אונליין
        mUserRef.child("isOnline").setValue(true);
        mUserRef.child("isOnline").onDisconnect().setValue(false);

        setupSettingsSpinner();
        listenToUserData();
        
        btnStartGame.setOnClickListener(v -> {
            MusicManager.sound_login(this);
            showCategoryDialog();
        });

        shop.setOnClickListener(v -> {
            MusicManager.sound_login(this);
            startActivity(new Intent(this, ShopSelectionActivity.class));
        });
    }
    

    private void showSearchingDialog(String category) {
        if (searchingDialog != null && searchingDialog.isShowing()) searchingDialog.dismiss();
        searchingDialog = new AlertDialog.Builder(this)
                .setTitle("מחפש יריב...")
                .setMessage("אנא המתן בזמן שאנחנו מוצאים לך יריב ראוי קטגוריית " + category + "...")
                .setNegativeButton("ביטול", (dialog, which) -> {
                    if (mpManager != null) mpManager.leaveRoom(category);
                    cleanupOnlineSession();
                })
                .setCancelable(false)
                .create();

        if (searchingDialog.getWindow() != null) {
            searchingDialog.getWindow().setBackgroundDrawableResource(R.drawable.board_background);
        }
        searchingDialog.show();

        mpManager = new MultiplayerManager(mAuth.getUid(), new MultiplayerManager.MultiplayerCallback() {
            @Override
            public void onMatchFound(String roomId, boolean isPlayer1) {
                if (searchingDialog != null) searchingDialog.dismiss();
                vibrate(500); // Vibrate on match found
                if (isPlayer1) {
                    DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);
                    roomRef.child("category").setValue(category);
                    startOnlineGame(roomId, category, true);
                } else {
                    startOnlineGame(roomId, category, false);
                }
            }

            @Override
            public void onGameStateChanged(DataSnapshot snapshot) {
                // Not needed for initial flow since we pass category now
            }

            @Override
            public void onOpponentDisconnected() {
                runOnUiThread(() -> {
                    Toast.makeText(MenuActivity.this, "היריב התנתק", Toast.LENGTH_SHORT).show();
                    cleanupOnlineSession();
                });
            }
        });

        mpManager.seekGame(category);
    }

    private void startOnlineGame(String roomId, String category, boolean isPlayer1) {
        cleanupOnlineSession();
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("CATEGORY", category);
        intent.putExtra("IS_ONLINE", true);
        intent.putExtra("ROOM_ID", roomId);
        intent.putExtra("IS_PLAYER_1", isPlayer1);
        startActivity(intent);
    }

    private void cleanupOnlineSession() {
        if (searchingDialog != null && searchingDialog.isShowing()) {
            searchingDialog.dismiss();
        }
        if (waitingDialog != null && waitingDialog.isShowing()) {
            waitingDialog.dismiss();
        }
        if (onlineCategoryDialog != null && onlineCategoryDialog.isShowing()) {
            onlineCategoryDialog.dismiss();
        }
        mpManager = null;
    }

    private void listenToUserData() {
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("username").getValue(String.class);
                    if (name != null) {
                        tvName.setText("name: " + name);
                    }
                    Integer money = snapshot.child("money").getValue(Integer.class);
                    if (money != null) {
                        MoneyManager.getInstance().setMoney(money);
                        updateMoneyDisplay(money);
                    }
                    
                    Integer winStreak = snapshot.child("winStreak").getValue(Integer.class);
                    if (winStreak != null) {
                        MoneyManager.getInstance().setWinStreak(winStreak);
                        if (winStreak > 0) {
                            tvStreak.setVisibility(View.VISIBLE);
                            tvStreak.setText("streak: " + winStreak + " 🔥");
                        } else {
                            tvStreak.setVisibility(View.GONE);
                        }
                    }

                    unlockedCategories.clear();
                    DataSnapshot catsSnapshot = snapshot.child("categories");
                    for (DataSnapshot postSnapshot : catsSnapshot.getChildren()) {
                        String cat = postSnapshot.getValue(String.class);
                        if (cat != null) unlockedCategories.add(cat);
                    }

                    // סנכרון הגדרות
                    Boolean musicOn = snapshot.child("musicOn").getValue(Boolean.class);
                    Boolean sfxOn = snapshot.child("sfxOn").getValue(Boolean.class);
                    android.content.SharedPreferences.Editor editor = getSharedPreferences("GameSettings", MODE_PRIVATE).edit();
                    if (musicOn != null) {
                        editor.putBoolean("music_on", musicOn);
                        if (musicOn) MusicManager.playBackgroundMusic(MenuActivity.this);
                        else MusicManager.pauseBackgroundMusic();
                    }
                    if (sfxOn != null) {
                        editor.putBoolean("sfx_on", sfxOn);
                    }
                    editor.apply();
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateMoneyDisplay(int currentMoney) {
        if (shouldAnimateMoney && lastMoney != -1 && currentMoney != lastMoney) {
            animateMoneyChange(lastMoney, currentMoney);
            shouldAnimateMoney = false;
        } else {
            tvMoney.setText("money: " + currentMoney + "💰");
            tvMoney.setTextColor(DEFAULT_MONEY_COLOR);
        }
        lastMoney = currentMoney;
    }

    private void animateMoneyChange(int from, int to) {
        int color = to > from ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828");
        tvMoney.setTextColor(color);

        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(1000);
        animator.addUpdateListener(a -> tvMoney.setText("money: " + a.getAnimatedValue() + "💰"));
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                tvMoney.setTextColor(DEFAULT_MONEY_COLOR);
            }
        });
        animator.start();
    }

    private void setupSettingsSpinner() {
        spinnerSettings.setPopupBackgroundResource(R.drawable.board_background);
        final String[] options = {"", "   settings", "   instructions", "   logout"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setText("");
                return v;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                if (position == 0) {
                    v.setVisibility(View.GONE);
                    v.setLayoutParams(new android.widget.AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    return v;
                }
                TextView tv = (TextView) v;
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                tv.setPadding(0, 30, 0, 30);
                tv.setTextColor(Color.BLACK);
                tv.setTextSize(18);
                return v;
            }
        };
        spinnerSettings.setAdapter(adapter);
        spinnerSettings.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return;
                String selected = options[position];
                if ("   settings".equals(selected) || "   instructions".equals(selected)) {
                    Intent intent = new Intent(MenuActivity.this, InfoActivity.class);
                    intent.putExtra("START_PAGE", selected);
                    startActivity(intent);
                    MusicManager.sound_login(MenuActivity.this);
                } else if ("   logout".equals(selected)) {
                    MusicManager.sound_login(MenuActivity.this);
                    showLogoutDialog();
                }
                parent.setSelection(0);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("יציאה")
                .setMessage("האם אתה בטוח שברצונך להתנתק?")
                .setPositiveButton("כן", (d, w) -> {
                    if (mUserRef != null) {
                        mUserRef.child("isOnline").setValue(false).addOnCompleteListener(task -> {
                            performFinalLogout();
                        });
                    } else {
                        performFinalLogout();
                    }
                })
                .setNegativeButton("לא", null)
                .show();
    }

    private void performFinalLogout() {
        mAuth.signOut();
        
        // Also sign out from Google if configured
        com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = 
            new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();
        com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso).signOut();

        UnlockManager.resetInstance();
        MoneyManager.resetInstance();

        getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("rememberMe", false)
                .apply();

        Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String selectedCategory = ""; // משתנה גלובלי או מקומי לשמירת הבחירה

    private void showCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_category, null);

        LinearLayout cardRegular = view.findViewById(R.id.cardRegular);
        LinearLayout cardMarvel = view.findViewById(R.id.cardMarvel);
        LinearLayout cardAnime = view.findViewById(R.id.cardAnime);

        // בדיקת קטגוריות פתוחות
        if (unlockedCategories.contains("marvel")) cardMarvel.setVisibility(View.VISIBLE);
        if (unlockedCategories.contains("anime")) cardAnime.setVisibility(View.VISIBLE);

        // לוגיקת בחירה
        View.OnClickListener cardClickListener = v -> {
            // איפוס כל הרקעים (נחזיר לרקע הרגיל שלך)
            cardRegular.setBackgroundResource(R.drawable.button_frame);
            cardMarvel.setBackgroundResource(R.drawable.button_frame);
            cardAnime.setBackgroundResource(R.drawable.button_frame);

            // סימון הקלף שנבחר (נשתמש ב-Tint או ברקע אחר כדי להבליט)
            v.setBackgroundResource(R.drawable.radio_button_frame); // כאן כדאי להשתמש ברקע שמראה בחירה

            if (v.getId() == R.id.cardRegular) selectedCategory = "רגיל";
            else if (v.getId() == R.id.cardMarvel) selectedCategory = "מארוול";
            else if (v.getId() == R.id.cardAnime) selectedCategory = "אנימה";

            MusicManager.sound_login(MenuActivity.this);
        };

        cardRegular.setOnClickListener(cardClickListener);
        cardMarvel.setOnClickListener(cardClickListener);
        cardAnime.setOnClickListener(cardClickListener);

        builder.setView(view);
        builder.setPositiveButton("הבא", (dialogInterface, which) -> {
            if (!selectedCategory.isEmpty()) {
                MusicManager.sound_login(MenuActivity.this);
                showModeSelectionDialog(selectedCategory);
            }
        });

        builder.setNegativeButton("ביטול", null);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.board_background);

        dialog.show();
        styleDialogButtons(dialog);
    }

    private void showModeSelectionDialog(String category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_game_mode, null);

        LinearLayout cardVSComputer = view.findViewById(R.id.cardVSComputer);
        LinearLayout cardOnline = view.findViewById(R.id.cardOnline);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        // לחיצה על נגד מחשב
        cardVSComputer.setOnClickListener(v -> {
            MusicManager.sound_login(this);
            dialog.dismiss();
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("CATEGORY", category);
            startActivity(intent);
        });

        // לחיצה על אונליין
        cardOnline.setOnClickListener(v -> {
            MusicManager.sound_login(this);
            dialog.dismiss();
            showSearchingDialog(category);
        });

        builder.setNegativeButton("חזור", (dialogInterface, which) -> {
            showCategoryDialog();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.board_background);
        }

        dialog.show();
        styleDialogButtons(dialog);
    }

    private void styleDialogButtons(AlertDialog dialog) {
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(Color.BLACK);
            positiveButton.setTextSize(22);
            positiveButton.setTypeface(null, Typeface.BOLD);
        }
        if (negativeButton != null) {
            negativeButton.setTextColor(Color.BLACK);
            negativeButton.setTextSize(22);
            negativeButton.setTypeface(null, Typeface.BOLD);
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

    private void vibrate(long duration) {
        android.os.Vibrator v = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (v != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                v.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(duration);
            }
        }
    }
 
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}