package com.example.sagivproject.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.Card;
import com.example.sagivproject.models.GameRoom;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

/**
 * An interface that defines the contract for operations related to the online memory game.
 * <p>
 * This service manages matchmaking (finding or creating rooms), game board initialization,
 * real-time state synchronization (card flips, scores, turns), and automatic forfeit handling.
 * </p>
 */
public interface IMemoryGameService {
    /**
     * Finds an available game room with a "waiting" status or creates a new one if none exist.
     *
     * @param user     The {@link User} looking for a game session.
     * @param callback The callback invoked with the joined or newly created {@link GameRoom}.
     */
    void findOrCreateRoom(User user, DatabaseCallback<GameRoom> callback);

    /**
     * Retrieves all active game rooms from the database with real-time updates.
     *
     * @param callback The callback invoked with the complete list of rooms.
     */
    void getAllRoomsRealtime(@NonNull DatabaseCallback<List<GameRoom>> callback);

    /**
     * Attaches a listener to monitor the status changes of a specific game room (e.g., transitions to "playing" or "finished").
     *
     * @param roomId   The unique identifier of the game room.
     * @param callback The {@link IRoomStatusCallback} to handle status events.
     */
    void listenToRoomStatus(@NonNull String roomId, @NonNull IRoomStatusCallback callback);

    /**
     * Removes the status listener from a specific game room to stop receiving updates.
     *
     * @param roomId The unique identifier of the game room.
     */
    void removeRoomListener(@NonNull String roomId);

    /**
     * Cancels or deletes a game room from the database.
     *
     * @param roomId   The unique identifier of the room to cancel.
     * @param callback An optional callback invoked upon completion.
     */
    void cancelRoom(@NonNull String roomId, @Nullable DatabaseCallback<Void> callback);

    /**
     * Initializes the game board by distributing cards and setting the first turn.
     *
     * @param roomId       The unique identifier of the room.
     * @param cards        The list of {@link Card} objects representing the board layout.
     * @param firstTurnUid The UID of the player who will start the game.
     * @param callback     An optional callback invoked upon completion.
     */
    void initGameBoard(String roomId, List<Card> cards, String firstTurnUid, @Nullable DatabaseCallback<Void> callback);

    /**
     * Attaches a real-time listener to a specific game session to synchronize moves and scores.
     *
     * @param roomId   The unique identifier of the game room.
     * @param callback The callback invoked with the current {@link GameRoom} state on every change.
     */
    void listenToGame(String roomId, DatabaseCallback<GameRoom> callback);

    /**
     * Detaches the real-time listener from a specific game session.
     *
     * @param roomId The unique identifier of the game room.
     */
    void stopListeningToGame(String roomId);

    /**
     * Updates a specific top-level field in a game room's database record.
     *
     * @param roomId The unique identifier of the room.
     * @param field  The name of the field to modify.
     * @param value  The new value to assign to the field.
     */
    void updateRoomField(String roomId, String field, Object value);

    /**
     * Increments the score counter for a specific player within a game session.
     *
     * @param roomId    The unique identifier of the room.
     * @param playerUid The UID of the player whose score should be increased.
     * @param callback  An optional callback invoked upon completion.
     */
    void incrementScore(String roomId, String playerUid, @Nullable DatabaseCallback<Void> callback);

    /**
     * Updates the interactive state (revealed/matched) of a specific card on the board.
     *
     * @param roomId   The unique identifier of the room.
     * @param index    The position of the card in the board list.
     * @param revealed Whether the card is currently face-up.
     * @param matched  Whether the card has been successfully matched.
     */
    void updateCardStatus(String roomId, int index, boolean revealed, boolean matched);

    /**
     * Sets whether the game session is currently processing a match check to prevent concurrent clicks.
     *
     * @param roomId       The unique identifier of the room.
     * @param isProcessing true if an animation or validation is in progress.
     */
    void setProcessing(String roomId, boolean isProcessing);

    /**
     * Updates the daily cognitive statistics for a user after a game completes.
     *
     * @param uid   The unique identifier of the user.
     * @param isWin true if the user was the winner of the session.
     */
    void updateDailyMemoryStats(String uid, boolean isWin);

    /**
     * Configures a Firebase "onDisconnect" operation to automatically forfeit the match if a player loses connection.
     *
     * @param roomId      The unique identifier of the room.
     * @param opponentUid The UID of the opponent who should be declared the winner.
     */
    void setupForfeitOnDisconnect(String roomId, String opponentUid);

    /**
     * Cancels any pending "onDisconnect" forfeit operations for a specific room.
     *
     * @param roomId The unique identifier of the room.
     */
    void removeForfeitOnDisconnect(String roomId);

    /**
     * Callback interface for receiving significant game room status transitions.
     */
    interface IRoomStatusCallback {
        /**
         * Invoked when the room is fully populated and the match begins.
         *
         * @param room The {@link GameRoom} object in its "playing" state.
         */
        void onRoomStarted(GameRoom room);

        /**
         * Invoked if the room is deleted (e.g., host canceled or opponent left while waiting).
         */
        void onRoomDeleted();

        /**
         * Invoked when the game session concludes naturally.
         *
         * @param room The final state of the {@link GameRoom}.
         */
        void onRoomFinished(GameRoom room);

        /**
         * Invoked if an error occurs while monitoring the room.
         *
         * @param e The exception encountered.
         */
        void onFailed(Exception e);
    }
}
