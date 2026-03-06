package com.example.sagivproject.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.Card;
import com.example.sagivproject.models.GameRoom;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

/**
 * An interface for memory game operations.
 */
public interface IMemoryGameService {
    void findOrCreateRoom(User user, DatabaseCallback<GameRoom> callback);

    void getAllRoomsRealtime(@NonNull DatabaseCallback<List<GameRoom>> callback);

    void listenToRoomStatus(@NonNull String roomId, @NonNull IRoomStatusCallback callback);

    void removeRoomListener(@NonNull String roomId);

    void cancelRoom(@NonNull String roomId, @Nullable DatabaseCallback<Void> callback);

    void initGameBoard(String roomId, List<Card> cards, String firstTurnUid, @Nullable DatabaseCallback<Void> callback);

    void listenToGame(String roomId, DatabaseCallback<GameRoom> callback);

    void stopListeningToGame(String roomId);

    void updateRoomField(String roomId, String field, Object value);

    void incrementScore(String roomId, String playerUid, @Nullable DatabaseCallback<Void> callback);

    void updateCardStatus(String roomId, int index, boolean revealed, boolean matched);

    void setProcessing(String roomId, boolean isProcessing);

    void addUserWin(String uid);

    void setupForfeitOnDisconnect(String roomId, String opponentUid);

    void removeForfeitOnDisconnect(String roomId);

    /**
     * Logs a wrong move for a user in the memory game.
     */
    void logWrongMove(String uid);

    interface IRoomStatusCallback {
        void onRoomStarted(GameRoom room);

        void onRoomDeleted();

        void onRoomFinished(GameRoom room);

        void onFailed(Exception e);
    }
}
