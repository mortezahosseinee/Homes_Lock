package com.projects.company.homes_lock.database.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.projects.company.homes_lock.database.tables.Device;

import java.util.List;

@Dao
public interface DeviceDao {

    @Insert
    void insert(Device device);

    @Delete
    void delete(Device... devices);

    @Update
    void update(Device... devices);

    @Query("DELETE FROM device")
    void deleteAll();

    @Query("SELECT * FROM device")
    LiveData<List<Device>> getAllDevices();
}