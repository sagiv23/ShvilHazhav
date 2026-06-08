package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.Card;
import com.example.sagivproject.models.GameRoom;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseCallback;
import com.example.sagivproject.services.IMemoryGameService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

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

    private static final String FIELD_STATUS = "status";
    private static final String FIELD_CARDS = "cards";
    private static final String FIELD_CURRENT_TURN_UID = "currentTurnUid";
    private static final String FIELD_IS_REVEALED = "isRevealed";
    private static final String FIELD_IS_MATCHED = "isMatched";
    private static final String FIELD_PROCESSING_MATCH = "processingMatch";
    private static final String FIELD_WINNER_UID = "winnerUid";

    private static final String STATUS_WAITING = "waiting";
    private static final String STATUS_PLAYING = "playing";
    private static final String STATUS_FINISHED = "finished";

    /**
     * Root database reference for game rooms.
     */
    private final DatabaseReference roomsReference;

    /**
     * Map of active status listeners for individual game rooms.
     */
    private final Map<String, ValueEventListener> roomStatusListeners = new ConcurrentHashMap<>();

    /**
     * Listener for the currently active game session state.
     */
    private ValueEventListener activeGameListener;

    /**
     * Global listener for monitoring all active rooms (Admin use).
     */
    private ValueEventListener allRoomsListener;

    /**
     * Constructs a new MemoryGameServiceImpl.
     *
     * @param firebaseDatabase The {@link FirebaseDatabase} instance.
     */
    @Inject
    public MemoryGameServiceImpl(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, ROOMS_PATH, GameRoom.class);
        this.roomsReference = readData(ROOMS_PATH);
    }

    /**
     * Uses a Firebase transaction to atomically find a waiting room or create a new one.
     * Optimizes room search by:
     * 1. Preventing a user from joining their own room.
     * 2. Cleaning up duplicate waiting rooms for the same user.
     * 3. Favoring joining an existing waiting room to speed up matchmaking.
     *
     * @param user     The user seeking a match.
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
                String userId = user.getId();
                MutableData myExistingWaitingRoom = null;
                MutableData joinableRoom = null;

                // Single pass to identify an available room and any existing room belonging to the user
                for (MutableData roomData : currentData.getChildren()) {
                    GameRoom room = roomData.getValue(GameRoom.class);
                    if (room == null || !STATUS_WAITING.equals(room.getStatus())) continue;

                    if (userId.equals(room.getPlayer1Uid())) {
                        myExistingWaitingRoom = roomData;
                    } else if (room.getPlayer2Uid() == null && joinableRoom == null) {
                        joinableRoom = roomData;
                    }
                }

                // 1. Prioritize joining an existing room created by another player
                if (joinableRoom != null) {
                    // Clean up current user's old waiting room if it exists to keep database tidy
                    if (myExistingWaitingRoom != null) {
                        myExistingWaitingRoom.setValue(null);
                    }

                    GameRoom roomToJoin = joinableRoom.getValue(GameRoom.class);
                    if (roomToJoin != null) {
                        roomToJoin.setPlayer2Uid(userId);
                        roomToJoin.setStatus(STATUS_PLAYING);
                        joinableRoom.setValue(roomToJoin);
                        roomIdForUser = joinableRoom.getKey();
                        return Transaction.success(currentData);
                    }
                }

                // 2. If no other rooms are available, check if the user already has a waiting room
                if (myExistingWaitingRoom != null) {
                    roomIdForUser = myExistingWaitingRoom.getKey();
                    return Transaction.success(currentData);
                }

                // 3. No suitable rooms found; create a new waiting room
                String newRoomId = generateId();
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
                    callback.onFailed(new Exception("שגיאה בתהליך מציאת חדר."));
                } else {
                    if (roomIdForUser == null) {
                        callback.onFailed(new Exception("מזהה חדר לא נמצא."));
                        return;
                    }
                    GameRoom finalRoom = snapshot.child(roomIdForUser).getValue(GameRoom.class);
                    if (finalRoom != null) {
                        callback.onCompleted(finalRoom);
                    } else {
                        callback.onFailed(new Exception("נתוני החדר לא נמצאו לאחר העדכון."));
                    }
                }
            }
        });
    }

    @Override
    public void getAllRoomsRealtime(@NonNull DatabaseCallback<List<GameRoom>> callback) {
        stopAllRoomsRealtime();
        allRoomsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<GameRoom> roomList = StreamSupport.stream(snapshot.getChildren().spliterator(), false)
                        .map(child -> child.getValue(GameRoom.class))
                        .filter(Objects::nonNull)
                        .collect(java.util.stream.Collectors.toList());
                callback.onCompleted(roomList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailed(error.toException());
            }
        };
        roomsReference.addValueEventListener(allRoomsListener);
    }

    @Override
    public void stopAllRoomsRealtime() {
        if (allRoomsListener != null) {
            roomsReference.removeEventListener(allRoomsListener);
            allRoomsListener = null;
        }
    }

    /**
     * Attaches a listener to track room status transitions (e.g. from waiting to playing).
     *
     * @param roomId   Room ID.
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
        readData(ROOMS_PATH + "/" + roomId).addValueEventListener(listener);
        roomStatusListeners.put(roomId, listener);
    }

    @Override
    public void removeRoomListener(@NonNull String roomId) {
        ValueEventListener listener = roomStatusListeners.remove(roomId);
        if (listener != null) readData(ROOMS_PATH + "/" + roomId).removeEventListener(listener);
    }

    @Override
    public void cancelRoom(@NonNull String roomId, @Nullable DatabaseCallback<Void> callback) {
        runTransaction(ROOMS_PATH + "/" + roomId, room -> {
            if (room != null && STATUS_WAITING.equals(room.getStatus()) && room.getPlayer2Uid() == null) {
                return null; // Deletes the room node
            }
            return room;
        }, (callback == null) ? null : new DatabaseCallback<>() {
            @Override
            public void onCompleted(GameRoom result) {
                callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    @Override
    public void initGameBoard(String roomId, List<Card> cards, String firstTurnUid, DatabaseCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_CURRENT_TURN_UID, firstTurnUid);
        updates.put(FIELD_STATUS, STATUS_PLAYING);
        updates.put(FIELD_CARDS, cards);

        readData(ROOMS_PATH + "/" + roomId).updateChildren(updates, (error, ref) -> {
            if (callback != null) {
                if (error != null) callback.onFailed(error.toException());
                else callback.onCompleted(null);
            }
        });
    }

    /**
     * Sets up a real-time listener for the active game room state.
     *
     * @param roomId   Room ID.
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
        readData(ROOMS_PATH + "/" + roomId).addValueEventListener(activeGameListener);
    }

    @Override
    public void stopListeningToGame(String roomId) {
        if (activeGameListener != null) {
            readData(ROOMS_PATH + "/" + roomId).removeEventListener(activeGameListener);
            activeGameListener = null;
        }
    }

    @Override
    public void updateRoomField(String roomId, String field, Object value) {
        writeData(ROOMS_PATH + "/" + roomId + "/" + field, value, null);
    }

    /**
     * Atomic transaction to increment a player's score.
     *
     * @param roomId    Room identifier.
     * @param playerUid Player to reward.
     * @param callback  Optional result callback.
     */
    @Override
    public void incrementScore(String roomId, String playerUid, @Nullable DatabaseCallback<Void> callback) {
        runTransaction(ROOMS_PATH + "/" + roomId, room -> {
            if (room == null) return null;
            if (playerUid.equals(room.getPlayer1Uid()))
                room.setPlayer1Score(room.getPlayer1Score() + 1);
            else if (playerUid.equals(room.getPlayer2Uid()))
                room.setPlayer2Score(room.getPlayer2Score() + 1);
            return room;
        }, (callback == null) ? null : new DatabaseCallback<>() {
            @Override
            public void onCompleted(GameRoom result) {
                callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    @Override
    public void finishGame(String roomId, String winnerUid, @Nullable DatabaseCallback<Boolean> callback) {
        readData(ROOMS_PATH + "/" + roomId).runTransaction(new Transaction.Handler() {
            private boolean transitioned = false;

            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                transitioned = false;
                GameRoom room = currentData.getValue(GameRoom.class);
                if (room == null) return Transaction.success(currentData);

                // If stats already updated, do nothing and indicate no transition
                if (room.isStatsUpdated()) {
                    return Transaction.success(currentData);
                }

                // Mark as finished
                room.setStatus(STATUS_FINISHED);
                if (room.getWinnerUid() == null) {
                    room.setWinnerUid(winnerUid);
                }

                // Atomic check to ensure only one client triggers stats
                room.setStatsUpdated(true);
                transitioned = true;

                currentData.setValue(room);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {
                if (callback != null) {
                    if (error != null) callback.onFailed(error.toException());
                    else callback.onCompleted(committed && transitioned);
                }
            }
        });
    }

    @Override
    public void updateCardStatus(String roomId, int index, boolean revealed, boolean matched) {
        if (index < 0) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_IS_REVEALED, revealed);
        updates.put(FIELD_IS_MATCHED, matched);
        readData(ROOMS_PATH + "/" + roomId + "/" + FIELD_CARDS + "/" + index).updateChildren(updates);
    }

    @Override
    public void setProcessing(String roomId, boolean isProcessing) {
        updateRoomField(roomId, FIELD_PROCESSING_MATCH, isProcessing);
    }

    /**
     * Configures forfeit on disconnect. If connection is lost, room status becomes finished
     * and the opponent is declared the winner.
     */
    @Override
    public void setupForfeitOnDisconnect(String roomId, String opponentUid) {
        DatabaseReference roomRef = readData(ROOMS_PATH + "/" + roomId);
        roomRef.child(FIELD_STATUS).onDisconnect().setValue(STATUS_FINISHED);
        roomRef.child(FIELD_WINNER_UID).onDisconnect().setValue(opponentUid);
    }

    @Override
    public void removeForfeitOnDisconnect(String roomId) {
        DatabaseReference roomRef = readData(ROOMS_PATH + "/" + roomId);
        roomRef.child(FIELD_STATUS).onDisconnect().cancel();
        roomRef.child(FIELD_WINNER_UID).onDisconnect().cancel();
    }
}