package com.extralab.notifybox.Notifications

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.extralab.notifybox.RoomDB.NotificationDatabase
import com.extralab.notifybox.RoomDB.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationListener : NotificationListenerService() {

    private lateinit var database: NotificationDatabase

    override fun onCreate() {
        super.onCreate()
        database = NotificationDatabase.getDatabase(this)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        handleNotificationRemoval(sbn)
    }
/*
    override fun onListenerConnected() {
        super.onListenerConnected()
        CoroutineScope(Dispatchers.IO).launch {
            val activeNotifications = activeNotifications
            val activePackages = activeNotifications.map { it.packageName }.toSet()
            val allNotifications = database.notificationDao().getAllNotifications()

            allNotifications.forEach { notification ->
                if (notification.packageName !in activePackages) {
                    database.notificationDao().delete(notification)
                }
            }
        }
    }
*/
    private fun handleNotificationRemoval(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val packageName = sbn.packageName
        val title = notification.extras?.getString(Notification.EXTRA_TITLE, "")
        val text = notification.extras?.getString(Notification.EXTRA_TEXT, "")
        val timestamp = sbn.postTime
        val appName = getAppName(packageName)

        if (title.isNullOrBlank() || text.isNullOrBlank()) {
            Log.d("NotificationListener", "Empty notification title or text, skipping.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (packageName == "com.android.dialer" || packageName.contains(
                    "call",
                    ignoreCase = true
                )
            ) {
                handleCallNotification(packageName, title, text, timestamp, appName)
            } else {
                handleRegularNotification(packageName, title, text, timestamp, appName)
            }
        }
    }

    private fun getAppName(packageName: String): String {
        val packageManager = applicationContext.packageManager
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("NotificationListener", "App name not found for package: $packageName")
            ""
        }
    }

    private suspend fun handleCallNotification(
        packageName: String,
        title: String,
        text: String,
        timestamp: Long,
        appName: String
    ) {
        val existingNotification =
            database.notificationDao().getNotificationByPackageAndTitle(packageName, title)

        if (existingNotification == null) {
            insertNotificationToDatabase(
                NotificationEntity(
                    0,
                    title,
                    text,
                    packageName,
                    "",
                    timestamp,
                    appName
                )
            )
        } else {
            existingNotification.timestamp = timestamp
            updateNotificationInDatabase(existingNotification)
        }
    }

    private suspend fun handleRegularNotification(
        packageName: String,
        title: String,
        text: String,
        timestamp: Long,
        appName: String
    ) {
        insertNotificationToDatabase(
            NotificationEntity(
                0,
                title,
                text,
                packageName,
                "",
                timestamp,
                appName
            )
        )
    }

    private suspend fun insertNotificationToDatabase(notification: NotificationEntity) {
        withContext(Dispatchers.IO) {
            database.notificationDao().insert(notification)
        }
    }

    private suspend fun updateNotificationInDatabase(notification: NotificationEntity) {
        withContext(Dispatchers.IO) {
            database.notificationDao().update(notification)
        }
    }
}
