package com.example.fixed_guess_who;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MoneyManager {

    private static MoneyManager instance;
    private int money;
    private int winStreak;
    private DatabaseReference mUserRef;

    private MoneyManager() {
        // ערך ברירת מחדל עד שהנתונים יגיעו מהענן
        money = 0;
        winStreak = 0;

        // הגדרת נתיב למשתמש ב-Firebase
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            mUserRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        }
    }

    public static MoneyManager getInstance() {
        if (instance == null) {
            instance = new MoneyManager();
        }
        return instance;
    }

    public int getMoney() {
        return money;
    }

    public int getWinStreak() {
        return winStreak;
    }

    // עדכון הכסף המקומי (נקרא מה-Listener ב-MenuActivity)
    public void setMoney(int amount) {
        this.money = amount;
    }

    public void setWinStreak(int streak) {
        this.winStreak = streak;
    }

    public void handleWin() {
        winStreak++;
        int bonus = (winStreak >= 3) ? 5 : 0; // בונוס של 5 מטבעות מרצף של 3 ומעלה
        addMoney(10 + bonus);
    }

    public void handleLoss() {
        winStreak = 0;
        subtractMoney(5);
    }

    // הוספת כסף וסנכרון לענן
    public void addMoney(int amount) {
        money += amount;
        updateFirebase();
    }

    public boolean subtractMoney(int amount) {
        if (money >= amount) {
            money -= amount;
            updateFirebase();
            return true;
        } else {
            return false;
        }
    }

    private void updateFirebase() {
        if (mUserRef != null) {
            mUserRef.child("money").setValue(money);
            mUserRef.child("winStreak").setValue(winStreak);
        }
    }

    public void usePowerup(String type) {
        if (mUserRef != null) {
            // We use a transaction or just a simple decrementation
            mUserRef.child("powerups").child(type).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    long current = (long) task.getResult().getValue();
                    if (current > 0) {
                        mUserRef.child("powerups").child(type).setValue(current - 1);
                    }
                }
            });
        }
    }

    public void setDisconnectPenalty(int amount) {
        if (mUserRef != null) {
            mUserRef.child("money").onDisconnect().setValue(money - amount);
        }
    }

    public void cancelDisconnectPenalty() {
        if (mUserRef != null) {
            mUserRef.child("money").onDisconnect().cancel();
        }
    }

    public static void resetInstance() {
        instance = null;
    }
}