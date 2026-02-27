package com.example.fixed_guess_who;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIPlayer {

    private List<GameCharacter> possiblecharacters;
    private List<Question> remainingquestions;
    private Random random = new Random();

    // נתוני למידה
    private int playerWinsCount;
    private int aiWinsCount;
    private float aggressiveness;

    private static final String PREFS_NAME = "ai_memory";
    private static final String KEY_PLAYER_WINS = "player_wins";
    private static final String KEY_AI_WINS = "ai_wins";

    public AIPlayer(List<GameCharacter> characters, List<Question> questions, Context context) {
        this.possiblecharacters = new ArrayList<>(characters);
        this.remainingquestions = new ArrayList<>(questions);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.playerWinsCount = prefs.getInt(KEY_PLAYER_WINS, 0);
        this.aiWinsCount = prefs.getInt(KEY_AI_WINS, 0);

        calculateAggressiveness();
    }

    private void calculateAggressiveness() {
        if (playerWinsCount == 0 && aiWinsCount == 0) {
            this.aggressiveness = 0.3f;
        }
        else {
            int totalGames = playerWinsCount + aiWinsCount + 1;
            this.aggressiveness = (float) playerWinsCount / totalGames;

            if (this.aggressiveness < 0.2f) {
                this.aggressiveness = 0.2f;
            }
            if (this.aggressiveness > 0.8f) {
                this.aggressiveness = 0.8f;
            }
        }
    }

    public Question chooseQuestion(int playerRemainingChars) {

        if (remainingquestions.isEmpty() || possiblecharacters.size() <= 1) {
            return null;
        }

        if (shouldTakeFinalRisk(playerRemainingChars)) {
            return null;
        }

        Question bestQuestion = null;
        double bestSplitScore = 999999.0;

        for (int i = 0; i < remainingquestions.size(); i++) {
            Question q = remainingquestions.get(i);
            int countYes = 0;

            for (int j = 0; j < possiblecharacters.size(); j++) {
                GameCharacter c = possiblecharacters.get(j);
                if (c.hasAttribute(q.key)) {
                    countYes++;
                }
            }

            int countNo = possiblecharacters.size() - countYes;
            double currentSplitScore = Math.abs(countYes - countNo);
            double noise = random.nextDouble() * 0.1;

            if (countYes > 0 && countNo > 0) {
                if (currentSplitScore + noise < bestSplitScore) {
                    bestSplitScore = currentSplitScore + noise;
                    bestQuestion = q;
                }
            }
        }

        if (bestQuestion != null) {
            remainingquestions.remove(bestQuestion);
        }
        return bestQuestion;
    }

    public GameCharacter makeFinalGuessObject() {
        if (possiblecharacters == null || possiblecharacters.isEmpty()) {
            return null;
        }
        int index = random.nextInt(possiblecharacters.size());
        return possiblecharacters.get(index);
    }

    public boolean shouldTakeFinalRisk(int playerRemainingChars) {
        if (playerRemainingChars <= 1) {
            return true;
        }

        if (playerRemainingChars == 2) {
            return random.nextFloat() < (aggressiveness + 0.2f);
        }

        if (playerRemainingChars == 3 && possiblecharacters.size() > 5) {
            return random.nextFloat() < (aggressiveness * 0.5f);
        }

        return false;
    }

    public void filterPossibleCharacters(Question q, boolean answer) {
        List<GameCharacter> toKeep = new ArrayList<>();
        for (GameCharacter character : possiblecharacters) {
            if (character.hasAttribute(q.key) == answer) {
                toKeep.add(character);
            }
        }
        
        possiblecharacters.clear();
        possiblecharacters.addAll(toKeep);
    }

    public void recordPlayerWin(Context context) {
        playerWinsCount++;
        saveMemory(context);
    }

    public void recordAIWin(Context context) {
        aiWinsCount++;
        saveMemory(context);
    }

    private void saveMemory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_PLAYER_WINS, playerWinsCount);
        editor.putInt(KEY_AI_WINS, aiWinsCount);
        editor.apply();
        calculateAggressiveness();
    }

    public int getRemainingCharactersCount() {
        return possiblecharacters.size();
    }

    public void removeQuestionFromAI(Question q) {
        for (int i = 0; i < remainingquestions.size(); i++) {
            Question question = remainingquestions.get(i);
            if (question.key.equals(q.key)) {
                remainingquestions.remove(i);
                break;
            }
        }
    }
}