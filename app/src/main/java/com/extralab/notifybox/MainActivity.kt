package com.extralab.notifybox

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.extralab.notifybox.Notifications.NotificationAdapter
import com.extralab.notifybox.RoomDB.NotificationEntity
import com.extralab.notifybox.RoomDB.NotificationViewModel
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

    private lateinit var notificationViewModel: NotificationViewModel
    private val REQUEST_POST_NOTIFICATIONS = 1001

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

        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        btn_open_settings = findViewById(R.id.btn_open_settings)
        layout_no_notification = findViewById(R.id.layout_no_notification)
        recyclerView = findViewById(R.id.recyclerView)

        displayDrawer()
        runtimeNotificationPermission()
        checkNotificationAccess()

        // Set initial visibility
        recyclerView.visibility = View.GONE
        layout_no_notification.visibility = View.GONE

        notificationAdapter = NotificationAdapter(this)
        recyclerView.adapter = notificationAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_all -> loadAllNotifications()
                R.id.nav_today -> loadTodayNotifications()
                R.id.nav_yesterday -> loadYesterdayNotifications()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun runtimeNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_POST_NOTIFICATIONS
                )
            }
        }
    }

    private fun requestNotificationAccess() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        requestPermissionLauncher.launch(intent)
    }

    private fun isNotificationServiceEnabled(context: Context): Boolean {
        val enabledNotificationListeners = Settings.Secure.getString(
            context.contentResolver, "enabled_notification_listeners"
        )
        val packageName = context.packageName
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(
            packageName
        )
    }

    private fun checkNotificationAccess() {
        if (isNotificationServiceEnabled(this)) {
            btn_open_settings.visibility = View.GONE
            loadTodayNotifications()
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

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    // Display Drawer
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
        toggle.drawerArrowDrawable.color = Color.WHITE

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // If the drawer is closed, invoke the default back pressed behavior
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }


    // GET ALL, TODAY, YESTERDAY, WEEK NOTIFICATIONS
    private fun loadAllNotifications() {
        notificationViewModel.getAllNotifications().observe(this, Observer { notifications ->
            notificationAdapter.submitList(notifications)
            updateRecyclerViewVisibility(notifications)
        })
    }

    private fun loadTodayNotifications() {
        notificationViewModel.getTodayNotifications().observe(this, Observer { notifications ->
            notificationAdapter.submitList(notifications)
            updateRecyclerViewVisibility(notifications)
        })
    }

    private fun loadYesterdayNotifications() {
        notificationViewModel.getYesterdayNotifications().observe(this, Observer { notifications ->
            notificationAdapter.submitList(notifications)
            updateRecyclerViewVisibility(notifications)
        })
    }

    private fun updateRecyclerViewVisibility(notifications: List<NotificationEntity>) {
        if (!isNotificationServiceEnabled(this)) {
            checkNotificationAccess()
            return
        }
        if (notifications.isEmpty()) {
            recyclerView.visibility = View.GONE
            layout_no_notification.visibility = View.VISIBLE
        } else {
            layout_no_notification.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}
