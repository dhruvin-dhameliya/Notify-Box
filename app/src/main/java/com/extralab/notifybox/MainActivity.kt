package com.extralab.notifybox

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.extralab.notifybox.Notifications.NotificationAdapter
import com.extralab.notifybox.RoomDB.NotificationDatabase
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var btn_open_settings: MaterialButton
    private lateinit var layout_no_notification: LinearLayout
    private lateinit var recyclerView: RecyclerView

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: Toolbar

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Check notification access permission again after returning from settings
            checkNotificationAccess()
            if (isNotificationServiceEnabled(this)) {
                showSnackbar("Notification access permission granted")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        displayDrawer()

        btn_open_settings = findViewById(R.id.btn_open_settings)
        layout_no_notification = findViewById(R.id.layout_no_notification)
        recyclerView = findViewById(R.id.recyclerView)

        checkNotificationAccess()

        // Set initial visibility
        recyclerView.visibility = View.GONE
        layout_no_notification.visibility = View.GONE

        notificationAdapter = NotificationAdapter(this)
        recyclerView.adapter = notificationAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun requestNotificationAccess() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        requestPermissionLauncher.launch(intent)
    }

    private fun isNotificationServiceEnabled(context: Context): Boolean {
        val enabledNotificationListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        val packageName = context.packageName
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(
            packageName
        )
    }

    private fun checkNotificationAccess() {
        if (isNotificationServiceEnabled(this)) {
            btn_open_settings.visibility = View.GONE
            loadNotifications()
        } else {
            recyclerView.visibility = View.GONE
            layout_no_notification.visibility = View.GONE
            btn_open_settings.visibility = View.VISIBLE
            btn_open_settings.setOnClickListener {
                requestNotificationAccess()
            }
            showSnackbar("Please enable notification service access")
        }
    }

    private fun loadNotifications() {
        val dao = NotificationDatabase.getDatabase(applicationContext).notificationDao()

        // Observe changes in notifications
        dao.getAllNotifications().observe(this) { notifications ->
            notificationAdapter.submitList(notifications)
            if (notifications.isEmpty()) {
                recyclerView.visibility = View.GONE
                layout_no_notification.visibility = View.VISIBLE
            } else {
                layout_no_notification.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun displayDrawer() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
