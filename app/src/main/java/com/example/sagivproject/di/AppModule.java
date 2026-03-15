package com.example.sagivproject.di;

import android.app.AlarmManager;
import android.content.Context;

import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.services.IAuthService;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.IEmergencyService;
import com.example.sagivproject.services.IForumCategoriesService;
import com.example.sagivproject.services.IForumService;
import com.example.sagivproject.services.IImageService;
import com.example.sagivproject.services.IMedicationService;
import com.example.sagivproject.services.IMemoryGameService;
import com.example.sagivproject.services.IStatsService;
import com.example.sagivproject.services.ITipOfTheDayService;
import com.example.sagivproject.services.IUserService;
import com.example.sagivproject.services.impl.AuthServiceImpl;
import com.example.sagivproject.services.impl.EmergencyServiceImpl;
import com.example.sagivproject.services.impl.ForumCategoriesServiceImpl;
import com.example.sagivproject.services.impl.ForumServiceImpl;
import com.example.sagivproject.services.impl.ImageServiceImpl;
import com.example.sagivproject.services.impl.MedicationServiceImpl;
import com.example.sagivproject.services.impl.MemoryGameServiceImpl;
import com.example.sagivproject.services.impl.StatsServiceImpl;
import com.example.sagivproject.services.impl.TipOfTheDayServiceImpl;
import com.example.sagivproject.services.impl.UserServiceImpl;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * Hilt module that provides and binds dependencies for the application.
 * <p>
 * This module is installed in the {@link SingletonComponent}, ensuring that the provided
 * instances have a singleton scope throughout the application's lifecycle.
 * It provides core Firebase components, utility classes, and binds service interfaces
 * to their respective implementations.
 * </p>
 */
@Module
@InstallIn(SingletonComponent.class)
public abstract class AppModule {

    /**
     * Provides a singleton instance of {@link DatabaseReference}.
     *
     * @return The root reference to the Firebase Realtime Database.
     */
    @Provides
    @Singleton
    public static DatabaseReference provideDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Provides a singleton instance of {@link FirebaseDatabase}.
     *
     * @return The {@link FirebaseDatabase} instance.
     */
    @Provides
    @Singleton
    public static FirebaseDatabase provideFirebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }

    /**
     * Provides a singleton instance of {@link Gson} for JSON serialization/deserialization.
     *
     * @return A {@link Gson} instance.
     */
    @Provides
    @Singleton
    public static Gson provideGson() {
        return new Gson();
    }

    /**
     * Provides the system's {@link AlarmManager} service.
     *
     * @param context The application context.
     * @return The {@link AlarmManager} system service.
     */
    @Provides
    @Singleton
    public static AlarmManager provideAlarmManager(@ApplicationContext Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Binds the {@link IDatabaseService} interface to its implementation.
     */
    @Binds
    @Singleton
    public abstract IDatabaseService bindDatabaseService(DatabaseService databaseService);

    /**
     * Binds the {@link IAuthService} interface to its implementation.
     */
    @Binds
    @Singleton
    public abstract IAuthService bindAuthService(AuthServiceImpl authService);

    /**
     * Binds the {@link IUserService} interface to its implementation.
     */
    @Binds
    @Singleton
    public abstract IUserService bindUserService(UserServiceImpl userService);

    /**
     * Binds the {@link IMedicationService} interface to its implementation.
     */
    @Binds
    @Singleton
    public abstract IMedicationService bindMedicationService(MedicationServiceImpl medicationService);

    /**
     * Binds the {@link IStatsService} interface to its implementation.
     */
    @Binds
    @Singleton
    public abstract IStatsService bindStatsService(StatsServiceImpl statsService);

    /**
     * Binds the {@link IForumService} interface to its implementation.
     */
    @Binds
    @Singleton
    public abstract IForumService bindForumService(ForumServiceImpl forumService);

    /**
     * Binds the {@link IForumCategoriesService} interface to its implementation.
     */
    @Binds
    @Singleton
    public abstract IForumCategoriesService bindForumCategoryService(ForumCategoriesServiceImpl forumCategoryService);

    /**
     * Binds the {@link IMemoryGameService} interface to its implementation.
     */
    @Binds
    @Singleton
    public abstract IMemoryGameService bindGameService(MemoryGameServiceImpl gameService);

    /**
     * Binds the {@link IImageService} interface to its implementation.
     */
    @Binds
    @Singleton
    public abstract IImageService bindImageService(ImageServiceImpl imageService);

    /**
     * Binds the {@link ITipOfTheDayService} interface to its implementation.
     */
    @Binds
    @Singleton
    public abstract ITipOfTheDayService bindTipOfTheDayService(TipOfTheDayServiceImpl tipOfTheDayService);

    /**
     * Binds the {@link IEmergencyService} interface to its implementation.
     */
    @Binds
    @Singleton
    public abstract IEmergencyService bindEmergencyService(EmergencyServiceImpl emergencyService);
}
