package com.example.sagivproject.di;

import android.app.AlarmManager;
import android.content.Context;

import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.services.IAuthService;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.IForumCategoriesService;
import com.example.sagivproject.services.IForumService;
import com.example.sagivproject.services.IGameService;
import com.example.sagivproject.services.IImageService;
import com.example.sagivproject.services.IMedicationService;
import com.example.sagivproject.services.IStatsService;
import com.example.sagivproject.services.ITipOfTheDayService;
import com.example.sagivproject.services.IUserService;
import com.example.sagivproject.services.impl.AuthServiceImpl;
import com.example.sagivproject.services.impl.ForumCategoriesServiceImpl;
import com.example.sagivproject.services.impl.ForumServiceImpl;
import com.example.sagivproject.services.impl.GameServiceImpl;
import com.example.sagivproject.services.impl.ImageServiceImpl;
import com.example.sagivproject.services.impl.MedicationServiceImpl;
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

@Module
@InstallIn(SingletonComponent.class)
public abstract class AppModule {

    @Provides
    @Singleton
    public static DatabaseReference provideDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference();
    }

    @Provides
    @Singleton
    public static FirebaseDatabase provideFirebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }

    @Provides
    @Singleton
    public static Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    public static AlarmManager provideAlarmManager(@ApplicationContext Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @Binds
    @Singleton
    public abstract IDatabaseService bindDatabaseService(DatabaseService databaseService);

    @Binds
    @Singleton
    public abstract IAuthService bindAuthService(AuthServiceImpl authService);

    @Binds
    @Singleton
    public abstract IUserService bindUserService(UserServiceImpl userService);

    @Binds
    @Singleton
    public abstract IMedicationService bindMedicationService(MedicationServiceImpl medicationService);

    @Binds
    @Singleton
    public abstract IStatsService bindStatsService(StatsServiceImpl statsService);

    @Binds
    @Singleton
    public abstract IForumService bindForumService(ForumServiceImpl forumService);

    @Binds
    @Singleton
    public abstract IForumCategoriesService bindForumCategoryService(ForumCategoriesServiceImpl forumCategoryService);

    @Binds
    @Singleton
    public abstract IGameService bindGameService(GameServiceImpl gameService);

    @Binds
    @Singleton
    public abstract IImageService bindImageService(ImageServiceImpl imageService);

    @Binds
    @Singleton
    public abstract ITipOfTheDayService bindTipOfTheDayService(TipOfTheDayServiceImpl tipOfTheDayService);
}
