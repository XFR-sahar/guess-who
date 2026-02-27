package com.example.fixed_guess_who;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MultiplayerManager {

    private final DatabaseReference mDatabase;
    private String roomId;
    private String playerId;
    private boolean isPlayer1;
    private boolean matchFoundTriggered = false;
    private MultiplayerCallback callback;

    public interface MultiplayerCallback {
        void onMatchFound(String roomId, boolean isPlayer1);
        void onGameStateChanged(DataSnapshot snapshot);
        void onOpponentDisconnected();
    }

    public MultiplayerManager(String playerId, MultiplayerCallback callback) {
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        this.playerId = playerId;
        this.callback = callback;
    }

    public void seekGame(String category) {
        matchFoundTriggered = false;
        mDatabase.child("available_rooms").child(category).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Room found, join it
                    for (DataSnapshot roomSnap : snapshot.getChildren()) {
                        roomId = roomSnap.getKey();
                        isPlayer1 = false;
                        joinRoom(roomId, category);
                        break;
                    }
                } else {
                    // No room found, create one
                    createRoom(category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void createRoom(String category) {
        roomId = mDatabase.child("rooms").push().getKey();
        isPlayer1 = true;

        Map<String, Object> roomData = new HashMap<>();
        roomData.put("player1", playerId);
        roomData.put("p1Connected", true);
        roomData.put("status", "waiting");
        roomData.put("category", category);

        mDatabase.child("rooms").child(roomId).setValue(roomData);
        mDatabase.child("available_rooms").child(category).child(roomId).setValue(true);
        
        mDatabase.child("rooms").child(roomId).child("p1Connected").onDisconnect().setValue(false);
        mDatabase.child("available_rooms").child(category).child(roomId).onDisconnect().removeValue();

        listenToRoom();
    }

    private void joinRoom(String roomId, String category) {
        this.roomId = roomId;
        mDatabase.child("rooms").child(roomId).child("p2Connected").setValue(playerId);
        mDatabase.child("rooms").child(roomId).child("p2ConnectedFlag").setValue(true);
        mDatabase.child("rooms").child(roomId).child("status").setValue("started");
        mDatabase.child("available_rooms").child(category).child(roomId).removeValue();

        if (callback != null && !matchFoundTriggered) {
            matchFoundTriggered = true;
            callback.onMatchFound(roomId, false);
        }
        mDatabase.child("rooms").child(roomId).child("p2ConnectedFlag").onDisconnect().setValue(false);
        listenToRoom();
    }

    private void listenToRoom() {
        mDatabase.child("rooms").child(roomId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Room was deleted by someone (probably both left)
                    return;
                }

                String oppConnKey = isPlayer1 ? "p2ConnectedFlag" : "p1Connected";
                Boolean oppConnected = snapshot.child(oppConnKey).getValue(Boolean.class);
                if (oppConnected != null && !oppConnected) {
                    if (callback != null) callback.onOpponentDisconnected();
                }

                String status = snapshot.child("status").getValue(String.class);
                if ("started".equals(status) && isPlayer1 && !matchFoundTriggered) {
                    matchFoundTriggered = true;
                    if (callback != null) callback.onMatchFound(roomId, true);
                }

                if (callback != null) {
                    callback.onGameStateChanged(snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void joinExistingRoom(String roomId, boolean isPlayer1) {
        this.roomId = roomId;
        this.isPlayer1 = isPlayer1;
        listenToRoom();
    }

    public void updateGameState(String key, Object value) {
        if (roomId != null) {
            mDatabase.child("rooms").child(roomId).child(key).setValue(value);
        }
    }

    public void leaveRoom(String category) {
        if (roomId != null) {
            mDatabase.child("rooms").child(roomId).removeValue();
            if (category != null) {
                mDatabase.child("available_rooms").child(category).child(roomId).removeValue();
            }
        }
    }
}
