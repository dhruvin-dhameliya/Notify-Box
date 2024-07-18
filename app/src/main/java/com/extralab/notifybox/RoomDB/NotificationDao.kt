package com.extralab.notifybox.RoomDB

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Update
    suspend fun update(notification: NotificationEntity)

//    @Delete
//    suspend fun delete(notification: NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): LiveData<List<NotificationEntity>> // Use LiveData here

    @Query("SELECT * FROM notifications WHERE packageName = :packageName AND title = :title ORDER BY timestamp DESC LIMIT 1")
    suspend fun getNotificationByPackageAndTitle(packageName: String, title: String): NotificationEntity?
}