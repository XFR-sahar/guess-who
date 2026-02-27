package com.example.fixed_guess_who;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;

import androidx.appcompat.app.AlertDialog;

public class GameUIManager {

    private final Activity activity;

    public GameUIManager(Activity activity) {
        this.activity = activity;
    }

    public enum GameEndReason {
        YOU_GUESSED_RIGHT,
        YOU_GUESSED_WRONG,
        OPPONENT_GUESSED_RIGHT,
        OPPONENT_GUESSED_WRONG,
        OPPONENT_DISCONNECTED,
        OPPONENT_LEFT_GAME,
        YOU_LEFT_GAME;

        public boolean isWin() {
            return this == YOU_GUESSED_RIGHT ||
                   this == OPPONENT_GUESSED_WRONG ||
                   this == OPPONENT_DISCONNECTED ||
                   this == OPPONENT_LEFT_GAME;
        }
    }

    public void handleGameEnd(
            GameEndReason reason,
            String characterName,
            View overlayView,
            TextView tvTitle,
            TextView tvCharInfo,
            View buttonContainer,
            OnGameEndListener listener) {

        String tempTitle = "המשחק נגמר";
        String tempDesc = "";
        boolean tempWon = false;

        if (reason == GameEndReason.YOU_GUESSED_RIGHT) {
            tempTitle = "ניחשת נכון!";
            tempDesc = "הדמות הייתה: " + characterName;
            tempWon = true;
        } else if (reason == GameEndReason.YOU_GUESSED_WRONG) {
            tempTitle = "ניחשת לא נכון";
            tempDesc = "הדמות הייתה: " + characterName;
            tempWon = false;
        } else if (reason == GameEndReason.OPPONENT_GUESSED_RIGHT) {
            tempTitle = "היריב ניחש נכון";
            tempDesc = "הוא ניחש שהדמות שלך היא " + characterName;
            tempWon = false;
        } else if (reason == GameEndReason.OPPONENT_GUESSED_WRONG) {
            tempTitle = "היריב ניחש לא נכון";
            tempDesc = "הוא ניחש שהדמות שלך היא " + characterName;
            tempWon = true;
        } else if (reason == GameEndReason.OPPONENT_DISCONNECTED) {
            tempTitle = "היריב התנתק";
            tempDesc = "ניצחת בניצחון טכני!";
            tempWon = true;
        }
        else if (reason == GameEndReason.OPPONENT_LEFT_GAME) {
            tempTitle = "היריב פרש";
            tempDesc = "ניצחת בניצחון טכני!";
            tempWon = true;
        }
        else if (reason == GameEndReason.YOU_LEFT_GAME) {
            tempTitle = "פרשת ימוזר אפס";
            tempDesc = "הפסדת יחלש";
            tempWon = false;
        }

        final String finalTitle = tempTitle;
        final boolean finalWon = tempWon;

        tvTitle.setText(tempTitle);
        tvCharInfo.setText(tempDesc);

        buttonContainer.setVisibility(View.GONE);
        overlayView.setVisibility(View.VISIBLE);
        overlayView.setAlpha(0f);
        overlayView.setScaleX(0.7f);
        overlayView.setScaleY(0.7f);

        overlayView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .withEndAction(() ->
                        overlayView.animate()
                                .setStartDelay(1300)
                                .alpha(0f)
                                .setDuration(400)
                                .withEndAction(() -> {
                                    overlayView.setVisibility(View.GONE);
                                    processMoneyAndShowDialog(finalWon, finalTitle, listener);
                                })
                                .start()
                )
                .start();
    }

    private void processMoneyAndShowDialog(boolean won, String title, OnGameEndListener listener) {
        int oldMoney = MoneyManager.getInstance().getMoney();
        int oldStreak = MoneyManager.getInstance().getWinStreak();
        
        if (won) {
            MoneyManager.getInstance().handleWin();
        } else {
            MoneyManager.getInstance().handleLoss();
        }

        int newMoney = MoneyManager.getInstance().getMoney();
        int moneyChange = newMoney - oldMoney;
        int newStreak = MoneyManager.getInstance().getWinStreak();

        String streakInfo = "";
        if (won && newStreak >= 3) {
            streakInfo = "\n🔥 רצף ניצחונות: " + newStreak + " (+5 בונוס!)";
        } else if (won) {
            streakInfo = "\n🔥 רצף ניצחונות: " + newStreak;
        }

        showFinalDialog(won, title + streakInfo, moneyChange, oldMoney, listener);
    }

    private void showFinalDialog(
            boolean won,
            String title,
            int moneyChange,
            int oldMoney,
            OnGameEndListener listener) {

        int layoutResId = won ? R.layout.dialog_victory : R.layout.dialog_lose;
        View view = activity.getLayoutInflater().inflate(layoutResId, null);

        TextView tvMoney = view.findViewById(R.id.tvMoney);
        TextView tvDialogTitle = view.findViewById(won ? R.id.tvVictoryTitle : R.id.tvLoseTitle);
        Button btnMenu = view.findViewById(R.id.btnBackToMenu);

        if (tvDialogTitle != null) {
            tvDialogTitle.setText(title);
        }

        if (tvMoney != null) {
            if (moneyChange > 0) {
                tvMoney.setText("הרווחת: +" + moneyChange + " 💰");
            } else if (moneyChange < 0) {
                tvMoney.setText("איבדת: -" + Math.abs(moneyChange) + " 💰");
            } else {
                tvMoney.setText("הכסף נשאר אותו דבר 💰");
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(view)
                .setCancelable(false)
                .create();

        dialog.setOnShowListener(d -> {
            vibrate(500);
            if (won) MusicManager.playVictory(activity);
            else MusicManager.playLose(activity);
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            dialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);
            dialog.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        btnMenu.setOnClickListener(v -> {
            if (moneyChange != 0) {
                MusicManager.playCoins(activity);
            }

            dialog.dismiss();
            if (listener != null) {
                listener.onBackToMenu(oldMoney);
            }
        });
    }

    public void showFinalResultDirectly(boolean won, OnGameEndListener listener) {
        processMoneyAndShowDialog(won, won ? "ניצחת!" : "היית קרוב...", listener);
    }

    private void vibrate(long duration) {
        Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(duration);
            }
        }
    }
 
    public interface OnGameEndListener {
        void onBackToMenu(int oldMoney);
    }
}
