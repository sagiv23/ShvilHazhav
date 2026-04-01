package com.example.sagivproject.di;

import android.app.AlarmManager;
import android.content.Context;

import com.example.sagivproject.services.IAdapterService;
import com.example.sagivproject.services.IAuthService;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.IDialogService;
import com.example.sagivproject.services.IEmergencyService;
import com.example.sagivproject.services.IFallDetectionService;
import com.example.sagivproject.services.IForumCategoriesService;
import com.example.sagivproject.services.IForumService;
import com.example.sagivproject.services.IImageService;
import com.example.sagivproject.services.IMedicationService;
import com.example.sagivproject.services.IMemoryGameService;
import com.example.sagivproject.services.IStatsService;
import com.example.sagivproject.services.ITipOfTheDayService;
import com.example.sagivproject.services.IUserService;
import com.example.sagivproject.services.impl.AdapterService;
import com.example.sagivproject.services.impl.AuthServiceImpl;
import com.example.sagivproject.services.impl.DatabaseService;
import com.example.sagivproject.services.impl.DialogService;
import com.example.sagivproject.services.impl.EmergencyServiceImpl;
import com.example.sagivproject.services.impl.FallDetectionManager;
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
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.android.scopes.ActivityScoped;
import dagger.hilt.components.SingletonComponent;

/**
 * Hilt module that provides and binds dependencies for the application.
 * <p>
 * This class contains both singleton-scoped and activity-scoped dependency definitions.
 * It provides core Firebase components, utility classes, and binds service interfaces
 * to their respective implementation classes.
 * </p>
 */
@Module
@InstallIn(SingletonComponent.class)
public abstract class AppModule {

    /**
     * Provides a singleton instance of {@link DatabaseReference}.
     * @return The root reference to the Firebase Realtime Database.
     */
    @Provides
    @Singleton
    public static DatabaseReference provideDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Provides a singleton instance of {@link FirebaseDatabase}.
     * @return The {@link FirebaseDatabase} instance.
     */
    @Provides
    @Singleton
    public static FirebaseDatabase provideFirebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }

    /**
     * Provides a singleton instance of {@link Gson} for JSON serialization and deserialization.
     * @return A {@link Gson} instance.
     */
    @Provides
    @Singleton
    public static Gson provideGson() {
        return new Gson();
    }

    /**
     * Provides the system's {@link AlarmManager} service.
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
     * @param databaseService The {@link DatabaseService} implementation.
     * @return The bound interface.
     */
    @Binds
    @Singleton
    public abstract IDatabaseService bindDatabaseService(DatabaseService databaseService);

    /**
     * Binds the {@link IAuthService} interface to its implementation.
     * @param authService The {@link AuthServiceImpl} implementation.
     * @return The bound interface.
     */
    @Binds
    @Singleton
    public abstract IAuthService bindAuthService(AuthServiceImpl authService);

    /**
     * Binds the {@link IUserService} interface to its implementation.
     * @param userService The {@link UserServiceImpl} implementation.
     * @return The bound interface.
     */
    @Binds
    @Singleton
    public abstract IUserService bindUserService(UserServiceImpl userService);

    /**
     * Binds the {@link IMedicationService} interface to its implementation.
     * @param medicationService The {@link MedicationServiceImpl} implementation.
     * @return The bound interface.
     */
    @Binds
    @Singleton
    public abstract IMedicationService bindMedicationService(MedicationServiceImpl medicationService);

    /**
     * Binds the {@link IStatsService} interface to its implementation.
     * @param statsService The {@link StatsServiceImpl} implementation.
     * @return The bound interface.
     */
    @Binds
    @Singleton
    public abstract IStatsService bindStatsService(StatsServiceImpl statsService);

    /**
     * Binds the {@link IForumService} interface to its implementation.
     * @param forumService The {@link ForumServiceImpl} implementation.
     * @return The bound interface.
     */
    @Binds
    @Singleton
    public abstract IForumService bindForumService(ForumServiceImpl forumService);

    /**
     * Binds the {@link IForumCategoriesService} interface to its implementation.
     * @param forumCategoryService The {@link ForumCategoriesServiceImpl} implementation.
     * @return The bound interface.
     */
    @Binds
    @Singleton
    public abstract IForumCategoriesService bindForumCategoryService(ForumCategoriesServiceImpl forumCategoryService);

    /**
     * Binds the {@link IMemoryGameService} interface to its implementation.
     * @param gameService The {@link MemoryGameServiceImpl} implementation.
     * @return The bound interface.
     */
    @Binds
    @Singleton
    public abstract IMemoryGameService bindGameService(MemoryGameServiceImpl gameService);

    /**
     * Binds the {@link IImageService} interface to its implementation.
     * @param imageService The {@link ImageServiceImpl} implementation.
     * @return The bound interface.
     */
    @Binds
    @Singleton
    public abstract IImageService bindImageService(ImageServiceImpl imageService);

    /**
     * Binds the {@link ITipOfTheDayService} interface to its implementation.
     * @param tipOfTheDayService The {@link TipOfTheDayServiceImpl} implementation.
     * @return The bound interface.
     */
    @Binds
    @Singleton
    public abstract ITipOfTheDayService bindTipOfTheDayService(TipOfTheDayServiceImpl tipOfTheDayService);

    /**
     * Binds the {@link IEmergencyService} interface to its implementation.
     * @param emergencyService The {@link EmergencyServiceImpl} implementation.
     * @return The bound interface.
     */
    @Binds
    @Singleton
    public abstract IEmergencyService bindEmergencyService(EmergencyServiceImpl emergencyService);

    /**
     * Binds the {@link IFallDetectionService} interface to its implementation.
     * @param fallDetectionManager The {@link FallDetectionManager} implementation.
     * @return The bound interface.
     */
    @Binds
    @Singleton
    public abstract IFallDetectionService bindFallDetectionService(FallDetectionManager fallDetectionManager);

    /**
     * Nested Hilt module for activity-scoped bindings.
     * <p>
     * This module must be installed in {@link ActivityComponent} because the services it binds
     * are scoped to the activity lifecycle.
     * </p>
     */
    @Module
    @InstallIn(ActivityComponent.class)
    public interface ActivityBindingsModule {
        /**
         * Binds the {@link IDialogService} interface to its implementation.
         * @param dialogService The {@link DialogService} implementation.
         * @return The bound interface.
         */
        @Binds
        @ActivityScoped
        IDialogService bindDialogService(DialogService dialogService);

        /**
         * Binds the {@link IAdapterService} interface to its implementation.
         * @param adapterService The {@link AdapterService} implementation.
         * @return The bound interface.
         */
        @Binds
        @ActivityScoped
        IAdapterService bindAdapterService(AdapterService adapterService);
    }
}