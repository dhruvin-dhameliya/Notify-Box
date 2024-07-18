package com.extralab.notifybox.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val title: String,
    val text: String,
    val packageName: String,
    val icon: String,
    var timestamp: Long,
    val appName: String
)
