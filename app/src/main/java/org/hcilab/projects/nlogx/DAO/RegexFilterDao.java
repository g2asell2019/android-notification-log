package org.hcilab.projects.nlogx.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;

import org.hcilab.projects.nlogx.Entity.RegexFilterEntity;

import java.util.List;

@Dao
public interface RegexFilterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(RegexFilterEntity filter);

    @Query("SELECT * FROM regex_filters WHERE packageName = :pkg")
    RegexFilterEntity getByPackage(String pkg);

    @Query("SELECT * FROM regex_filters")
    List<RegexFilterEntity> getAll();

    @Delete
    void delete(RegexFilterEntity filter);

    // TODO: Add more queries if needed, like search or pattern match
}
