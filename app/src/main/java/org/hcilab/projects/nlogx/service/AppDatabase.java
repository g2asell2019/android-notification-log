package org.hcilab.projects.nlogx.service;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import org.hcilab.projects.nlogx.DAO.AppFilterDao;
import org.hcilab.projects.nlogx.DAO.RegexFilterDao;
import org.hcilab.projects.nlogx.Entity.AppFilterEntity;
import org.hcilab.projects.nlogx.Entity.RegexFilterEntity;

@Database(entities = {RegexFilterEntity.class, AppFilterEntity.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    public abstract RegexFilterDao regexFilterDao();
    public abstract AppFilterDao appFilterDao();
    private static volatile AppDatabase INSTANCE;



    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // TODO: Replace "regex_db" with your own DB name if you want
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "regex_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
