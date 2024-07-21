package com.extralab.notifybox.RoomDB

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val notificationDao: NotificationDao =
        NotificationDatabase.getDatabase(application).notificationDao()

    init {
        deleteOldNotifications()
    }

    fun getAllNotifications(): LiveData<List<NotificationEntity>> {
        return notificationDao.getAllNotifications()
    }

    fun getTodayNotifications(): LiveData<List<NotificationEntity>> {
        val currentDate = System.currentTimeMillis()
        return notificationDao.getTodayNotifications(currentDate)
    }

    fun getYesterdayNotifications(): LiveData<List<NotificationEntity>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayDate = calendar.timeInMillis
        return notificationDao.getYesterdayNotifications(yesterdayDate)
    }

    private fun deleteOldNotifications() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayDate = calendar.timeInMillis

        viewModelScope.launch(Dispatchers.IO) {
            notificationDao.deleteOldNotifications(yesterdayDate)
        }
    }
}
