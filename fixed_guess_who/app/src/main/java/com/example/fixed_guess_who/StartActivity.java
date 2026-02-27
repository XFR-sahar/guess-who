package com.example.fixed_guess_who;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    ImageView questionMark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // מסך מלא
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        MusicManager.initSounds(this);

        questionMark = findViewById(R.id.question_mark);

        ObjectAnimator rotate = ObjectAnimator.ofFloat(questionMark, "rotation", -20f, 20f);
        rotate.setDuration(800);
        rotate.setRepeatCount(ValueAnimator.INFINITE);
        rotate.setRepeatMode(ValueAnimator.REVERSE);

        AnimatorSet set = new AnimatorSet();
        set.play(rotate);
        set.start();

        countDownTimer.start();
    }

    CountDownTimer countDownTimer = new CountDownTimer(3000, 3000) {
        @Override
        public void onTick(long l) { }

        @Override
        public void onFinish() {
            com.google.firebase.auth.FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
            com.google.firebase.auth.FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser != null) {
                String uid = currentUser.getUid();
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference()
                    .child("users").child(uid).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Boolean isOnline = snapshot.child("isOnline").getValue(Boolean.class);
                                if (isOnline != null && isOnline) {
                                    mAuth.signOut();
                                    goToLogin("משתמש זה כבר מחובר במכשיר אחר");
                                } else {
                                    // Set online and proceed
                                    snapshot.getRef().child("isOnline").setValue(true);
                                    snapshot.getRef().child("isOnline").onDisconnect().setValue(false);
                                    startActivity(new Intent(StartActivity.this, MenuActivity.class));
                                    overridePendingTransition(R.anim.fade, R.anim.scale);
                                    finish();
                                }
                            } else {
                                goToLogin(null);
                            }
                        }

                        @Override
                        public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                            goToLogin(null);
                        }
                    });
            } else {
                goToLogin(null);
            }
        }

        private void goToLogin(String error) {
            Intent intent = new Intent(StartActivity.this, LoginActivity.class);
            if (error != null) intent.putExtra("LOGIN_ERROR", error);
            startActivity(intent);
            overridePendingTransition(R.anim.fade, R.anim.scale);
            finish();
        }
    };
}
