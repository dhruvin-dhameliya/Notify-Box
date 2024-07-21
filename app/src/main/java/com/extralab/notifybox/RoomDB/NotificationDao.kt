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

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): LiveData<List<NotificationEntity>> // Use LiveData here

    @Query("SELECT * FROM notifications WHERE packageName = :packageName AND title = :title ORDER BY timestamp DESC LIMIT 1")
    suspend fun getNotificationByPackageAndTitle(
        packageName: String,
        title: String
    ): NotificationEntity?

    @Query("SELECT * FROM notifications WHERE date(timestamp / 1000, 'unixepoch') = date(:currentDate / 1000, 'unixepoch') ORDER BY timestamp DESC")
    fun getTodayNotifications(currentDate: Long): LiveData<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE date(timestamp / 1000, 'unixepoch') = date(:yesterdayDate / 1000, 'unixepoch') ORDER BY timestamp DESC")
    fun getYesterdayNotifications(yesterdayDate: Long): LiveData<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE date(timestamp / 1000, 'unixepoch') >= date(:weekStartDate / 1000, 'unixepoch') ORDER BY timestamp DESC")
    fun getCurrentWeekNotifications(weekStartDate: Long): LiveData<List<NotificationEntity>>
}
