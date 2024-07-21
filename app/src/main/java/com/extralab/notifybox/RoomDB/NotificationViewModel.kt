package com.extralab.notifybox.RoomDB

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import java.util.Calendar

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val notificationDao: NotificationDao =
        NotificationDatabase.getDatabase(application).notificationDao()

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

    fun getCurrentWeekNotifications(): LiveData<List<NotificationEntity>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val weekStartDate = calendar.timeInMillis
        return notificationDao.getCurrentWeekNotifications(weekStartDate)
    }
}
