package com.projects.company.homes_lock.database.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.projects.company.homes_lock.database.tables.Device;

import java.util.List;

@Dao
public interface DeviceDao extends BaseDao<Device> {

    @Insert
    void insert(Device device);

    @Delete
    void delete(Device... device);

    @Update
    void update(Device... device);

    @Query("DELETE FROM device")
    void deleteAll();

    @Query("SELECT * FROM device")
    LiveData<List<Device>> getAllDevices();

    @Query("SELECT COUNT(*) FROM device")
    LiveData<Integer> getAllDevicesCount();
}