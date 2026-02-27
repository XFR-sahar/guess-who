package com.example.fixed_guess_who;

import android.content.Context;

public class TurnManager {

    public interface TurnCallback {
        void onPlayerTimeOut();
        void onAIAsking(Question q, OnAnswerListener answerListener);
        void onAIGuess(GameCharacter character, boolean iscorrect);
        void onTurnEnd();
    }

    public interface OnAnswerListener {
        void onAnswer(boolean answer);
    }

    private final AIPlayer ai;
    private final GameCharacter playercharacter;
    private final TurnCallback callback;
    private final Context context;
    private int playerRemainingCountForAI = 24;

    public TurnManager(AIPlayer ai, GameCharacter playercharacter, TurnCallback callback, Context context) {
        this.ai = ai;
        this.playercharacter = playercharacter;
        this.callback = callback;
        this.context = context;
    }

    public void onPlayerTimeOut() {
        if (callback != null) {
            callback.onPlayerTimeOut();
        }
    }

    public void updatePlayerProgress(int count) {
        this.playerRemainingCountForAI = count;
    }

    public void startAITurn() {
        int aiRemainingCount = ai.getRemainingCharactersCount();
        // 1. בדיקה אסטרטגית האם כדאי לנחש ניחוש סופי עכשיו
        if (aiRemainingCount == 1 || ai.shouldTakeFinalRisk(playerRemainingCountForAI)) {
            performAIGuess();
            return;
        }
        // 2. בחירת שאלה חכמה מה-AI
        Question q = ai.chooseQuestion(playerRemainingCountForAI);
        if (q != null && callback != null) {
            callback.onAIAsking(q, answer -> {
                // ה-AI מעדכן את הרשימה שלו לפי התשובה
                ai.filterPossibleCharacters(q, answer);
                // סיום התור של ה-AI
                callback.onTurnEnd();
            });
        } else {
            performAIGuess();
        }
    }

    private void performAIGuess() {
        // השם המדויק של המתודה ב-AIPlayer הוא makeFinalGuess
        // אנחנו שולפים את הדמות שה-AI החליט לנחש
        GameCharacter guess = ai.makeFinalGuessObject();

        if (guess != null) {
            // השוואה בין השם של הדמות שניחש ה-AI לשם של הדמות של השחקן
            // שימוש ב-name ישירות וב-toLowerCase כדי לעמוד בדרישות שלך
            boolean iscorrect = guess.name.toLowerCase().equalsIgnoreCase(playercharacter.name.toLowerCase());

            if (iscorrect) {
                ai.recordAIWin(context);
            } else {
                ai.recordPlayerWin(context);
            }

            if (callback != null) {
                callback.onAIGuess(guess, iscorrect);
            }
        }
    }

}