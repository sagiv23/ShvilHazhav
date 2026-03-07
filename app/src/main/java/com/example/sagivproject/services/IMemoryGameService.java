package com.example.sagivproject.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.Card;
import com.example.sagivproject.models.GameRoom;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

/**
 * An interface that defines the contract for operations related to the memory game.
 * <p>
 * This service manages game rooms, player matching, game state updates, and statistics for the memory game.
 * </p>
 */
public interface IMemoryGameService {
    /**
     * Finds an available game room or creates a new one for the user.
     *
     * @param user     The user looking for a game.
     * @param callback The callback to be invoked with the joined or created room.
     */
    void findOrCreateRoom(User user, DatabaseCallback<GameRoom> callback);

    /**
     * Retrieves all active game rooms with real-time updates.
     *
     * @param callback The callback to be invoked with the list of rooms.
     */
    void getAllRoomsRealtime(@NonNull DatabaseCallback<List<GameRoom>> callback);

    /**
     * Listens for status changes in a specific game room (e.g., started, finished).
     *
     * @param roomId   The ID of the room to listen to.
     * @param callback The callback for room status events.
     */
    void listenToRoomStatus(@NonNull String roomId, @NonNull IRoomStatusCallback callback);

    /**
     * Stops listening for status changes in a specific game room.
     *
     * @param roomId The ID of the room.
     */
    void removeRoomListener(@NonNull String roomId);

    /**
     * Cancels or deletes a game room.
     *
     * @param roomId   The ID of the room to cancel.
     * @param callback An optional callback to be invoked upon completion.
     */
    void cancelRoom(@NonNull String roomId, @Nullable DatabaseCallback<Void> callback);

    /**
     * Initializes the game board for a room.
     *
     * @param roomId       The ID of the room.
     * @param cards        The list of cards to place on the board.
     * @param firstTurnUid The UID of the player who gets the first turn.
     * @param callback     An optional callback to be invoked upon completion.
     */
    void initGameBoard(String roomId, List<Card> cards, String firstTurnUid, @Nullable DatabaseCallback<Void> callback);

    /**
     * Listens for real-time game state updates in a specific room.
     *
     * @param roomId   The ID of the room.
     * @param callback The callback to be invoked with the updated game room object.
     */
    void listenToGame(String roomId, DatabaseCallback<GameRoom> callback);

    /**
     * Stops listening for game state updates in a specific room.
     *
     * @param roomId The ID of the room.
     */
    void stopListeningToGame(String roomId);

    /**
     * Updates a specific field in a game room's database record.
     *
     * @param roomId The ID of the room.
     * @param field  The name of the field to update.
     * @param value  The new value for the field.
     */
    void updateRoomField(String roomId, String field, Object value);

    /**
     * Increments the score for a specific player in a game room.
     *
     * @param roomId    The ID of the room.
     * @param playerUid The UID of the player whose score should be incremented.
     * @param callback  An optional callback to be invoked upon completion.
     */
    void incrementScore(String roomId, String playerUid, @Nullable DatabaseCallback<Void> callback);

    /**
     * Updates the revealed/matched status of a card at a specific index.
     *
     * @param roomId   The ID of the room.
     * @param index    The index of the card in the board list.
     * @param revealed Whether the card is currently flipped/revealed.
     * @param matched  Whether the card has been matched.
     */
    void updateCardStatus(String roomId, int index, boolean revealed, boolean matched);

    /**
     * Sets whether the game room is currently processing an action (e.g., checking for a match).
     *
     * @param roomId       The ID of the room.
     * @param isProcessing True if processing, false otherwise.
     */
    void setProcessing(String roomId, boolean isProcessing);

    /**
     * Updates the daily statistics for a user after a memory game session.
     *
     * @param uid   The user's ID.
     * @param isWin True if the user won the game, false otherwise.
     */
    void updateDailyMemoryStats(String uid, boolean isWin);

    /**
     * Sets up a forfeit mechanism that triggers if the opponent disconnects.
     *
     * @param roomId      The ID of the room.
     * @param opponentUid The UID of the opponent.
     */
    void setupForfeitOnDisconnect(String roomId, String opponentUid);

    /**
     * Removes the forfeit mechanism for a specific room.
     *
     * @param roomId The ID of the room.
     */
    void removeForfeitOnDisconnect(String roomId);

    /**
     * A callback interface for room status events.
     */
    interface IRoomStatusCallback {
        /**
         * Called when the room has two players and is ready to start.
         *
         * @param room The game room object.
         */
        void onRoomStarted(GameRoom room);

        /**
         * Called when the room is deleted (e.g., opponent canceled).
         */
        void onRoomDeleted();

        /**
         * Called when the game in the room is finished.
         *
         * @param room The final state of the game room.
         */
        void onRoomFinished(GameRoom room);

        /**
         * Called when an error occurs while listening to room status.
         *
         * @param e The exception that occurred.
         */
        void onFailed(Exception e);
    }
}
