package com.example.fixed_guess_who;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private GridLayout gridCharacters;
    private LinearLayout rootLayout;
    private TextView tvScreenTitle;
    private ImageView imgSideCharacter;
    private TextView tvSideCharacterName;
    private View victoryOverlay, loseOverlay, introSelectionOverlay, confirmationSelectionOverlay,yourTurnOverlay,opponentTurnOverlay;
    private View vsIntroOverlay, waitingForOpponentOverlay;
    private TextView tvVsMyName, tvVsOpponentName;
    private ImageView imgSelectedConfirm;
    private TextView tvSelectedConfirmName;
    private TextView tvVictoryTitle, tvVictoryCharacter;
    private TextView tvLoseTitle, tvLoseCharacter;
    private TextView tvYourTurn, tvOponentTurn;
    private View gameStartOverlay;
    private TextView tvStartTurnMessage, tvCountdown, playerTurn, opponentTurn;
    private String myUsername = "";
    private String opponentUsername = "";
    private boolean vsIntroShowing = false;
    private List<GameCharacter> characters = new ArrayList<>();
    private GameCharacter playerCharacter;
    private GameCharacter opponentCharacter;
    private HashMap<String, List<Question>> questionsMap;
    private ImageView selectedImage;
    private AIPlayer aiPlayer;
    private TurnManager turnManager;
    private TextView tvTimer;
    private CountDownTimer playerTimer;
    private boolean isOpponentScreen = false;
    private GameUIManager uiManager;
    private boolean isOnline = false;
    private String roomId;
    private boolean isPlayer1 = false;
    private MultiplayerManager mpManager;
    private boolean opponentReady = false;
    private boolean playerReady = false;

    private boolean gameEnded = false;
    private String currentOnlineTurn = "";
    private String lastReceivedQuestionId = null;

    private static final int NORMAL_COLOR = 0xFFFFF3C4;
    private static final int REMOVED_COLOR = 0xFF000000;
    private static final int PLAYER_BG_COLOR = 0xFFA48F63;
    private static final int OPPONENT_BG_COLOR = 0xFF7A694E;

    private List<Boolean> playerCardStates = new ArrayList<>();
    private List<Boolean> opponentCardStates = new ArrayList<>();

    private Button btnHint, btnPeek;
    private int hintCount = 0, peekCount = 0;
    private DatabaseReference mUserPowerupsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game);

        // אתחול Views
        initViews();
        setupButtons();

        // קבלת הקטגוריה שנבחרה
        String category = getIntent().getStringExtra("CATEGORY");
        isOnline = getIntent().getBooleanExtra("IS_ONLINE", false);
        roomId = getIntent().getStringExtra("ROOM_ID");
        isPlayer1 = getIntent().getBooleanExtra("IS_PLAYER_1", false);

        if (isOnline) {
            setupMultiplayerManager();
        }

        // Bug 1: mark player as "in game" so friends see the invite button as disabled
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users").child(uid)
                    .child("isOnline").setValue(false);
        }

        characters = DataManager.getCharactersByCategory(category);
        questionsMap = DataManager.getQuestions(category);

        // Initialize state lists early to avoid IndexOutOfBoundsException in displayCharacters
        for (int i = 0; i < characters.size(); i++) {
            playerCardStates.add(true);
            opponentCardStates.add(true);
        }

        uiManager = new GameUIManager(this);
        displayCharacters();
        updateBackgroundByScreen();

        if (isOnline) {
            // Show VS intro overlay first (2s), then start normal selection flow
            showVsIntroOverlay();
        } else {
            // Start selection flow with intro overlay (offline)
            introSelectionOverlay.setVisibility(View.VISIBLE);
            rootLayout.postDelayed(() -> {
                if (isFinishing() || isDestroyed()) return;
                introSelectionOverlay.setVisibility(View.GONE);
                showCharacterSelectionDialog();
            }, 2000);
        }
    }

    private boolean isSelectionDialogShowing = false;
    private AlertDialog selectionDialog;
    private CountDownTimer selectionTimer;

    private void showCharacterSelectionDialog() {
        if (isSelectionDialogShowing) return;
        isSelectionDialogShowing = true;

        View view = getLayoutInflater().inflate(R.layout.dialog_character_selection, null);
        GridLayout grid = view.findViewById(R.id.gridSelectionCharacters);
        TextView tvTimer = view.findViewById(R.id.tvSelectionTimer);

        selectionDialog = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                .setView(view)
                .setCancelable(false)
                .create();

        if (selectionDialog.getWindow() != null) {
            selectionDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            selectionDialog.getWindow().setBackgroundDrawableResource(R.drawable.board_background);
        }


        

        grid.post(() -> {
            int columns = 10;
            int totalWidth = grid.getWidth();
            int spacing = dp(5);
            int size = (totalWidth - spacing * (columns - 1)) / columns;

            int totalItems = characters.size();
            for (int i = 0; i < totalItems; i++) {
                final GameCharacter c = characters.get(i);
                CardView card = (CardView) getLayoutInflater().inflate(R.layout.item_character_card, null);
                
                int row = i / columns;
                int col = i % columns;
                
                // Centering logic for the last row
                int totalRows = (int) Math.ceil((double) totalItems / columns);
                if (row == totalRows - 1) {
                    int itemsInLastRow = totalItems % columns;
                    if (itemsInLastRow == 0) itemsInLastRow = columns;
                    int offset = (columns - itemsInLastRow) / 2;
                    col += offset;
                }

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(row),
                        GridLayout.spec(col)
                );
                params.width = size;
                params.height = (int) (size * 1.1f);
                params.setMargins(spacing / 3, spacing / 3, spacing / 3, spacing / 3);
                card.setLayoutParams(params);

                ImageView img = card.findViewById(R.id.imgCharacter);
                TextView tv = card.findViewById(R.id.tvCharacterName);

                img.setImageResource(c.imageResId);
                tv.setText(c.name);

                card.setOnClickListener(v -> {
                    MusicManager.sound_login(this);
                    if (selectionTimer != null) selectionTimer.cancel();
                    isSelectionDialogShowing = false;
                    selectionDialog.dismiss();
                    onCharacterSelected(c);
                });

                grid.addView(card);
            }
        });

        selectionTimer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("זמן לבחירה: " + (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                if (selectionDialog != null && selectionDialog.isShowing()) {
                    isSelectionDialogShowing = false;
                    selectionDialog.dismiss();
                    GameCharacter randomChar = characters.get(new Random().nextInt(characters.size()));
                    onCharacterSelected(randomChar);
                    Toast.makeText(GameActivity.this, "הזמן נגמר! נבחרה עבורך דמות באופן אקראי", Toast.LENGTH_SHORT).show();
                }
            }
        }.start();

        selectionDialog.show();
    }

    private void showVsIntroOverlay() {
        if (vsIntroOverlay == null) return;
        vsIntroOverlay.setVisibility(View.VISIBLE);
        vsIntroShowing = true;

        // Load own username then listen for opponent name in Firebase
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("username")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            myUsername = snapshot.getValue(String.class) != null ? snapshot.getValue(String.class) : "אתה";
                            tvVsMyName.setText(myUsername);
                            // Write my name to the room so opponent can display it
                            if (roomId != null) {
                                String myKey = isPlayer1 ? "p1Name" : "p2Name";
                                FirebaseDatabase.getInstance().getReference("rooms").child(roomId).child(myKey).setValue(myUsername);
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }

        // Listen for opponent name in Firebase room
        if (roomId != null) {
            String oppKey = isPlayer1 ? "p2Name" : "p1Name";
            FirebaseDatabase.getInstance().getReference("rooms").child(roomId).child(oppKey)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.getValue(String.class);
                            if (name != null && !name.isEmpty()) {
                                opponentUsername = name;
                                tvVsOpponentName.setText(opponentUsername);
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }

        // After 2 seconds, hide VS overlay and start normal selection flow
        rootLayout.postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            vsIntroOverlay.setVisibility(View.GONE);
            vsIntroShowing = false;
            introSelectionOverlay.setVisibility(View.VISIBLE);
            rootLayout.postDelayed(() -> {
                if (isFinishing() || isDestroyed()) return;
                introSelectionOverlay.setVisibility(View.GONE);
                showCharacterSelectionDialog();
            }, 2000);
        }, 2000);
    }

    private void setupMultiplayerManager() {
        mpManager = new MultiplayerManager(FirebaseAuth.getInstance().getUid(), new MultiplayerManager.MultiplayerCallback() {
            @Override
            public void onMatchFound(String id, boolean p1) {
                // Room already joined
            }

            @Override
            public void onGameStateChanged(DataSnapshot snapshot) {
                handleOnlineStateChange(snapshot);
            }

            @Override
            public void onOpponentDisconnected() {
            if (isFinishing() || isDestroyed() || gameEnded) return;
            gameEnded = true;
            uiManager.handleGameEnd(
                    GameUIManager.GameEndReason.OPPONENT_DISCONNECTED,
                    opponentCharacter != null ? opponentCharacter.name : "???",
                    loseOverlay, // We use loseOverlay because that's where the background is for game end screens
                    tvLoseTitle,
                    tvLoseCharacter,
                    findViewById(R.id.buttonContainer),
                    oldMoney -> finalizeGameAndLeave(oldMoney)
            );
        }
        });
        mpManager.joinExistingRoom(roomId, isPlayer1);
        
        // Set force-close penalty
        MoneyManager.getInstance().setDisconnectPenalty(5);
    }

    private void handleOnlineStateChange(DataSnapshot snapshot) {
        // Sync character selection
        String oppCharKey = isPlayer1 ? "p2Char" : "p1Char";
        if (opponentCharacter == null && snapshot.hasChild(oppCharKey)) {
            String charName = snapshot.child(oppCharKey).getValue(String.class);
            opponentCharacter = findCharacterByName(charName);
            opponentReady = true;
            checkBothReady();
        }

        // Sync questions
        String myQuestionKey = isPlayer1 ? "p1Asking" : "p2Asking";
        String oppQuestionKey = isPlayer1 ? "p2Asking" : "p1Asking";
        
        if (snapshot.hasChild(oppQuestionKey)) {
            DataSnapshot qSnap = snapshot.child(oppQuestionKey);
            String questionKey = qSnap.child("key").getValue(String.class);
            String questionText = qSnap.child("text").getValue(String.class);
            
            // Only show if it's a new question
            if (questionKey != null && !questionKey.equals(lastReceivedQuestionId)) {
                Question q = new Question(questionText, questionKey);
                showOnlineOpponentQuestion(q);
            }
        }

        // Sync answers
        String myAnswerKey = isPlayer1 ? "p1Answer" : "p2Answer";
        String oppAnswerKey = isPlayer1 ? "p2Answer" : "p1Answer";

        if (snapshot.hasChild(oppAnswerKey)) {
            Boolean answer = snapshot.child(oppAnswerKey).getValue(Boolean.class);
            if (answer != null) {
                handleOnlineOpponentAnswer(answer);
            }
        }
        
        if (isOnline && !isPlayer1 && snapshot.hasChild("p1Starts") && !startOverlayShown) {
            Boolean p1StartsObj = snapshot.child("p1Starts").getValue(Boolean.class);
            if (p1StartsObj != null) {
                startOverlayShown = true;
                // Hide waiting overlay for player 2 if shown
                if (waitingForOpponentOverlay != null)
                    waitingForOpponentOverlay.setVisibility(View.GONE);
                
                currentOnlineTurn = p1StartsObj ? "p1" : "p2";
                boolean meStarts = currentOnlineTurn.equals("p2");
                
                // Show the game-start overlay for Player 2
                tvStartTurnMessage.setText(meStarts ? "אתה מתחיל" : "היריב מתחיל");
                gameStartOverlay.setVisibility(View.VISIBLE);
                new CountDownTimer(4000, 1000) {
                    @Override
                    public void onTick(long ms) {
                        if (isFinishing() || isDestroyed()) { cancel(); return; }
                        int s = (int)(ms / 1000);
                        tvCountdown.setText(s > 0 ? String.valueOf(s) : "התחל!");
                    }
                    @Override
                    public void onFinish() {
                        if (isFinishing() || isDestroyed()) return;
                        gameStartOverlay.animate().alpha(0f).setDuration(500).withEndAction(() -> {
                            gameStartOverlay.setVisibility(View.GONE);
                            gameStartOverlay.setAlpha(1f);
                            finalizeStartGame(meStarts);
                        }).start();
                    }
                }.start();
            }
        }

        // Sync turn
        if (isOnline && snapshot.hasChild("currentTurn")) {
            String newTurn = snapshot.child("currentTurn").getValue(String.class);
            if (newTurn != null && !newTurn.equals(currentOnlineTurn)) {
                currentOnlineTurn = newTurn;
                handleTurnChange();
            }
        }

        // Sync turn timer (only if online)
        if (isOnline && snapshot.hasChild("lastTurnStartTime")) {
            Long startTime = snapshot.child("lastTurnStartTime").getValue(Long.class);
            if (startTime != null) {
                long estimatedServerTime = System.currentTimeMillis();
                startPlayerTurnSynced(startTime, estimatedServerTime);
            }
        }

        // Sync Opponent Board
        String oppBoardKey = isPlayer1 ? "p2Board" : "p1Board";
        if (isOnline && snapshot.hasChild(oppBoardKey)) {
            DataSnapshot boardSnap = snapshot.child(oppBoardKey);
            for (DataSnapshot cardSnap : boardSnap.getChildren()) {
                int index = Integer.parseInt(cardSnap.getKey());
                Boolean isAlive = cardSnap.getValue(Boolean.class);
                if (isAlive != null && index < opponentCardStates.size()) {
                    opponentCardStates.set(index, isAlive);
                }
            }
            if (isOpponentScreen) displayCharacters();
        }

        // Sync game end
        if (isOnline && snapshot.hasChild("gameOutcome")) {
            String winnerRole = snapshot.child("gameOutcome").getValue(String.class);
            if (winnerRole != null && !gameEnded) {
                gameEnded = true;
                boolean iWon = winnerRole.equals(isPlayer1 ? "p1" : "p2");

                // Extract detailed guess info if available
                String lastGuessBy = snapshot.child("lastGuessBy").getValue(String.class);
                String lastGuessName = snapshot.child("lastGuessName").getValue(String.class);
                Boolean lastGuessCorrect = snapshot.child("lastGuessCorrect").getValue(Boolean.class);
                Boolean opponentLeft = snapshot.child("opponentLeft").getValue(Boolean.class);

                if (lastGuessName == null) {
                    lastGuessName = opponentCharacter != null ? opponentCharacter.name : "???";
                }

                // If the opponent explicitly quit, show the 'opponent left' win screen
                if (Boolean.TRUE.equals(opponentLeft) && iWon) {
                    uiManager.handleGameEnd(
                        GameUIManager.GameEndReason.OPPONENT_LEFT_GAME,
                        "",
                        victoryOverlay, tvVictoryTitle, tvVictoryCharacter,
                        findViewById(R.id.buttonContainer),
                        oldMoney -> finalizeGameAndLeave(oldMoney)
                    );
                    return;
                }

                GameUIManager.GameEndReason reason;
                if (lastGuessBy != null) {
                    boolean isOpponentGuess = !lastGuessBy.equals(isPlayer1 ? "p1" : "p2");
                    if (isOpponentGuess) {
                        reason = (lastGuessCorrect != null && lastGuessCorrect) ?
                            GameUIManager.GameEndReason.OPPONENT_GUESSED_RIGHT :
                            GameUIManager.GameEndReason.OPPONENT_GUESSED_WRONG;
                        handleRemoteGameEndWithDetails(reason, lastGuessName);
                    } else {
                        reason = (lastGuessCorrect != null && lastGuessCorrect) ?
                            GameUIManager.GameEndReason.YOU_GUESSED_RIGHT :
                            GameUIManager.GameEndReason.YOU_GUESSED_WRONG;
                        handleRemoteGameEndWithDetails(reason, opponentCharacter != null ? opponentCharacter.name : lastGuessName);
                    }
                } else if (iWon) {
                    handleRemoteGameEndWithDetails(GameUIManager.GameEndReason.OPPONENT_DISCONNECTED, "");
                } else {
                    handleRemoteGameEndWithDetails(GameUIManager.GameEndReason.YOU_GUESSED_WRONG, opponentCharacter != null ? opponentCharacter.name : "???");
                }
            }
        }
    }

    private void handleRemoteGameEndWithDetails(GameUIManager.GameEndReason reason, String charName) {
        if (isFinishing() || isDestroyed()) return;
        uiManager.handleGameEnd(
                reason,
                charName,
                (reason.isWin() ? victoryOverlay : loseOverlay),
                (reason.isWin() ? tvVictoryTitle : tvLoseTitle),
                (reason.isWin() ? tvVictoryCharacter : tvLoseCharacter),
                findViewById(R.id.buttonContainer),
                oldMoney -> finalizeGameAndLeave(oldMoney)
        );
    }



    private void handleTurnChange() {
        boolean isMyTurn = currentOnlineTurn.equals(isPlayer1 ? "p1" : "p2");
        if (isMyTurn) {
            // New turn started for me, update start time for sync
            mpManager.updateGameState("lastTurnStartTime", ServerValue.TIMESTAMP);

            // Show "your turn" overlay for 1.5s
            if (yourTurnOverlay != null) {
                yourTurnOverlay.setVisibility(View.VISIBLE);
                rootLayout.postDelayed(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    yourTurnOverlay.setVisibility(View.GONE);
                }, 1500);
            }
        } else {
            // Waiting for opponent's turn start time update
        }
    }

    private void showOnlineOpponentQuestion(Question q) {
        if (q == null) return;
        lastReceivedQuestionId = q.key;

        // Clear the question from DB immediately so it doesn't trigger again
        mpManager.updateGameState(isPlayer1 ? "p2Asking" : "p1Asking", null);

        stopTimer(); // Pause timer while answering
        AlertDialog qDialog = new AlertDialog.Builder(this)
            .setTitle("היריב שואל...")
            .setMessage(q.text)
            .setPositiveButton("כן", (d, w) -> {
                boolean realAnswer = playerCharacter.hasAttribute(q.key);
                if (!realAnswer) {
                    Toast.makeText(GameActivity.this, "תגיד תאמת יפרה", Toast.LENGTH_SHORT).show();
                    showOnlineOpponentQuestion(q);
                } else {
                    sendOnlineAnswer(true);
                    lastReceivedQuestionId = null;
                }
            })
            .setNegativeButton("לא", (d, w) -> {
                boolean realAnswer = playerCharacter.hasAttribute(q.key);
                if (realAnswer) {
                    Toast.makeText(GameActivity.this, "תגיד תאמת יפרה", Toast.LENGTH_SHORT).show();
                    showOnlineOpponentQuestion(q);
                } else {
                    sendOnlineAnswer(false);
                    lastReceivedQuestionId = null;
                }
            })
            .setCancelable(false)
            .create();
        if (qDialog.getWindow() != null) qDialog.getWindow().setBackgroundDrawableResource(R.drawable.board_background);
        stopTimer(); // Pause timer while answering
        vibrate(500); // Vibrate on receiving a question
        qDialog.show();
        // Force black message text so it's readable on all phone themes
        android.widget.TextView msg = qDialog.findViewById(android.R.id.message);
        if (msg != null) msg.setTextColor(Color.BLACK);
    }

    private void sendOnlineAnswer(boolean answer) {
        mpManager.updateGameState(isPlayer1 ? "p1Answer" : "p2Answer", answer);
        // Turn switches to the one who asked
    }

    private void handleOnlineOpponentAnswer(Boolean answer) {
        if (answer == null || lastAskedQuestion == null) return;
        
        handleOnlineOpponentAnswerActual(answer, lastAskedQuestion);
        lastAskedQuestion = null; // Reset after processing
    }

    private Question lastAskedQuestion;

    private void handleOnlineOpponentAnswerActual(Boolean answer, Question q) {
        if (answer == null || q == null) return;

        int remainingCount = 0;
        for (int i = 0; i < characters.size(); i++) {
            GameCharacter c = characters.get(i);
            if (c.hasAttribute(q.key) != answer) {
                playerCardStates.set(i, false);
                updateCardVisualsInGrid(i, false);
            }
            if (playerCardStates.get(i)) {
                remainingCount++;
            }
        }
        
        if (turnManager != null) {
            turnManager.updatePlayerProgress(remainingCount);
        }

        // Clear the answer and question in DB to signal we processed it
        mpManager.updateGameState(isPlayer1 ? "p2Answer" : "p1Answer", null);
        
        // After processing an answer, it becomes the opponent's turn.
        if (isOnline) {
             mpManager.updateGameState("currentTurn", isPlayer1 ? "p2" : "p1");
        }
    }

    private GameCharacter findCharacterByName(String name) {
        for (GameCharacter c : characters) {
            if (c.name.equalsIgnoreCase(name)) return c;
        }
        return null;
    }

    private boolean gameStarted = false;
    private boolean startOverlayShown = false;
    private void checkBothReady() {
        if (playerReady && opponentReady && !gameStarted) {
            gameStarted = true;
            // Hide waiting overlay before starting game logic
            if (waitingForOpponentOverlay != null)
                waitingForOpponentOverlay.setVisibility(View.GONE);
            startGameLogic();
        }
    }

    private void onCharacterSelected(GameCharacter selected) {
        playerCharacter = selected;
        showSideCharacter();

        if (isOnline) {
            String key = isPlayer1 ? "p1Char" : "p2Char";
            mpManager.updateGameState(key, playerCharacter.name);
            playerReady = true;
            // Bug 2: don't call checkBothReady until the confirmation overlay has had time
            // to be visible, so the game-start overlay cannot appear on top of it.
        }

        confirmationSelectionOverlay.setVisibility(View.VISIBLE);
        imgSelectedConfirm.setImageResource(playerCharacter.imageResId);
        tvSelectedConfirmName.setText(playerCharacter.name.toLowerCase());

        imgSelectedConfirm.setAlpha(0f);
        imgSelectedConfirm.setScaleX(0.5f);
        imgSelectedConfirm.setScaleY(0.5f);
        imgSelectedConfirm.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(new android.view.animation.OvershootInterpolator())
                .start();

        if (!isOnline) {
            rootLayout.postDelayed(() -> {
                if (isFinishing() || isDestroyed()) return;
                confirmationSelectionOverlay.setVisibility(View.GONE);
                startGameLogic();
            }, 2000);
        } else {
            // After confirmation, hide it and show "waiting for opponent" overlay
            rootLayout.postDelayed(() -> {
                if (isFinishing() || isDestroyed()) return;
                confirmationSelectionOverlay.setVisibility(View.GONE);
                // Show waiting overlay if opponent hasn't chosen yet
                if (!opponentReady && waitingForOpponentOverlay != null) {
                    waitingForOpponentOverlay.setVisibility(View.VISIBLE);
                }
                checkBothReady();
            }, 2000);
        }
    }

    private void finalizeStartGame(boolean playerStarts) {
        showSideCharacter();
        setupTurnManager();
        
        // If online, penalty is already set in setupMultiplayerManager
        
        if (playerStarts) {
            startPlayerTurn();
        } else {
            setButtonsEnabled(false);
            if (isOnline) {
                // In online mode, we still start the timer to show "Opponent turn" countdown
                startPlayerTurn();
            } else if (turnManager != null) {
                turnManager.startAITurn();
            }
        }
    }

    private void showRandomizedStartOverlay() {
        boolean playerStarts = false;
        if (isOnline) {
            if (isPlayer1) {
                boolean p1Starts = new Random().nextBoolean();
                currentOnlineTurn = p1Starts ? "p1" : "p2";
                mpManager.updateGameState("p1Starts", p1Starts);
                mpManager.updateGameState("currentTurn", currentOnlineTurn);
            } else {
                // Player 2 waits for Player 1 to decide who starts
                return; 
            }
        } else {
            playerStarts = new Random().nextBoolean();
        }
        
        boolean playerStartsFinal = isOnline ? currentOnlineTurn.equals(isPlayer1 ? "p1" : "p2") : playerStarts;
        tvStartTurnMessage.setText(playerStartsFinal ? "אתה מתחיל" : "היריב מתחיל");
        gameStartOverlay.setVisibility(View.VISIBLE);

        new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                if (seconds > 0) {
                    tvCountdown.setText(String.valueOf(seconds));
                    tvCountdown.setScaleX(1.5f);
                    tvCountdown.setScaleY(1.5f);
                    tvCountdown.animate().scaleX(1f).scaleY(1f).setDuration(500).start();
                } else {
                    tvCountdown.setText("התחל!");
                }
            }

            @Override
            public void onFinish() {
                gameStartOverlay.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction(() -> {
                            gameStartOverlay.setVisibility(View.GONE);
                            gameStartOverlay.setAlpha(1f);
                            finalizeStartGame(playerStartsFinal);
                        }).start();
            }
        }.start();
    }

    private void startGameLogic() {
        // Initialize AIPlayer and Question setup
        List<Question> allQuestionsForAI = new ArrayList<>();
        if (questionsMap != null) {
            for (List<Question> categoryList : questionsMap.values()) {
                allQuestionsForAI.addAll(categoryList);
            }
        }
        aiPlayer = new AIPlayer(characters, allQuestionsForAI, this);

        // Pick random opponent character different from player
        if (!isOnline) {
            Random r = new Random();
            opponentCharacter = characters.get(r.nextInt(characters.size()));
            while (opponentCharacter.name.equals(playerCharacter.name)) {
                opponentCharacter = characters.get(r.nextInt(characters.size()));
            }
        }

        // Stop animations and refresh grid
        for (int i = 0; i < gridCharacters.getChildCount(); i++) {
            gridCharacters.getChildAt(i).clearAnimation();
        }

        showRandomizedStartOverlay();
    }

    private void setupTurnManager() {
        turnManager = new TurnManager(aiPlayer, playerCharacter, new TurnManager.TurnCallback() {

            @Override
            public void onPlayerTimeOut() {
                if (playerTimer != null) playerTimer.cancel();
                Toast.makeText(GameActivity.this, "הזמן שלך נגמר!", Toast.LENGTH_SHORT).show();
                if (turnManager != null) turnManager.startAITurn();
            }

            @Override
            public void onAIAsking(Question q, TurnManager.OnAnswerListener answerListener) {
                // 1. עצירת הטיימר של השחקן - התור שלו נעצר כרגע
                if (playerTimer != null) playerTimer.cancel();

                // 2. עדכון ה-AI והמאגר: מחיקת השאלה כדי שלא תחזור על עצמה
                removeQuestionFromMap(q);
                if (aiPlayer != null) {
                    aiPlayer.removeQuestionFromAI(q);
                }
                // 3. נעילת כפתורים למניעת לחיצות כפולות
                setButtonsEnabled(false);
                if (isOpponentScreen) {
                    Button btnSwitch = findViewById(R.id.btnSwitchScreen);
                    Button btnAsk = findViewById(R.id.btnAsk);
                    Button btnFinal = findViewById(R.id.btnFinalGuess);
                    Button btnBack = findViewById(R.id.btnBack);
                    switchScreenWithAnimation(btnSwitch, btnAsk, btnBack, btnFinal);
                }

                rootLayout.postDelayed(() -> {
                    boolean realAnswerForPlayer = playerCharacter.hasAttribute(q.key);

                    AlertDialog aiDialog = new AlertDialog.Builder(GameActivity.this)
                            .setTitle("ה-ai שואל אותך")
                            .setMessage(q.text)
                            .setPositiveButton("כן", (dialog, which) -> {
                                MusicManager.sound_login(GameActivity.this);

                                // מנגנון מניעת שקרים - חשוב כדי שה-AI לא יקבל נתונים שגויים ללמידה
                                if (!realAnswerForPlayer) {
                                    Toast.makeText(GameActivity.this, "תגיד תאמת יפרה", Toast.LENGTH_SHORT).show();
                                    // קריאה חוזרת להצגת השאלה עד שתתקבל תשובה נכונה
                                    onAIAsking(q, answerListener);
                                } else {
                                    // עדכון הלוח של ה-AI (הלוח שאנחנו רואים במסך יריב)
                                    updateOpponentBoard(q, true);
                                    answerListener.onAnswer(true);
                                }
                            })
                            .setNegativeButton("לא", (dialog, which) -> {
                                MusicManager.sound_login(GameActivity.this);

                                if (realAnswerForPlayer) {
                                    Toast.makeText(GameActivity.this, "תגיד תאמת יפרה", Toast.LENGTH_SHORT).show();
                                    onAIAsking(q, answerListener);
                                } else {
                                    updateOpponentBoard(q, false);
                                    answerListener.onAnswer(false);
                                }
                            })
                            .setCancelable(false) // חייבים לענות כדי להמשיך את רצף המשחק
                            .create();

                    // הצגת הדיאלוג עם עיצוב הרקע
                    if (!isFinishing() && !isDestroyed()) {
                        aiDialog.show();
                        if (aiDialog.getWindow() != null) {
                            aiDialog.getWindow().setBackgroundDrawableResource(R.drawable.board_background);
                        }
                        // Force black message text so it's readable on all phone themes
                        android.widget.TextView msg = aiDialog.findViewById(android.R.id.message);
                        if (msg != null) msg.setTextColor(Color.BLACK);
                    }

                }, 1200);
            }

            @Override
            public void onAIGuess(GameCharacter character, boolean iscorrect) {
                // 1. עצירת הטיימר של השחקן - המשחק נגמר ברגע שיש ניחוש סופי
                if (playerTimer != null) {
                    playerTimer.cancel();
                }

                if (!iscorrect) {
                    aiPlayer.recordPlayerWin(GameActivity.this);
                } else {
                    aiPlayer.recordAIWin(GameActivity.this);
                }

                String title = iscorrect ? "הפסדת" : "ניצחת!";
                String characterName = character.name;

                StringBuilder message = new StringBuilder();
                if (iscorrect) {
                    message.append("ה-מחשב ניחש נכון!\nהוא גילה שאתה: ").append(characterName);
                } else {
                    message.append("ה-מחשב טעה!\nהוא ניחש שאתה ").append(characterName)
                            .append(", אבל זה לא נכון.\nניצחת את המשחק!");
                }

                // 4. יצירת הדיאלוג הסופי להצגת התוצאה
                AlertDialog guessDialog = new AlertDialog.Builder(GameActivity.this)
                        .setTitle(title)
                        .setMessage(message.toString())
                        .setPositiveButton("המשך", (dialog, which) -> {
                            // סאונד סיום משחק (אופציונלי)
                            MusicManager.sound_login(GameActivity.this);

                            // אם ה-ai צדק (iscorrect=true), השחקן הפסיד (playerWon=false)
                            boolean playerWon = !iscorrect;

                            uiManager.showFinalResultDirectly(
                                    playerWon,
                                    oldMoney -> {
                                        Intent intent = new Intent(GameActivity.this, MenuActivity.class);
                                        intent.putExtra("FROM_GAME", true);
                                        intent.putExtra("OLD_MONEY", oldMoney);
                                        startActivity(intent);
                                        finish();
                                    }
                            );

                        })
                        .setCancelable(false) // מונע מהמשתמש לסגור את הדיאלוג בלחיצה בחוץ
                        .create();

                // 5. הצגת הדיאלוג למשתמש
                if (!isFinishing() && !isDestroyed()) {
                    guessDialog.show();

                    // 6. עיצוב הדיאלוג - החלת רקע הלוח המותאם אישית
                    if (guessDialog.getWindow() != null) {
                        guessDialog.getWindow().setBackgroundDrawableResource(R.drawable.board_background);
                    }
                }
            }

            @Override
            public void onTurnEnd() {
                if (isOnline) {
                    // In online mode, we don't automatically start AI turn.
                    // Instead, we wait for the opponent to act via Firebase.
                    setButtonsEnabled(false);
                } else {
                    rootLayout.postDelayed(() ->
                    {
                        startPlayerTurn();
                    }, 3000);
                }
            }
        }, this);
    }


    private void startPlayerTurn() {
        startPlayerTurnSynced(null, 0);
    }

    private void startPlayerTurnSynced(Long startTime, long currentServerTime) {
        setButtonsEnabled(true);
        stopTimer();
        
        boolean isMyTurn = true;
        int remainingSeconds = 30;

        if (isOnline) {
            isMyTurn = currentOnlineTurn.equals(isPlayer1 ? "p1" : "p2");
            if (!isMyTurn) {
                setButtonsEnabled(false);
            }
            if (startTime != null) {
                long elapsed = (currentServerTime - startTime) / 1000;
                remainingSeconds = (int) (30 - elapsed);
                if (remainingSeconds < 0) remainingSeconds = 0;
            }
        }

        int turnDuration = remainingSeconds * 1000;
        int tickInterval = 1000;

        String prefix = isOnline ? (isMyTurn ? "תורך: " : "תור היריב: ") : "זמן: ";
        tvTimer.setText(prefix + (turnDuration / 1000));
        tvTimer.setTextColor(isOnline ? (isMyTurn ? Color.GREEN : Color.RED) : Color.BLACK);

        final boolean finalIsMyTurn = isMyTurn;
        playerTimer = new CountDownTimer(turnDuration, tickInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isFinishing() && !isDestroyed()) {
                    String tickPrefix = isOnline ? (finalIsMyTurn ? "תורך: " : "תור היריב: ") : "זמן: ";
                    tvTimer.setText(tickPrefix + (millisUntilFinished / 1000));

                    if (millisUntilFinished <= 5000) {
                        tvTimer.setTextColor(Color.RED);
                    }
                }
            }

            @Override
            public void onFinish() {
                if (!isFinishing() && !isDestroyed()) {
                    String finishPrefix = isOnline ? (finalIsMyTurn ? "תורך: " : "תור היריב: ") : "זמן: ";
                    tvTimer.setText(finishPrefix + "0");
                    if (finalIsMyTurn) {
                        setButtonsEnabled(false);
                        if (isOnline) {
                            // Online: skip turn in Firebase
                            mpManager.updateGameState("currentTurn", isPlayer1 ? "p2" : "p1");
                            Toast.makeText(GameActivity.this, "נגמר הזמן! התור עבר", Toast.LENGTH_SHORT).show();
                        } else if (turnManager != null) {
                            turnManager.onPlayerTimeOut();
                        }
                    }
                }
            }
        }.start();
    }


    private void stopTimer() {
        if (playerTimer != null) {
            playerTimer.cancel();
            playerTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (selectionTimer != null) {
            selectionTimer.cancel();
            selectionTimer = null;
        }
        if (selectionDialog != null && selectionDialog.isShowing()) {
            selectionDialog.dismiss();
            selectionDialog = null;
        }
        stopTimer();
        if (isOnline && mpManager != null) {
            String category = getIntent().getStringExtra("CATEGORY");
            mpManager.leaveRoom(category);
        }
        super.onDestroy();
    }


    private void initViews() {
        gridCharacters = findViewById(R.id.gridCharacters);
        rootLayout = findViewById(R.id.rootLayout);
        tvScreenTitle = findViewById(R.id.tvScreenTitle);

        imgSideCharacter = findViewById(R.id.imgPlayerCharacter);
        tvSideCharacterName = findViewById(R.id.tvPlayerName);

        victoryOverlay = findViewById(R.id.victoryOverlay);
        tvVictoryTitle = findViewById(R.id.tvVictoryTitle);
        tvVictoryCharacter = findViewById(R.id.tvVictoryCharacter);

        loseOverlay = findViewById(R.id.loseOverlay);
        tvLoseTitle = findViewById(R.id.tvLoseTitle);
        tvLoseCharacter = findViewById(R.id.tvLoseCharacter);
        tvTimer = findViewById(R.id.tvTimer);

        introSelectionOverlay = findViewById(R.id.introSelectionOverlay);
        confirmationSelectionOverlay = findViewById(R.id.confirmationSelectionOverlay);
        imgSelectedConfirm = findViewById(R.id.imgSelectedConfirm);
        tvSelectedConfirmName = findViewById(R.id.tvSelectedConfirmName);

        gameStartOverlay = findViewById(R.id.gameStartOverlay);
        tvStartTurnMessage = findViewById(R.id.tvStartTurnMessage);
        tvCountdown = findViewById(R.id.tvCountdown);

        opponentTurnOverlay = findViewById(R.id.opponentTurnOverlay);
        tvOponentTurn = findViewById(R.id.tvOponentTurn);

        yourTurnOverlay = findViewById(R.id.yourTurnOverlay);
        tvYourTurn = findViewById(R.id.tvYourTurn);

        vsIntroOverlay = findViewById(R.id.vsIntroOverlay);
        tvVsMyName = findViewById(R.id.tvVsMyName);
        tvVsOpponentName = findViewById(R.id.tvVsOpponentName);

        waitingForOpponentOverlay = findViewById(R.id.waitingForOpponentOverlay);

        btnHint = findViewById(R.id.btnHint);
        btnPeek = findViewById(R.id.btnPeek);
        
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUserPowerupsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("powerups");
        listenToPowerups();
    }




    private void setupButtons() {
        Button btnBack = findViewById(R.id.btnBack);
        Button btnFinalGuess = findViewById(R.id.btnFinalGuess);
        Button btnSwitchScreen = findViewById(R.id.btnSwitchScreen);
        Button btnAsk = findViewById(R.id.btnAsk);

        btnAsk.setOnClickListener(v -> {
            MusicManager.sound_login(this);
            showQuestionsDialog();
        });


        btnSwitchScreen.setOnClickListener(v ->
                switchScreenWithAnimation(btnSwitchScreen, btnAsk, btnFinalGuess, btnBack));

        btnBack.setOnClickListener(v -> {
            MusicManager.sound_login(this);

            if (isOnline && !gameEnded) {
                // Online: warn about technical loss
                String message = "יציאה באמצע משחק אונליין תגרום להפסד טכני והחסרה של 5 מטבעות. האם תרצה להמשיך?";
                AlertDialog backDialog = new AlertDialog.Builder(this)
                        .setTitle("חזרה")
                        .setMessage(message)
                        .setPositiveButton("כן", (dialog, which) -> {
                            gameEnded = true;
                            // Tell Firebase the opponent wins AND that current player left
                            String winnerRole = isPlayer1 ? "p2" : "p1";
                            mpManager.updateGameState("opponentLeft", true);
                            mpManager.updateGameState("gameOutcome", winnerRole);

                            // Show loss screen locally
                            uiManager.handleGameEnd(
                                    GameUIManager.GameEndReason.YOU_LEFT_GAME,
                                    opponentCharacter != null ? opponentCharacter.name : "???",
                                    loseOverlay, tvLoseTitle, tvLoseCharacter,
                                    findViewById(R.id.buttonContainer),
                                    oldMoney -> finalizeGameAndLeave(oldMoney)
                            );
                        })
                        .setNegativeButton("לא", null)
                        .create();
                if (backDialog.getWindow() != null)
                    backDialog.getWindow().setBackgroundDrawableResource(R.drawable.board_background);
                backDialog.show();

            } else if (!isOnline && !gameEnded) {
                // Offline vs AI: also warn, then show lose dialog
                AlertDialog backDialog = new AlertDialog.Builder(this)
                        .setTitle("חזרה")
                        .setMessage("פרישה מהמשחק תיחשב כהפסד. האם אתה בטוח שברצונך לצאת?")
                        .setPositiveButton("כן", (dialog, which) -> {
                            gameEnded = true;
                            uiManager.handleGameEnd(
                                    GameUIManager.GameEndReason.YOU_LEFT_GAME,
                                    opponentCharacter != null ? opponentCharacter.name : "???",
                                    loseOverlay, tvLoseTitle, tvLoseCharacter,
                                    findViewById(R.id.buttonContainer),
                                    oldMoney -> finalizeGameAndLeave(oldMoney)
                            );
                        })
                        .setNegativeButton("לא", null)
                        .create();
                if (backDialog.getWindow() != null)
                    backDialog.getWindow().setBackgroundDrawableResource(R.drawable.board_background);
                backDialog.show();

            } else {
                // Game already ended
                finish();
            }
        });
        btnFinalGuess.setOnClickListener(v -> {
            MusicManager.sound_login(this);
            showFinalGuessDialog();
        });

        btnHint.setOnClickListener(v -> useHint());
        btnPeek.setOnClickListener(v -> usePeek());

        updateButtonsByScreen(btnSwitchScreen, btnAsk, btnFinalGuess, btnBack);
    }
    
    private void listenToPowerups() {
        mUserPowerupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long h = snapshot.child("hint").getValue(Long.class);
                Long p = snapshot.child("peek").getValue(Long.class);
                hintCount = h != null ? h.intValue() : 0;
                peekCount = p != null ? p.intValue() : 0;
                updatePowerupButtons();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updatePowerupButtons() {
        btnHint.setAlpha(hintCount > 0 ? 1.0f : 0.5f);
        btnPeek.setAlpha(peekCount > 0 ? 1.0f : 0.5f);
    }

    private void useHint() {
        if (hintCount <= 0) {
            Toast.makeText(this, "אין לך רמזים!", Toast.LENGTH_SHORT).show();
            return;
        }

        // לוגיקת רמז: הסרת 2 דמויות שגויות מהלוח
        List<Integer> aliveWrongIndices = new ArrayList<>();
        for (int i = 0; i < characters.size(); i++) {
            if (playerCardStates.get(i) && !characters.get(i).name.equals(opponentCharacter.name)) {
                aliveWrongIndices.add(i);
            }
        }

        if (aliveWrongIndices.size() < 2) {
            Toast.makeText(this, "נשאר מספר קטן מדי של דמויות!", Toast.LENGTH_SHORT).show();
            return;
        }

        Collections.shuffle(aliveWrongIndices);
        for (int i = 0; i < 3; i++) {
            int idx = aliveWrongIndices.get(i);
            playerCardStates.set(idx, false);
            updateCardVisualsInGrid(idx, false);
        }

        MoneyManager.getInstance().usePowerup("hint");
        MusicManager.sound_login(this);
        Toast.makeText(this, "רמז הופעל: 3 דמויות הוסרו!", Toast.LENGTH_SHORT).show();
    }

    private void usePeek() {
        if (peekCount <= 0) {
            Toast.makeText(this, "אין לך הצצות!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (opponentCharacter == null) return;

        // לוגיקת הצצה: חשיפת תכונה רנדומלית של היריב
        // נחפש תכונה שהיריב *כן* מחזיק
        List<Question> allPossibilities = new ArrayList<>();
        for (List<Question> list : questionsMap.values()) {
            allPossibilities.addAll(list);
        }
        
        List<Question> validQuestions = new ArrayList<>();
        for (Question q : allPossibilities) {
            if (opponentCharacter.hasAttribute(q.key)) {
                validQuestions.add(q);
            }
        }

        if (validQuestions.isEmpty()) {
            Toast.makeText(this, "לא נמצא מידע נוסף לחשיפה!", Toast.LENGTH_SHORT).show();
            return;
        }

        Question randomQ = validQuestions.get(new Random().nextInt(validQuestions.size()));
        
        new AlertDialog.Builder(this)
                .setTitle("הצצה!")
                .setMessage("גילית מידע על דמות היריב:\n\n" + randomQ.text + " - כן!")
                .setPositiveButton("מעולה", null)
                .show();

        MoneyManager.getInstance().usePowerup("peek");
        MusicManager.sound_login(this);
    }

    private void showQuestionsDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_questions_recycler, null);
        RecyclerView rv = view.findViewById(R.id.rvQuestionCategories);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<QuestionCategoryAdapter.QuestionGroup> groups = new ArrayList<>();
        String currentCategory = getIntent().getStringExtra("CATEGORY");

        // מיפוי קטגוריות למפתחות במפה
        Map<String, String> appearanceKeys = new HashMap<>();
        appearanceKeys.put("רגיל", "שאלות רגיל");
        appearanceKeys.put("אנימה", "שאלות מראה (אנימה)");
        appearanceKeys.put("מארוול", "שאלות מראה (מארוול)");

        Map<String, String> knowledgeKeys = new HashMap<>();
        knowledgeKeys.put("רגיל", "שאלות רגיל"); // אם יש שאלות ידע שונות אפשר לשנות
        knowledgeKeys.put("אנימה", "שאלות אנימה");
        knowledgeKeys.put("מארוול", "שאלות מארוול");

        String appearanceKey = appearanceKeys.get(currentCategory);
        String knowledgeKey = knowledgeKeys.get(currentCategory);

        List<Question> appearanceQuestions = new ArrayList<>();
        if (appearanceKey != null && questionsMap.get(appearanceKey) != null) {
            appearanceQuestions.addAll(questionsMap.get(appearanceKey));
        }

        List<Question> knowledgeQuestions = new ArrayList<>();
        if (knowledgeKey != null && questionsMap.get(knowledgeKey) != null) {
            knowledgeQuestions.addAll(questionsMap.get(knowledgeKey));
        }

        groups.add(new QuestionCategoryAdapter.QuestionGroup("שאלות מראה", appearanceQuestions, false));
        
        // נסתיר את שאלות הידע בקטגוריה הרגילה כי הן זהות לשאלות המראה
        if (!"רגיל".equals(currentCategory)) {
            groups.add(new QuestionCategoryAdapter.QuestionGroup("שאלות ידע", knowledgeQuestions, false));
        }

        if (groups.size() == 1) {
            groups.get(0).isExpanded = true;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setNegativeButton("סגור", null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.board_background);
        }

        QuestionCategoryAdapter adapter = new QuestionCategoryAdapter(groups, question -> {
            dialog.dismiss();
            removeQuestionFromMap(question);
            handleQuestion(question);
        });

        rv.setAdapter(adapter);
        dialog.show();
    }


    private void removeQuestionFromMap(Question q) {
        for (String key : questionsMap.keySet()) {
            List<Question> list = questionsMap.get(key);
            if (list != null) {
                list.remove(q);
            }
        }
    }

    private void handleQuestion(Question q) {
        // 1. נטרול מיידי של הכפתורים ועצירת טיימר
        setButtonsEnabled(false);
        if (playerTimer != null) playerTimer.cancel();
        if (aiPlayer != null) {
            aiPlayer.removeQuestionFromAI(q);
        }

        if (isOnline) {
            // Online mode: Send question to Firebase
            lastAskedQuestion = q;
            Map<String, Object> qData = new HashMap<>();
            qData.put("key", q.key);
            qData.put("text", q.text);
            mpManager.updateGameState(isPlayer1 ? "p1Asking" : "p2Asking", qData);
            // Now we wait for handleOnlineStateChange to see p1Answer or p2Answer
            return;
        }

        // VS AI mode:
        rootLayout.postDelayed(() -> {
            boolean realAnswer = opponentCharacter.hasAttribute(q.key);
            String answerText = realAnswer ? "כן!" : "לא.";

            AlertDialog answerDialog = new AlertDialog.Builder(this)
                    .setTitle("תשובת היריב")
                    .setMessage("שאלת: " + q.text + "\n\nתשובת המחשב: " + answerText)
                    .setPositiveButton("הבנתי", (dialog, which) -> {
                        // התור של ה-AI יתחיל רק אחרי שהדיאלוג ייסגר והנתונים יתעדכנו
                    })
                    .setCancelable(false)
                    .create();

            answerDialog.setOnDismissListener(dialog -> {
                int remainingCount = 0; // משתנה לספירה מחדש

                for (int i = 0; i < characters.size(); i++) {
                    GameCharacter c = characters.get(i);
                    if (c.hasAttribute(q.key) != realAnswer) {
                        playerCardStates.set(i, false);
                        updateCardVisualsInGrid(i, false);
                    }

                    // סופרים כמה דמויות נשארו "חיות" בלוח השחקן
                    if (playerCardStates.get(i)) {
                        remainingCount++;
                    }
                }

                // עדכון ה-TurnManager במספר המדויק לפני שתור ה-AI מתחיל
                if (turnManager != null) {
                    turnManager.updatePlayerProgress(remainingCount);
                }

                // עכשיו, כשה-AI יודע בדיוק כמה דמויות נשארו לך, אפשר להתחיל את התור שלו
                rootLayout.postDelayed(() -> {
                    if (turnManager != null) {
                        turnManager.startAITurn();
                    }
                }, 1000);
            });

            if (!isFinishing() && !isDestroyed()) {
                answerDialog.show();
                if (answerDialog.getWindow() != null) {
                    answerDialog.getWindow().setBackgroundDrawableResource(R.drawable.board_background);
                }
                // Force black message text so it's readable on all phone themes
                android.widget.TextView msg = answerDialog.findViewById(android.R.id.message);
                if (msg != null) msg.setTextColor(Color.BLACK);
            }
        }, 1200);
    }


    private void displayCharacters() {
        gridCharacters.removeAllViews();
        gridCharacters.setBackgroundColor(Color.TRANSPARENT);
        gridCharacters.setPadding(0, dp(1), 0, dp(5));
        gridCharacters.post(() -> {
            int columns = 8;
            int totalWidth = gridCharacters.getWidth();
            int spacing = dp(2);
            int size = (totalWidth - spacing * (columns - 1)) / columns;
            List<Boolean> currentStates = isOpponentScreen ? opponentCardStates : playerCardStates;
            for (int i = 0; i < characters.size(); i++) {
                CardView card = createCard(
                        characters.get(i),
                        i,
                        size,
                        currentStates.get(i),
                        spacing
                );
                card.setAlpha(0f);
                card.animate()
                        .alpha(1f)
                        .setStartDelay(i * 30L)
                        .setDuration(600)
                        .start();
                gridCharacters.addView(card);
            }
        });
    }

    private CardView createCard(
            GameCharacter c,
            int index,
            int size,
            boolean isAlive,
            int spacing
            ) {

        CardView card = (CardView) getLayoutInflater().inflate(R.layout.item_character_card, null);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = size;
        params.height = (int) (size * 1.15f);
        params.setMargins(spacing / 2, spacing / 2, spacing / 2, spacing / 2);
        card.setLayoutParams(params);

        ImageView img = card.findViewById(R.id.imgCharacter);
        TextView tv = card.findViewById(R.id.tvCharacterName);

        img.setImageResource(c.imageResId);
        tv.setText(c.name);

        if (!isAlive) {
            card.setCardBackgroundColor(REMOVED_COLOR);
            img.setAlpha(0.3f);
            tv.setTextColor(Color.GRAY);
        }

        if (!isOpponentScreen) {
            card.setOnClickListener(v -> {
                if (!playerCardStates.get(index)){
                    return;
                }
                selectedImage = img;
                showBigImage();
            });
        }

        return card;
    }

    private void showBigImage() {
        ImageView img = new ImageView(this);
        img.setImageDrawable(selectedImage.getDrawable());
        img.setAdjustViewBounds(true);

        AlertDialog bigImageDialog = new AlertDialog.Builder(this)
                .setView(img)
                .setPositiveButton("סגור", null)
                .create();

        bigImageDialog.show();

        if (bigImageDialog.getWindow() != null) {
            bigImageDialog.getWindow().setBackgroundDrawableResource(R.drawable.board_background);
        }
    }

    private void switchScreenWithAnimation(
            Button btnSwitch,
            Button btnAsk,
            Button btnBack,
            Button btnFinal) {

        int fromX = isOpponentScreen ?-rootLayout.getWidth() : rootLayout.getWidth();

        TranslateAnimation anim =
                new TranslateAnimation(fromX, 0, 0, 0);
        anim.setDuration(300);
        rootLayout.startAnimation(anim);

        isOpponentScreen = !isOpponentScreen;

        showSideCharacter();
        updateBackgroundByScreen();
        updateButtonsByScreen(btnSwitch, btnAsk, btnFinal, btnBack);
        displayCharacters();
    }


    private void updateBackgroundByScreen() {
        rootLayout.setBackgroundColor(
                isOpponentScreen ? OPPONENT_BG_COLOR : PLAYER_BG_COLOR
        );

        if (tvScreenTitle != null) {
            tvScreenTitle.setText(isOpponentScreen ? "מסך יריב" : "מסך שלי");
            tvScreenTitle.setTextColor(Color.parseColor("#3E2723"));
        }
        View boardContainer = (View) gridCharacters.getParent();
        boardContainer.setBackgroundResource(R.drawable.board_framed_gradient);

    }


    private void updateButtonsByScreen(
            Button btnSwitch,
            Button btnAsk,
            Button btnBack,
            Button btnFinal) {

        // 2. קביעת צבע טקסט
        int btnTextColor = Color.BLACK;

        // רשימת הכפתורים לעדכון
        Button[] allButtons = {btnSwitch, btnAsk, btnBack, btnFinal};

        for (Button btn : allButtons) {
            btn.setTextColor(btnTextColor);

            // במצב רגיל: מחזירים את ה-Drawable המקורי שלך מה-XML
            btn.setBackgroundResource(R.drawable.button_frame);

            // מוודאים שאין Tint שמסתיר את הצבע המקורי של ה-Drawable
            btn.setBackgroundTintList(null);

        }

        // 3. לוגיקת החלפת הטקסט והנראות
        btnSwitch.setText(isOpponentScreen ? "למסך שלי" : "למסך יריב");

        int visibility = isOpponentScreen ? View.GONE : View.VISIBLE;
        btnAsk.setVisibility(visibility);
        btnFinal.setVisibility(visibility);
        btnBack.setVisibility(visibility);

        if (btnHint != null) btnHint.setVisibility(visibility);
        if (btnPeek != null) btnPeek.setVisibility(visibility);
    }

    private void showSideCharacter() {

        CardView cardPlayer = findViewById(R.id.cardPlayerCharacter);
        CardView cardOpponent = findViewById(R.id.cardOpponentCharacter);

        // שליפת ה-LinearLayout הפנימי בתוך ה-CardView
        LinearLayout innerPlayerLayout = (LinearLayout) cardPlayer.getChildAt(0);
        LinearLayout innerOpponentLayout = (LinearLayout) cardOpponent.getChildAt(0);

        // הגדרת צבעים וטקסט
        int textColor = Color.BLACK;

        if (isOpponentScreen) {
            cardPlayer.setVisibility(View.GONE);
            cardOpponent.setVisibility(View.VISIBLE);


            // מצב רגיל: החזרת ה-drawable המקורי
            innerOpponentLayout.setBackgroundResource(R.drawable.card_side_background);
            innerOpponentLayout.setBackgroundTintList(null);


            TextView tvOpponentName = findViewById(R.id.tvOpponentName);
            tvOpponentName.setText("???");
            tvOpponentName.setTextColor(textColor);

        } else {
            cardPlayer.setVisibility(View.VISIBLE);
            cardOpponent.setVisibility(View.GONE);


            // מצב רגיל: החזרת ה-drawable המקורי מה-XML
            innerPlayerLayout.setBackgroundResource(R.drawable.card_side_background);
            innerPlayerLayout.setBackgroundTintList(null);


            // עדכון טקסט הכותרת הסטטי "הדמות שלך:" (הבן הראשון ב-Layout)
            if (innerPlayerLayout.getChildAt(0) instanceof TextView) {
                ((TextView) innerPlayerLayout.getChildAt(0)).setTextColor(textColor);
            }

            imgSideCharacter.setImageResource(playerCharacter.imageResId);

            // שם הדמות באותיות קטנות (לפי ההנחיה)
            tvSideCharacterName.setText(playerCharacter.name.toLowerCase());
            tvSideCharacterName.setTextColor(textColor);
        }
    }

    private void showFinalGuessDialog() {

        List<GameCharacter> availableCharacters = new ArrayList<>();
        for (int i = 0; i < characters.size(); i++) {
            if (playerCardStates.get(i)) {
                availableCharacters.add(characters.get(i));
            }
        }

        android.widget.ListAdapter adapter = new android.widget.ArrayAdapter<GameCharacter>(
                this, R.layout.list_item_guess, availableCharacters) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item_guess, parent, false);
                }
                GameCharacter c = getItem(position);
                ((ImageView) convertView.findViewById(R.id.imgGuessThumbnail)).setImageResource(c.imageResId);
                ((TextView) convertView.findViewById(R.id.tvGuessName)).setText(c.name);
                return convertView;
            }
        };

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("מי הדמות?")
                .setAdapter(adapter, (d, which) -> {
                    stopTimer();
                    handleFinalGuess(availableCharacters.get(which));
                })
                .setNegativeButton("ביטול", null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.board_background);
        }
        dialog.show();
    }

    private void handleFinalGuess(GameCharacter selected) {
        if (opponentCharacter == null) {
            if (isOnline) {
                Toast.makeText(this, "היריב טרם בחר דמות!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        boolean isCorrect = selected.name.equals(opponentCharacter.name);

        if (isOnline && !gameEnded) {
            gameEnded = true;
            String winnerRole = isCorrect ? (isPlayer1 ? "p1" : "p2") : (isPlayer1 ? "p2" : "p1");

            // Bug 5: write all game-end fields atomically in a single update so the
            // opponent's listener always sees all fields together (prevents reading
            // gameOutcome before lastGuessName is written, which caused "null" display).
            Map<String, Object> gameEndData = new HashMap<>();
            gameEndData.put("gameOutcome", winnerRole);
            gameEndData.put("lastGuessBy", isPlayer1 ? "p1" : "p2");
            gameEndData.put("lastGuessName", selected.name);
            gameEndData.put("lastGuessCorrect", isCorrect);

            FirebaseDatabase.getInstance().getReference("rooms").child(roomId)
                    .updateChildren(gameEndData);
        }

        uiManager.handleGameEnd(
                isCorrect ? GameUIManager.GameEndReason.YOU_GUESSED_RIGHT : GameUIManager.GameEndReason.YOU_GUESSED_WRONG,
                opponentCharacter.name,
                isCorrect ? victoryOverlay : loseOverlay,
                isCorrect ? tvVictoryTitle : tvLoseTitle,
                isCorrect ? tvVictoryCharacter : tvLoseCharacter,
                findViewById(R.id.buttonContainer),
                oldMoney -> {
                    finalizeGameAndLeave(oldMoney);
                }
        );

    }

    private void handleRemoteGameEnd(boolean iWon) {
        if (isFinishing() || isDestroyed()) return;
        
        GameUIManager.GameEndReason reason = iWon ? 
            GameUIManager.GameEndReason.YOU_GUESSED_RIGHT : 
            GameUIManager.GameEndReason.YOU_GUESSED_WRONG;

        uiManager.handleGameEnd(
                reason,
                opponentCharacter != null ? opponentCharacter.name : "???",
                iWon ? victoryOverlay : loseOverlay,
                iWon ? tvVictoryTitle : tvLoseTitle,
                iWon ? tvVictoryCharacter : tvLoseCharacter,
                findViewById(R.id.buttonContainer),
                oldMoney -> {
                    finalizeGameAndLeave(oldMoney);
                }
        );
    }

    private void finalizeGameAndLeave(int oldMoney) {
        // Cancel force-close penalty since game ended normally
        MoneyManager.getInstance().cancelDisconnectPenalty();
        
        if (isOnline && mpManager != null) {
            String category = getIntent().getStringExtra("CATEGORY");
            mpManager.leaveRoom(category);
        }
        Intent intent = new Intent(GameActivity.this, MenuActivity.class);
        intent.putExtra("FROM_GAME", true);
        intent.putExtra("OLD_MONEY", oldMoney);
        startActivity(intent);
        finish();
    }
    private int dp(int dp) {
        return (int) (dp *
                getResources()
                        .getDisplayMetrics()
                        .density);
    }

    private void setButtonsEnabled(boolean enabled) {
        Button btnAsk = findViewById(R.id.btnAsk);
        Button btnFinalGuess = findViewById(R.id.btnFinalGuess);

        // נטרול הכפתורים כדי שלא יהיה ניתן ללחוץ עליהם בתור ה-ai
        btnAsk.setEnabled(enabled);
        btnFinalGuess.setEnabled(enabled);

        // אופציונלי: שינוי ויזואלי כדי שהשחקן יבין שהם חסומים
        float alpha = enabled ? 1.0f : 0.5f;
        btnAsk.setAlpha(alpha);
        btnFinalGuess.setAlpha(alpha);
    }


    private void updateOpponentBoard(Question q, boolean answer) {
        for (int i = 0; i < characters.size(); i++) {
            // אם הדמות לא מתאימה לתשובה שהשחקן נתן, ה-AI "מוריד" אותה בלוח שלו
            if (characters.get(i).hasAttribute(q.key) != answer) {
                opponentCardStates.set(i, false);
            }
        }
        // אם אנחנו כרגע במסך היריב, נרענן את התצוגה
        if (isOpponentScreen) {
            displayCharacters();
        }
    }

    private void updateCardVisualsInGrid(int index, boolean isAlive) {
        CardView card = (CardView) gridCharacters.getChildAt(index);
        if (card != null && card.getChildCount() > 0) {
            LinearLayout layout = (LinearLayout) card.getChildAt(0);
            ImageView img = (ImageView) layout.getChildAt(0);

            card.setCardBackgroundColor(isAlive ? NORMAL_COLOR : REMOVED_COLOR);
            img.setAlpha(isAlive ? 1f : 0.25f);
        }
        // Sync our board state to Firebase
        if (isOnline) {
            Map<String, Object> boardUpdate = new HashMap<>();
            boardUpdate.put(String.valueOf(index), isAlive);
            mpManager.updateGameState(isPlayer1 ? "p1Board" : "p2Board", boardUpdate);
        }
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
}
