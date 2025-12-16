package com.example.sagivproject.services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.GameRoom;
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

    /// tag for logging
    /// @see Log
    private static final String TAG = "DatabaseService";

    /// paths for different data types in the database
    /// @see DatabaseService#readData(String)
    private static final String USERS_PATH = "users",
            FORUM_PATH = "forum",
            ROOMS_PATH = "rooms";

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
                Log.e(TAG, "Error getting data", task.getException());
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
                Log.e(TAG, "Error getting data", task.getException());
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
                    Log.e(TAG, "Transaction failed", error.toException());
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

    // region Rooms Section

    /// callback interface for room status realtime updates
    /// used to notify when a room starts playing or is deleted
    /// @see GameRoom
    public interface RoomStatusCallback {
        /// called when the room status changes to "playing"
        /// @param room the updated room object
        void onRoomStarted(GameRoom room);

        /// called when the room is deleted from the database
        void onRoomDeleted();

        /// called when the listener fails
        /// @param e the exception
        void onFailed(Exception e);
    }

    /// create a new game room and save it in the database
    /// @param user the user creating the room (player1)
    /// @param callback callback that returns the created GameRoom
    /// @see GameRoom
    /// @see DatabaseCallback
    public void createRoom(@NotNull User user,
                           @NotNull DatabaseCallback<GameRoom> callback) {
        String roomId = generateNewId(ROOMS_PATH);
        GameRoom room = new GameRoom(roomId, user);

        writeData(ROOMS_PATH + "/" + roomId, room, new DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                callback.onCompleted(room);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }


    public void findOrCreateRoom(User user, DatabaseCallback<GameRoom> callback) {
        String newRoomId = generateNewId(ROOMS_PATH);

        readData(ROOMS_PATH).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {

                for (MutableData roomData : currentData.getChildren()) {
                    GameRoom room = roomData.getValue(GameRoom.class);

                    if (room != null
                            && "waiting".equals(room.getStatus())
                            && room.getPlayer2() == null) {

                        room.setPlayer2(user);
                        room.setStatus("playing");
                        roomData.setValue(room);
                        return Transaction.success(currentData);
                    }
                }

                GameRoom newRoom = new GameRoom(newRoomId, user);
                currentData.child(newRoomId).setValue(newRoom);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (!committed || error != null) {
                    callback.onFailed(error != null ? error.toException() : new Exception("Match failed"));
                    return;
                }

                // מציאת החדר שבו המשתמש נמצא
                for (DataSnapshot snap : snapshot.getChildren()) {
                    GameRoom room = snap.getValue(GameRoom.class);
                    if (room != null &&
                            (user.getUid().equals(room.getPlayer1().getUid()) ||
                                    (room.getPlayer2() != null && user.getUid().equals(room.getPlayer2().getUid())))) {

                        callback.onCompleted(room);
                        return;
                    }
                }
            }
        });
    }

    /// listen for realtime updates on a specific room
    /// used to detect when the game starts or the room is deleted
    /// @param roomId the id of the room to listen to
    /// @param callback realtime room status callback
    /// @see ValueEventListener
    /// @see RoomStatusCallback
    public void listenToRoomStatus(@NotNull String roomId,
                                   @NotNull RoomStatusCallback callback) {
        readData(ROOMS_PATH + "/" + roomId)
                .addValueEventListener(new ValueEventListener() {

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
                });
    }

    /// cancel a room and remove it from the database
    /// usually called when the room creator leaves
    /// @param roomId the id of the room to delete
    /// @param callback callback for operation result
    /// @see DatabaseCallback
    public void cancelRoom(@NotNull String roomId,
                           @Nullable DatabaseCallback<Void> callback) {
        deleteData(ROOMS_PATH + "/" + roomId, callback);
    }

    // endregion Rooms Section
}