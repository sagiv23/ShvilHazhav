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
 * This service manages the entire lifecycle of a game, from matchmaking to completion.
 * It includes methods for finding/creating rooms, managing game state, handling player actions,
 * and setting up real-time listeners.
 * </p>
 */
public interface IGameService {
    /**
     * Finds an existing waiting room or creates a new one if none is available.
     *
     * @param user     The user who wants to join or create a game room.
     * @param callback A callback that returns the matched or newly created GameRoom.
     */
    void findOrCreateRoom(User user, DatabaseCallback<GameRoom> callback);

    /**
     * Listens to all game rooms in real-time.
     *
     * @param callback The callback that will receive the updated list of rooms.
     */
    void getAllRoomsRealtime(@NonNull DatabaseCallback<List<GameRoom>> callback);

    /**
     * Listens in real-time to changes in a specific room's status (e.g., waiting, playing, finished).
     *
     * @param roomId   The ID of the room to listen to.
     * @param callback A callback to notify about room status changes.
     */
    void listenToRoomStatus(@NonNull String roomId, @NonNull IRoomStatusCallback callback);

    /**
     * Removes a previously registered room status listener.
     *
     * @param roomId   The ID of the room.
     */
    void removeRoomListener(@NonNull String roomId);

    /**
     * Cancels and deletes a game room from the database, typically if the host leaves before it starts.
     *
     * @param roomId   The ID of the room to cancel.
     * @param callback An optional callback for success or failure.
     */
    void cancelRoom(@NonNull String roomId, @Nullable DatabaseCallback<Void> callback);

    /**
     * Initializes the game board data for a room.
     *
     * @param roomId       The ID of the game room.
     * @param cards        The shuffled list of cards for the game.
     * @param firstTurnUid The UID of the player who will take the first turn.
     * @param callback     An optional callback for success or failure.
     */
    void initGameBoard(String roomId, List<Card> cards, String firstTurnUid, @Nullable DatabaseCallback<Void> callback);

    /**
     * Listens in real-time to all changes in a game room's state.
     *
     * @param roomId   The ID of the game room.
     * @param callback A callback that receives the updated {@link GameRoom} object on every change.
     */
    void listenToGame(String roomId, DatabaseCallback<GameRoom> callback);

    /**
     * Stops listening to real-time game updates for a specific room.
     *
     * @param roomId The ID of the game room.
     */
    void stopListeningToGame(String roomId);

    /**
     * Updates a single field inside a game room.
     *
     * @param roomId The ID of the game room.
     * @param field  The name of the field to update.
     * @param value  The new value for the field.
     */
    void updateRoomField(String roomId, String field, Object value);

    /**
     * Atomically increments the score for a player in a given game room.
     *
     * @param roomId    The ID of the room where the score should be incremented.
     * @param playerUid The UID of the player whose score is to be incremented.
     * @param callback  An optional callback to handle success or failure of the operation.
     */
    void incrementScore(String roomId, String playerUid, @Nullable DatabaseCallback<Void> callback);

    /**
     * Updates the revealed and matched state of a specific card on the game board.
     *
     * @param roomId   The ID of the game room.
     * @param index    The index of the card in the `cards` list.
     * @param revealed Whether the card is currently revealed.
     * @param matched  Whether the card has been successfully matched.
     */
    void updateCardStatus(String roomId, int index, boolean revealed, boolean matched);

    /**
     * Sets the processing state of the game, used to prevent moves while a match is being checked.
     *
     * @param roomId       The ID of the game room.
     * @param isProcessing True if the game is currently processing a move, false otherwise.
     */
    void setProcessing(String roomId, boolean isProcessing);

    /**
     * Increments the win counter of a user.
     *
     * @param uid The UID of the winning user.
     */
    void addUserWin(String uid);

    /**
     * Defines automatic forfeit behavior if a player disconnects unexpectedly.
     *
     * @param roomId      The ID of the game room.
     * @param opponentUid The UID of the opponent who will win by forfeit.
     */
    void setupForfeitOnDisconnect(String roomId, String opponentUid);

    /**
     * Cancels the previously defined onDisconnect forfeit actions for a room.
     *
     * @param roomId The ID of the game room.
     */
    void removeForfeitOnDisconnect(String roomId);

    /**
     * A callback interface for monitoring the high-level status of a game room (e.g., started, deleted).
     */
    interface IRoomStatusCallback {
        /**
         * Called when an opponent joins a waiting room, and the game status changes to "playing".
         *
         * @param room The game room that has started.
         */
        void onRoomStarted(GameRoom room);

        /**
         * Called when a waiting game room has been deleted (e.g., by the host).
         */
        void onRoomDeleted();

        /**
         * Called when the game room status changes to "finished".
         *
         * @param room The finished game room state.
         */
        void onRoomFinished(GameRoom room);

        /**
         * Called when an error occurs while monitoring the game room.
         *
         * @param e The exception that occurred.
         */
        void onFailed(Exception e);
    }
}
