package com.example.fixed_guess_who;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class UnlockManager {

    private static UnlockManager instance;
    private DatabaseReference mUserRef;
    private final List<String> unlockedCategories = new ArrayList<>();
    private boolean dataLoaded = false;

    // ממשק להאזנה לטעינת נתוני הקטגוריות
    public interface OnUnlockDataLoadedListener {
        void onDataLoaded();
    }

    private OnUnlockDataLoadedListener dataLoadedListener;

    /**
     * קונסטרקטור פרטי - יוצר את ה-instance ומתחבר ל-Firebase
     * מאזין לשינויים בקטגוריות שנפתחו למשתמש הנוכחי
     */
    private UnlockManager() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        mUserRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        listenToCategories(); // מתחיל להאזין לשינויים בקטגוריות
    }

    /**
     * מאזין לשינויים בזמן אמת בקטגוריות המשתמש
     * מעדכן את הרשימה unlockedCategories ומודיע למאזין במידת הצורך
     */
    private void listenToCategories() {
        mUserRef.child("categories").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        unlockedCategories.clear();

                        for (DataSnapshot s : snapshot.getChildren()) {
                            String category = s.getValue(String.class);
                            if (category != null) {
                                unlockedCategories.add(category);
                            }
                        }

                        dataLoaded = true;
                        if (dataLoadedListener != null) {
                            dataLoadedListener.onDataLoaded();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // במקרה של שגיאה, כרגע לא עושים כלום
                    }
                }
        );
    }

    /**
     * מחזיר את ה-instance של UnlockManager (Singleton)
     */
    public static UnlockManager getInstance() {
        if (instance == null) {
            instance = new UnlockManager();
        }
        return instance;
    }

    /**
     * מגדיר מאזין שיקבל עדכון כאשר הנתונים נטענו
     */
    public void setOnDataLoadedListener(OnUnlockDataLoadedListener listener) {
        this.dataLoadedListener = listener;
        // אם הנתונים כבר נטענו – קורא מיד למאזין
        if (dataLoaded && listener != null) {
            listener.onDataLoaded();
        }
    }


    public boolean isMarvelUnlocked() {
        return unlockedCategories.contains("marvel");
    }


    public boolean isAnimeUnlocked() {
        return unlockedCategories.contains("anime");
    }


    public void unlockMarvel() {
        if (!isMarvelUnlocked()) {
            unlockedCategories.add("marvel");
            mUserRef.child("categories").setValue(unlockedCategories);
        }
    }

    public void unlockAnime() {
        if (!isAnimeUnlocked()) {
            unlockedCategories.add("anime");
            mUserRef.child("categories").setValue(unlockedCategories);
        }
    }

    public static void resetInstance() {
        instance = null;
    }
}
