package com.extralab.notifybox.Notifications

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.extralab.notifybox.R
import com.extralab.notifybox.RoomDB.NotificationEntity
import com.google.android.material.card.MaterialCardView
import java.util.Date

class NotificationAdapter(private val context: Context) :
    ListAdapter<NotificationEntity, NotificationAdapter.NotificationViewHolder>(
        NotificationDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = getItem(position)
        holder.bind(notification)
    }

    class NotificationViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val noty_app_name: TextView = itemView.findViewById(R.id.noty_app_name)
        private val noty_time: TextView = itemView.findViewById(R.id.noty_time)
        private val noty_title: TextView = itemView.findViewById(R.id.noty_title)
        private val noty_content: TextView = itemView.findViewById(R.id.noty_content)
        private val img_view_logo: ImageView = itemView.findViewById(R.id.img_view_logo)
        private val jump_to_original_notification: MaterialCardView =
            itemView.findViewById(R.id.jump_to_original_notification)

        fun bind(notification: NotificationEntity) {
            noty_time.text = longToDateString(notification.timestamp, "dd/MM/yyyy hh:mm a")
            noty_title.text = notification.title
            noty_content.text = notification.text

            try {
                // Load app icon using PackageManager
                val icon: Drawable =
                    context.packageManager.getApplicationIcon(notification.packageName)
                img_view_logo.setImageDrawable(icon)

                // Load app name using PackageManager
                val appName = context.packageManager.getApplicationLabel(
                    context.packageManager.getApplicationInfo(
                        notification.packageName,
                        PackageManager.GET_META_DATA
                    )
                ).toString()
                noty_app_name.text = appName

            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                Log.e(
                    "NotificationAdapter",
                    "App icon not found for package: ${notification.packageName}"
                )

                // Set a default icon and app_name if the app icon & name is not found
                img_view_logo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.logo))
                noty_app_name.text = "Unknown App"
            }

            jump_to_original_notification.setOnClickListener {
                openApp(notification.packageName)
            }
        }

        private fun longToDateString(timestamp: Long, format: String?): String {
            return DateFormat.format(format, Date(timestamp)).toString()
        }

        private fun openApp(packageName: String) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            } else {
                Toast.makeText(context, "Unable to open app", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationEntity>() {
        override fun areItemsTheSame(
            oldItem: NotificationEntity,
            newItem: NotificationEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: NotificationEntity,
            newItem: NotificationEntity
        ): Boolean {
            return oldItem == newItem
        }
    }
}
