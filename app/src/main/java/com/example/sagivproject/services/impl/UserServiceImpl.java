package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.bases.BaseDatabaseService;
import com.example.sagivproject.models.User;
import com.example.sagivproject.models.enums.UserRole;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IUserService;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import javax.inject.Inject;

public class UserServiceImpl extends BaseDatabaseService<User> implements IUserService {

    @Inject
    public UserServiceImpl() {
        super("users", User.class);
    }

    @Override
    public String generateUserId() {
        return super.generateId();
    }

    @Override
    public void createNewUser(@NonNull User user, @Nullable DatabaseCallback<Void> callback) {
        super.create(user, callback);
    }

    @Override
    public void getUser(@NonNull String uid, @NonNull DatabaseCallback<User> callback) {
        super.get(uid, callback);
    }

    @Override
    public void getUserList(@NonNull DatabaseCallback<List<User>> callback) {
        super.getAll(callback);
    }

    @Override
    public void deleteUser(@NonNull String uid, @Nullable DatabaseCallback<Void> callback) {
        super.delete(uid, callback);
    }

    @Override
    public void getUserByEmailAndPassword(@NonNull String email, @NonNull String password, @NonNull DatabaseCallback<User> callback) {
        getAll(new DatabaseCallback<>() {
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
                callback.onFailed(e);
            }
        });
    }

    @Override
    public void checkIfEmailExists(@NonNull String email, @NonNull DatabaseCallback<Boolean> callback) {
        getAll(new DatabaseCallback<>() {
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
                callback.onFailed(e);
            }
        });
    }

    @Override
    public void updateUser(@NonNull User user, @Nullable DatabaseCallback<Void> callback) {
        UnaryOperator<User> updateFunction = oldUser -> user;
        super.update(user.getId(), updateFunction, new DatabaseCallback<>() {
            @Override
            public void onCompleted(User updatedUser) {
                if (callback != null) callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) callback.onFailed(e);
            }
        });
    }

    @Override
    public void updateUserRole(@NonNull String uid, @NonNull UserRole role, @Nullable DatabaseCallback<Void> callback) {
        UnaryOperator<User> updateFunction = user -> {
            user.setRole(role);
            return user;
        };
        super.update(uid, updateFunction, new DatabaseCallback<>() {
            @Override
            public void onCompleted(User user) {
                if (callback != null) callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) callback.onFailed(e);
            }
        });
    }
}
