package com.example.sagivproject.services.impl;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.bases.BaseDatabaseService;
import com.example.sagivproject.models.Card;
import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.models.GameRoom;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IMemoryGameService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

/**
 * Implementation of the {@link IMemoryGameService} interface.
 * <p>
 * This class manages the complex real-time state synchronization for the 1-on-1 online memory game.
 * It handles:
 * <ul>
 * <li>Atomic matchmaking (finding or creating rooms).</li>
 * <li>Board initialization and card shuffling synchronization.</li>
 * <li>Turn-based logic and score tracking.</li>
 * <li>Automatic forfeit handling using Firebase's {@code onDisconnect}.</li>
 * <li>Daily cognitive performance statistics updates.</li>
 * </ul>
 * </p>
 */
public class MemoryGameServiceImpl extends BaseDatabaseService<GameRoom> implements IMemoryGameService {
    private static final String ROOMS_PATH = "rooms";
    private static final String USERS_PATH = "users";
    private static final String TAG = "GameServiceImpl";

    private static final String FIELD_STATUS = "status";
    private static final String FIELD_CARDS = "cards";
    private static final String FIELD_CURRENT_TURN_UID = "currentTurnUid";
    private static final String FIELD_IS_REVEALED = "isRevealed";
    private static final String FIELD_IS_MATCHED = "isMatched";
    private static final String FIELD_PROCESSING_MATCH = "processingMatch";
    private static final String FIELD_WINNER_UID = "winnerUid";
    private static final String FIELD_DAILY_STATS = "dailyStats";

    private static final String STATUS_WAITING = "waiting";
    private static final String STATUS_PLAYING = "playing";
    private static final String STATUS_FINISHED = "finished";
    private static final String VALUE_DRAW = "draw";

    private final DatabaseReference roomsReference;
    private final DatabaseReference usersReference;
    private final Map<String, ValueEventListener> roomStatusListeners = new ConcurrentHashMap<>();
    private ValueEventListener activeGameListener;

    /**
     * Constructs a new MemoryGameServiceImpl.
     * @param firebaseDatabase The {@link FirebaseDatabase} instance.
     */
    @Inject
    public MemoryGameServiceImpl(FirebaseDatabase firebaseDatabase) {
        super(ROOMS_PATH, GameRoom.class);
        this.roomsReference = firebaseDatabase.getReference(ROOMS_PATH);
        this.usersReference = firebaseDatabase.getReference(USERS_PATH);
    }

    /**
     * Uses a Firebase transaction to atomically find a waiting room or create a new one.
     * @param user The user seeking a match.
     * @param callback Result callback.
     */
    @Override
    public void findOrCreateRoom(User user, DatabaseCallback<GameRoom> callback) {
        if (user == null) {
            callback.onFailed(new IllegalArgumentException("User cannot be null"));
            return;
        }
        roomsReference.runTransaction(new Transaction.Handler() {
            private String roomIdForUser = null;

            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                for (MutableData roomData : currentData.getChildren()) {
                    try {
                        GameRoom room = roomData.getValue(GameRoom.class);
                        if (room != null && STATUS_WAITING.equals(room.getStatus()) && room.getPlayer2Uid() == null) {
                            room.setPlayer2Uid(user.getId());
                            room.setStatus(STATUS_PLAYING);
                            roomData.setValue(room);
                            roomIdForUser = room.getId();
                            return Transaction.success(currentData);
                        }
                    } catch (DatabaseException e) {
                        Log.e(TAG, "Error deserializing room", e);
                    }
                }
                String newRoomId = roomsReference.push().getKey();
                if (newRoomId == null) return Transaction.abort();
                GameRoom newRoom = new GameRoom(newRoomId, user);
                currentData.child(newRoomId).setValue(newRoom);
                roomIdForUser = newRoomId;
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (error != null) {
                    callback.onFailed(error.toException());
                } else if (!committed) {
                    callback.onFailed(new Exception("שגיאה במציאת חדר."));
                } else {
                    GameRoom finalRoom = snapshot.child(roomIdForUser).getValue(GameRoom.class);
                    if (finalRoom != null) callback.onCompleted(finalRoom);
                    else callback.onFailed(new Exception("שגיאה בנתוני החדר."));
                }
            }
        });
    }

    @Override
    public void getAllRoomsRealtime(@NonNull DatabaseCallback<List<GameRoom>> callback) {
        roomsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<GameRoom> roomList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    GameRoom room = child.getValue(GameRoom.class);
                    if (room != null) roomList.add(room);
                }
                callback.onCompleted(roomList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailed(error.toException());
            }
        });
    }

    /**
     * Attaches a listener to track room status transitions (e.g. from waiting to playing).
     * @param roomId Room ID.
     * @param callback Status events handler.
     */
    @Override
    public void listenToRoomStatus(@NonNull String roomId, @NonNull IRoomStatusCallback callback) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onRoomDeleted();
                    return;
                }
                GameRoom room = snapshot.getValue(GameRoom.class);
                if (room == null) return;
                if (STATUS_PLAYING.equals(room.getStatus())) callback.onRoomStarted(room);
                else if (STATUS_FINISHED.equals(room.getStatus())) callback.onRoomFinished(room);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailed(error.toException());
            }
        };
        roomsReference.child(roomId).addValueEventListener(listener);
        roomStatusListeners.put(roomId, listener);
    }

    @Override
    public void removeRoomListener(@NonNull String roomId) {
        ValueEventListener listener = roomStatusListeners.remove(roomId);
        if (listener != null) roomsReference.child(roomId).removeEventListener(listener);
    }

    @Override
    public void cancelRoom(@NonNull String roomId, @Nullable DatabaseCallback<Void> callback) {
        roomsReference.child(roomId).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                GameRoom room = currentData.getValue(GameRoom.class);
                if (room != null && STATUS_WAITING.equals(room.getStatus()) && room.getPlayer2Uid() == null) {
                    currentData.setValue(null);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (callback != null) {
                    if (error != null) callback.onFailed(error.toException());
                    else callback.onCompleted(null);
                }
            }
        });
    }

    @Override
    public void initGameBoard(String roomId, List<Card> cards, String firstTurnUid, DatabaseCallback<Void> callback) {
        Map<String, Object> roomUpdates = new HashMap<>();
        roomUpdates.put(FIELD_CURRENT_TURN_UID, firstTurnUid);
        roomUpdates.put(FIELD_STATUS, STATUS_PLAYING);
        roomsReference.child(roomId).child(FIELD_CARDS).setValue(cards)
                .addOnSuccessListener(aVoid -> roomsReference.child(roomId).updateChildren(roomUpdates, (error, ref) -> {
                    if (callback != null) {
                        if (error != null) callback.onFailed(error.toException());
                        else callback.onCompleted(null);
                    }
                }))
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailed(e);
                });
    }

    /**
     * Sets up a real-time listener for the active game room state.
     * @param roomId Room ID.
     * @param callback Handler for state updates.
     */
    @Override
    public void listenToGame(String roomId, DatabaseCallback<GameRoom> callback) {
        stopListeningToGame(roomId);
        activeGameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onCompleted(null);
                    return;
                }
                try {
                    GameRoom room = snapshot.getValue(GameRoom.class);
                    callback.onCompleted(room);
                } catch (DatabaseException e) {
                    callback.onFailed(e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailed(error.toException());
            }
        };
        roomsReference.child(roomId).addValueEventListener(activeGameListener);
    }

    @Override
    public void stopListeningToGame(String roomId) {
        if (activeGameListener != null) {
            roomsReference.child(roomId).removeEventListener(activeGameListener);
            activeGameListener = null;
        }
    }

    @Override
    public void updateRoomField(String roomId, String field, Object value) {
        roomsReference.child(roomId).child(field).setValue(value);
    }

    /**
     * Atomic transaction to increment a player's score.
     * @param roomId Room identifier.
     * @param playerUid Player to reward.
     * @param callback Optional result callback.
     */
    @Override
    public void incrementScore(String roomId, String playerUid, @Nullable DatabaseCallback<Void> callback) {
        roomsReference.child(roomId).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                GameRoom room = mutableData.getValue(GameRoom.class);
                if (room == null) return Transaction.success(mutableData);
                if (playerUid.equals(room.getPlayer1Uid()))
                    room.setPlayer1Score(room.getPlayer1Score() + 1);
                else if (playerUid.equals(room.getPlayer2Uid()))
                    room.setPlayer2Score(room.getPlayer2Score() + 1);
                mutableData.setValue(room);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (callback != null) {
                    if (error != null) callback.onFailed(error.toException());
                    else callback.onCompleted(null);
                }
            }
        });
    }

    @Override
    public void updateCardStatus(String roomId, int index, boolean revealed, boolean matched) {
        if (index < 0) return;
        DatabaseReference cardRef = roomsReference.child(roomId).child(FIELD_CARDS).child(String.valueOf(index));
        cardRef.child(FIELD_IS_REVEALED).setValue(revealed);
        cardRef.child(FIELD_IS_MATCHED).setValue(matched);
    }

    @Override
    public void setProcessing(String roomId, boolean isProcessing) {
        updateRoomField(roomId, FIELD_PROCESSING_MATCH, isProcessing);
    }

    /**
     * Updates daily memory game stats for a user using an atomic transaction.
     * @param uid User ID.
     * @param isWin true if session was won.
     */
    @Override
    public void updateDailyMemoryStats(String uid, boolean isWin) {
        if (uid == null || uid.isEmpty() || VALUE_DRAW.equals(uid)) return;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        usersReference.child(uid).child(FIELD_DAILY_STATS).child(today).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                DailyStats stats = currentData.getValue(DailyStats.class);
                if (stats == null) stats = new DailyStats();
                stats.addMemoryGamePlayed();
                if (isWin) {
                    stats.addMemoryWin();
                }
                currentData.setValue(stats);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
            }
        });
    }

    /**
     * Configures forfeit on disconnect. If connection is lost, room status becomes finished
     * and the opponent is declared the winner.
     */
    @Override
    public void setupForfeitOnDisconnect(String roomId, String opponentUid) {
        DatabaseReference roomRef = roomsReference.child(roomId);
        roomRef.child(FIELD_STATUS).onDisconnect().setValue(STATUS_FINISHED);
        roomRef.child(FIELD_WINNER_UID).onDisconnect().setValue(opponentUid);
    }

    @Override
    public void removeForfeitOnDisconnect(String roomId) {
        DatabaseReference roomRef = roomsReference.child(roomId);
        roomRef.child(FIELD_STATUS).onDisconnect().cancel();
        roomRef.child(FIELD_WINNER_UID).onDisconnect().cancel();
    }
}