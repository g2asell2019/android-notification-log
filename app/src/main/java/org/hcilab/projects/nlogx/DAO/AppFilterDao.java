package org.hcilab.projects.nlogx.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.hcilab.projects.nlogx.Entity.AppFilterEntity;

import java.util.List;

@Dao
public interface AppFilterDao {

    @Query("SELECT * FROM app_filter")
    List<AppFilterEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(AppFilterEntity filter);
    @Query("SELECT * FROM app_filter WHERE packageName = :pkg")
    AppFilterEntity getByPackage(String pkg);

    @Delete
    void delete(AppFilterEntity appFilterEntity);
}
