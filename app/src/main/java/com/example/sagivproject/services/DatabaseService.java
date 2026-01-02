package com.example.sagivproject.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.Card;
import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.GameRoom;
import com.example.sagivproject.models.ImageData;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/// a service to interact with the Firebase Realtime Database.
/// this class is a singleton, use getInstance() to get an instance of this class
/// @see #getInstance()
/// @see FirebaseDatabase
public class DatabaseService {
    /// paths for different data types in the database
    /// @see DatabaseService#readData(String)
    private static final String USERS_PATH = "users",
            FORUM_PATH = "forum",
            ROOMS_PATH = "rooms",
            IMAGES_PATH = "images";

    /// callback interface for database operations
    /// @param <T> the type of the object to return
    /// @see DatabaseCallback#onCompleted(Object)
    /// @see DatabaseCallback#onFailed(Exception)
    public interface DatabaseCallback<T> {
        /// called when the operation is completed successfully
        public void onCompleted(T object);

        /// called when the operation fails with an exception
        public void onFailed(Exception e);
    }

    /// the instance of this class
    /// @see #getInstance()
    private static DatabaseService instance;

    /// the reference to the database
    /// @see DatabaseReference
    /// @see FirebaseDatabase#getReference()
    private final DatabaseReference databaseReference;

    /// the listener for realtime updates on the active game
    /// @see DatabaseService#listenToGame(String, DatabaseCallback)
    /// @see DatabaseCallback
    private ValueEventListener activeGameListener;

    /// use getInstance() to get an instance of this class
    /// @see DatabaseService#getInstance()
    private DatabaseService() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    /// get an instance of this class
    /// @return an instance of this class
    /// @see DatabaseService
    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }


    // region private generic methods
    // to write and read data from the database

    /// write data to the database at a specific path
    /// @param path the path to write the data to
    /// @param data the data to write (can be any object, but must be serializable, i.e. must have a default constructor and all fields must have getters and setters)
    /// @param callback the callback to call when the operation is completed
    /// @see DatabaseCallback
    private void writeData(@NotNull final String path, @NotNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
        readData(path).setValue(data, (error, ref) -> {
            if (error != null) {
                if (callback == null) return;
                callback.onFailed(error.toException());
            } else {
                if (callback == null) return;
                callback.onCompleted(null);
            }
        });
    }

    /// remove data from the database at a specific path
    /// @param path the path to remove the data from
    /// @param callback the callback to call when the operation is completed
    /// @see DatabaseCallback
    private void deleteData(@NotNull final String path, @Nullable final DatabaseCallback<Void> callback) {
        readData(path).removeValue((error, ref) -> {
            if (error != null) {
                if (callback == null) return;
                callback.onFailed(error.toException());
            } else {
                if (callback == null) return;
                callback.onCompleted(null);
            }
        });
    }

    /// read data from the database at a specific path
    /// @param path the path to read the data from
    /// @return a DatabaseReference object to read the data from
    /// @see DatabaseReference

    private DatabaseReference readData(@NotNull final String path) {
        return databaseReference.child(path);
    }


    /// get data from the database at a specific path
    /// @param path the path to get the data from
    /// @param clazz the class of the object to return
    /// @param callback the callback to call when the operation is completed
    /// @see DatabaseCallback
    /// @see Class
    private <T> void getData(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<T> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onFailed(task.getException());
                return;
            }
            T data = task.getResult().getValue(clazz);
            callback.onCompleted(data);
        });
    }

    /// get a list of data from the database at a specific path
    /// @param path the path to get the data from
    /// @param clazz the class of the objects to return
    /// @param callback the callback to call when the operation is completed
    private <T> void getDataList(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<List<T>> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onFailed(task.getException());
                return;
            }
            List<T> tList = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                T t = dataSnapshot.getValue(clazz);
                tList.add(t);
            });

            callback.onCompleted(tList);
        });
    }

    /// generate a new id for a new object in the database
    /// @param path the path to generate the id for
    /// @return a new id for the object
    /// @see String
    /// @see DatabaseReference#push()

    private String generateNewId(@NotNull final String path) {
        return databaseReference.child(path).push().getKey();
    }

    /// run a transaction on the data at a specific path </br>
    /// good for incrementing a value or modifying an object in the database
    /// @param path the path to run the transaction on
    /// @param clazz the class of the object to return
    /// @param function the function to apply to the current value of the data
    /// @param callback the callback to call when the operation is completed
    /// @see DatabaseReference#runTransaction(Transaction.Handler)
    private <T> void runTransaction(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull UnaryOperator<T> function, @NotNull final DatabaseCallback<T> callback) {
        readData(path).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                T currentValue = currentData.getValue(clazz);
                if (currentValue == null) {
                    currentValue = function.apply(null);
                } else {
                    currentValue = function.apply(currentValue);
                }
                currentData.setValue(currentValue);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    callback.onFailed(error.toException());
                    return;
                }
                T result = currentData != null ? currentData.getValue(clazz) : null;
                callback.onCompleted(result);
            }
        });

    }

    // endregion of private methods for reading and writing data

    // public methods to interact with the database

    // region User Section

    /// generate a new id for a new user in the database
    /// @return a new id for the user
    /// @see #generateNewId(String)
    /// @see User
    public String generateUserId() {
        return generateNewId(USERS_PATH);
    }

    /// create a new user in the database
    /// @param user the user object to create
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive void
    ///            if the operation fails, the callback will receive an exception
    /// @see DatabaseCallback
    /// @see User
    public void createNewUser(@NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        writeData(USERS_PATH + "/" + user.getUid(), user, callback);
    }

    /// get a user from the database
    /// @param uid the id of the user to get
    /// @param callback the callback to call when the operation is completed
    ///               the callback will receive the user object
    ///             if the operation fails, the callback will receive an exception
    /// @see DatabaseCallback
    /// @see User
    public void getUser(@NotNull final String uid, @NotNull final DatabaseCallback<User> callback) {
        getData(USERS_PATH + "/" + uid, User.class, callback);
    }

    /// get all the users from the database
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive a list of user objects
    ///            if the operation fails, the callback will receive an exception
    /// @see DatabaseCallback
    /// @see List
    /// @see User
    public void getUserList(@NotNull final DatabaseCallback<List<User>> callback) {
        getDataList(USERS_PATH, User.class, callback);
    }

    /// delete a user from the database
    /// @param uid the user id to delete
    /// @param callback the callback to call when the operation is completed
    public void deleteUser(@NotNull final String uid, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(USERS_PATH + "/" + uid, callback);
    }

    /// get a user by email and password
    /// @param email the email of the user
    /// @param password the password of the user
    /// @param callback the callback to call when the operation is completed
    ///            the callback will receive the user object
    ///          if the operation fails, the callback will receive an exception
    /// @see DatabaseCallback
    /// @see User
    public void getUserByEmailAndPassword(@NotNull final String email, @NotNull final String password, @NotNull final DatabaseCallback<User> callback) {
        getUserList(new DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                for (User user : users) {
                    if (Objects.equals(user.getEmail(), email) && Objects.equals(user.getPassword(), password)) {
                        callback.onCompleted(user);
                        return;
                    }
                }
                callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {

            }
        });
    }

    /// check if an email already exists in the database
    /// @param email the email to check
    /// @param callback the callback to call when the operation is completed
    public void checkIfEmailExists(@NotNull final String email, @NotNull final DatabaseCallback<Boolean> callback) {
        getUserList(new DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                for (User user : users) {
                    if (Objects.equals(user.getEmail(), email)) {
                        callback.onCompleted(true);
                        return;
                    }
                }
                callback.onCompleted(false);
            }

            @Override
            public void onFailed(Exception e) {

            }
        });
    }

    /// update a user in the database
    /// @param user the user object to update
    /// @param callback the callback to call when the operation is completed
    public void updateUser(@NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        runTransaction(USERS_PATH + "/" + user.getUid(), User.class, currentUser -> user, new DatabaseCallback<User>() {
            @Override
            public void onCompleted(User object) {
                if (callback != null) {
                    callback.onCompleted(null);
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) {
                    callback.onFailed(e);
                }
            }
        });
    }

    /// update only the admin status of a user
    /// @param uid user id
    /// @param isAdmin new admin value (true/false)
    /// @param callback result callback
    public void updateUserAdminStatus(@NotNull final String uid, boolean isAdmin, @Nullable final DatabaseCallback<Void> callback) {
        readData(USERS_PATH + "/" + uid + "/isAdmin")
                .setValue(isAdmin, (error, ref) -> {
                    if (error != null) {
                        if (callback != null) callback.onFailed(error.toException());
                    } else {
                        if (callback != null) callback.onCompleted(null);
                    }
                });
    }

    // endregion User Section

    // region Medication Section

    /// create a new medication in the database
    /// @param uid the id of the user
    /// @param medication the medication object to create
    /// @param callback the callback to call when the operation is completed
    public void createNewMedication(@NotNull final String uid, @NotNull final Medication medication, @Nullable final DatabaseCallback<Void> callback) {
        writeData(USERS_PATH + "/" + uid + "/medications/" + medication.getId(), medication, callback);
    }

    /// get a medication from the database
    /// @param uid user's id
    /// @param medicationId the id of the medication to get
    /// @param callback the callback to call when the operation is completed
    public void getMedication(@NotNull final String uid, @NotNull final String medicationId, @NotNull final DatabaseCallback<Medication> callback) {
        getData(USERS_PATH + "/" + uid + "/medications/" + medicationId, Medication.class, callback);
    }

    /// get all the medications of a specific user
    /// @param uid the id of the user
    /// @param callback the callback
    public void getUserMedicationList(@NotNull final String uid, @NotNull final DatabaseCallback<List<Medication>> callback) {
        getDataList(USERS_PATH + "/" + uid + "/medications", Medication.class, callback);
    }

    /// generate a new id for a medication under a specific user
    /// @return a new id for the medication
    public String generateMedicationId(@NotNull final String uid) {
        return generateNewId(USERS_PATH + "/" + uid + "/medications");
    }

    /// delete a medication from the database
    /// @param uid user id
    /// @param medicationId id to delete
    /// @param callback callback
    public void deleteMedication(@NotNull final String uid, @NotNull final String medicationId, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(USERS_PATH + "/" + uid + "/medications/" + medicationId, callback);
    }

    /// update a medication in the database
    /// @param uid user id
    /// @param medication medication to update
    /// @param callback callback
    public void updateMedication(String uid, Medication medication, @Nullable DatabaseCallback<Void> callback) {
        writeData(USERS_PATH + "/" + uid + "/medications/" + medication.getId(), medication, callback);
    }

    // endregion Medication Section

    // region Forum Section

    /// generate a new id for a new forum message
    /// @return a new id for the forum message
    /// @see #generateNewId(String)
    /// @see ForumMessage
    public String generateForumMessageId() {
        return generateNewId(FORUM_PATH);
    }

    /// send a new message to the forum
    /// @param message the ForumMessage object to send
    /// @param callback the callback to call when the operation is completed
    ///                 the callback will receive void on success or an exception on fail
    /// @see DatabaseCallback
    /// @see ForumMessage
    public void sendForumMessage(ForumMessage message, DatabaseCallback<Void> callback) {
        writeData(FORUM_PATH + "/" + message.getMessageId(), message, callback);
    }

    /// get all forum messages in realtime (live updates)
    /// @param callback the callback that will receive a List<ForumMessage> when data changes
    ///                 if the operation fails, the callback will receive an exception
    /// @see DatabaseCallback
    /// @see ForumMessage
    /// @see ValueEventListener
    public void getForumMessagesRealtime(DatabaseCallback<List<ForumMessage>> callback) {
        readData(FORUM_PATH)
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<ForumMessage> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            ForumMessage msg = child.getValue(ForumMessage.class);
                            list.add(msg);
                        }
                        callback.onCompleted(list);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onFailed(error.toException());
                    }
                });
    }

    /// delete a specific forum message from the database
    /// @param messageId the id of the forum message to delete
    /// @param callback the callback to call when the operation is completed
    ///                 the callback will receive void on success or an exception on fail
    /// @see DatabaseCallback
    public void deleteForumMessage(@NotNull final String messageId, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(FORUM_PATH + "/" + messageId, callback);
    }

    // endregion Forum Section

    // region Game Section

    /// callback interface for realtime room status updates
    /// used to notify listeners when a room starts playing, is deleted,
    /// or when an error occurs while listening
    /// @see GameRoom
    public interface RoomStatusCallback {
        /// called when the room status changes to "playing"
        /// usually means that both players are connected and the game can start
        /// @param room the updated GameRoom object
        void onRoomStarted(GameRoom room);

        /// called when the room no longer exists in the database
        /// usually happens when the room is cancelled or deleted
        void onRoomDeleted();

        /// called when the listener fails due to a database error
        /// @param e the exception describing the failure
        void onFailed(Exception e);
    }

    /// find an existing waiting room or create a new one if none is available
    /// tries to match the given user with another waiting player atomically
    /// if a waiting room is found:
    ///     - the user is set as player2
    ///     - the room status changes to "playing"
    /// if no waiting room exists:
    ///     - a new room is created with the user as player1
    /// @param user the user who wants to join or create a game room
    /// @param callback callback that returns the matched or newly created GameRoom
    ///                 or an exception if the transaction fails
    /// @see GameRoom
    /// @see Transaction
    public void findOrCreateRoom(User user, DatabaseCallback<GameRoom> callback) {
        String newRoomId = generateNewId(ROOMS_PATH);
        final String[] matchedRoomId = new String[1];

        readData(ROOMS_PATH).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                for (MutableData roomData : currentData.getChildren()) {
                    GameRoom room = roomData.getValue(GameRoom.class);

                    if (room != null && "waiting".equals(room.getStatus()) && room.getPlayer2() == null) {
                        room.setPlayer2(user);
                        room.setStatus("playing");
                        roomData.setValue(room);
                        matchedRoomId[0] = room.getRoomId();
                        return Transaction.success(currentData);
                    }
                }

                GameRoom newRoom = new GameRoom(newRoomId, user);
                currentData.child(newRoomId).setValue(newRoom);
                matchedRoomId[0] = newRoomId;
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (!committed || error != null) {
                    callback.onFailed(error != null ? error.toException() : new Exception("Match failed"));
                    return;
                }

                if (matchedRoomId[0] != null) {
                    GameRoom room = snapshot.child(matchedRoomId[0]).getValue(GameRoom.class);
                    callback.onCompleted(room);
                    return;
                }
            }
        });
    }

    /// listen in realtime to changes in a specific room status
    /// used mainly before the game starts, to detect:
    ///     - when the room status becomes "playing"
    ///     - when the room is deleted or cancelled
    /// @param roomId the id of the room to listen to
    /// @param callback callback to notify about room start, deletion or errors
    /// @return the ValueEventListener instance so it can later be removed
    /// @see RoomStatusCallback
    /// @see ValueEventListener
    public ValueEventListener listenToRoomStatus(
            @NotNull String roomId,
            @NotNull RoomStatusCallback callback) {
        ValueEventListener listener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onRoomDeleted();
                    return;
                }

                GameRoom room = snapshot.getValue(GameRoom.class);
                if (room == null) return;

                if ("playing".equals(room.getStatus())) {
                    callback.onRoomStarted(room);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailed(error.toException());
            }
        };

        readData(ROOMS_PATH + "/" + roomId).addValueEventListener(listener);
        return listener;
    }

    /// remove a previously registered room status listener
    /// should be called when leaving the waiting screen
    /// to prevent memory leaks and unnecessary updates
    /// @param roomId the id of the room
    /// @param listener the listener instance returned from listenToRoomStatus
    /// @see ValueEventListener
    public void removeRoomListener(@NotNull String roomId,
                                   @NotNull ValueEventListener listener) {
        readData(ROOMS_PATH + "/" + roomId).removeEventListener(listener);
    }

    /// cancel and delete a game room from the database
    /// usually called when a player leaves before the game starts
    /// @param roomId the id of the room to cancel
    /// @param callback optional callback for success or failure
    public void cancelRoom(@NotNull String roomId,
                           @Nullable DatabaseCallback<Void> callback) {
        deleteData(ROOMS_PATH + "/" + roomId, callback);
    }

    /// initialize the game board data for a room
    /// sets:
    ///     - the list of cards
    ///     - the UID of the player whose turn is first
    ///     - the room status to "playing"
    /// should be called once both players are connected
    /// @param roomId the id of the game room
    /// @param cards the shuffled list of cards for the game
    /// @param firstTurnUid the UID of the player who starts the game
    /// @param callback callback for success or failure
    /// @see Card
    public void initGameBoard(String roomId, List<Card> cards, String firstTurnUid, DatabaseCallback<Void> callback) {
        readData(ROOMS_PATH + "/" + roomId + "/cards").setValue(cards);
        readData(ROOMS_PATH + "/" + roomId + "/currentTurnUid").setValue(firstTurnUid);
        readData(ROOMS_PATH + "/" + roomId + "/status").setValue("playing",
                (error, ref) -> {
                    if (callback == null) return;
                    if (error != null) callback.onFailed(error.toException());
                    else callback.onCompleted(null);
                });
    }

    /// listen in realtime to all changes in a game room
    /// receives full GameRoom updates whenever any field changes
    /// only one active game listener is kept at a time
    /// if a listener already exists, it will be removed before adding a new one
    /// @param roomId the id of the game room
    /// @param callback callback that receives updated GameRoom objects
    /// @see GameRoom
    /// @see ValueEventListener
    public void listenToGame(String roomId, DatabaseCallback<GameRoom> callback) {
        // אם כבר יש מאזין פעיל, נסיר אותו קודם כדי למנוע כפל האזנות
        stopListeningToGame(roomId);

        activeGameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GameRoom room = snapshot.getValue(GameRoom.class);
                callback.onCompleted(room);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailed(error.toException());
            }
        };

        readData(ROOMS_PATH + "/" + roomId).addValueEventListener(activeGameListener);
    }

    /// stop listening to realtime game updates
    /// removes the active game listener if one exists
    /// should be called when leaving the game screen
    /// @param roomId the id of the game room
    public void stopListeningToGame(String roomId) {
        if (activeGameListener != null) {
            readData(ROOMS_PATH + "/" + roomId).removeEventListener(activeGameListener);
            activeGameListener = null;
        }
    }

    /// update a single field inside a game room
    /// useful for lightweight updates such as:
    ///     - currentTurnUid
    ///     - status
    ///     - winnerUid
    /// @param roomId the id of the game room
    /// @param field the field name to update
    /// @param value the new value for the field
    public void updateRoomField(String roomId, String field, Object value) {
        readData(ROOMS_PATH + "/" + roomId + "/" + field).setValue(value);
    }

    /// update the reveal and match state of a specific card in the game board
    /// typically called after a player flips or matches cards
    /// @param roomId the id of the game room
    /// @param index the index of the card in the cards list
    /// @param revealed whether the card is currently revealed
    /// @param matched whether the card has been successfully matched
    /// @see Card
    public void updateCardStatus(String roomId, int index, boolean revealed, boolean matched) {
        readData(ROOMS_PATH + "/" + roomId + "/cards/" + index + "/isRevealed").setValue(revealed);
        readData(ROOMS_PATH + "/" + roomId + "/cards/" + index + "/isMatched").setValue(matched);
    }

    /// set the processing state of the game
    /// used to prevent players from acting while a match is being evaluated
    /// @param roomId the id of the game room
    /// @param isProcessing true if the game is currently processing a move
    public void setProcessing(String roomId, boolean isProcessing) {
        updateRoomField(roomId, "processingMatch", isProcessing);
    }

    /// increment the win counter of a user
    /// uses a transaction to safely increase the value
    /// ignored if the uid is null, empty or represents a draw
    /// @param uid the UID of the winning user
    public void addUserWin(String uid) {
        if (uid == null || uid.isEmpty() || uid.equals("draw")) return;

        runTransaction(USERS_PATH + "/" + uid + "/countWins", Integer.class,
                currentWins -> (currentWins == null) ? 1 : currentWins + 1,
                new DatabaseCallback<Integer>() {
                    @Override public void onCompleted(Integer object) {}
                    @Override public void onFailed(Exception e) {}
                });
    }

    /// define automatic forfeit behavior when a player disconnects unexpectedly
    /// if the client disconnects:
    ///     - room status is set to "finished"
    ///     - the opponent is declared as the winner
    /// uses Firebase onDisconnect handlers
    /// @param roomId the id of the game room
    /// @param opponentUid the UID of the opponent who will win by forfeit
    public void setupForfeitOnDisconnect(String roomId, String opponentUid) {
        // הגדרת הערכים שישתנו ב-DB ברגע שהשרת מזהה ניתוק
        readData(ROOMS_PATH + "/" + roomId + "/status").onDisconnect().setValue("finished");
        readData(ROOMS_PATH + "/" + roomId + "/winnerUid").onDisconnect().setValue(opponentUid);
    }

    /// cancel previously defined onDisconnect forfeit actions
    /// should be called when the game ends normally
    /// to prevent incorrect forfeit handling
    /// @param roomId the id of the game room
    public void removeForfeitOnDisconnect(String roomId) {
        readData(ROOMS_PATH + "/" + roomId + "/status").onDisconnect().cancel();
        readData(ROOMS_PATH + "/" + roomId + "/winnerUid").onDisconnect().cancel();
    }

    // endregion Game Section

    // region ImageMedication section
    public void getAllImages(DatabaseCallback<List<ImageData>> callback) {
        getDataList(IMAGES_PATH, ImageData.class, callback);
    }
    public String generateImageId() {
        return generateNewId(IMAGES_PATH);
    }
    public void createImage(@NonNull ImageData image, @Nullable DatabaseCallback<Void> callback) {
        writeData(IMAGES_PATH + "/" + image.getId(), image, callback);
    }

    public void updateAllImages(List<ImageData> list, DatabaseCallback<Void> callback) {
        //נמחוק את כל התמונות הקיימות ונכתוב את הרשימה המעודכנת
        readData(IMAGES_PATH).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (int i = 0; i < list.size(); i++) {
                    ImageData img = list.get(i);
                    writeData(IMAGES_PATH + "/" + img.getId(), img, null);
                }
                if (callback != null) callback.onCompleted(null);
            } else if (callback != null) {
                callback.onFailed(task.getException());
            }
        });
    }

    //העלאת תמונות למסד נתונים - למחוק בסוף הפרויקט!

    public void getImage(
            @NotNull String imageId,
            @NotNull DatabaseCallback<ImageData> callback
    ) {
        getData(IMAGES_PATH + "/" + imageId, ImageData.class, callback);
    }
}