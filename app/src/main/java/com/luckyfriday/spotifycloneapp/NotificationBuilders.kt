package com.luckyfriday.spotifycloneapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat

object NotificationBuilders {
    // 1. Show notification
    @SuppressLint("RemoteViewLayout")
    fun showNotification(
        context: Context,
        progress: Float,
        title: String,
        duration: String,
        descriptions: String
    ): Notification {
        val channelId = "CHANNEL_ID"
        val channelName = "CHANNEL NAME"
        val channelDescription = "CHANNEL DESCRIPTION"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
            enableLights(false)
            enableVibration(false)
            setSound(null, null)
        }

        notificationManager.createNotificationChannel(notificationChannel)
        val notificationLayout =
            RemoteViews(context.packageName, R.layout.layout_notification).apply {
                setTextViewText(R.id.tv_title_notification, title)
                setTextViewText(R.id.tv_timer_notification, duration)
                setProgressBar(R.id.seekbar_notification, 100, progress.toInt(), false)
            }

        val notificationLayoutExpanded =
            RemoteViews(context.packageName, R.layout.layout_notification).apply {
                setTextViewText(R.id.tv_title_notification, title)
                setTextViewText(R.id.tv_subtitle_notification, descriptions)
                setTextViewText(R.id.tv_timer_notification, duration)
                setProgressBar(R.id.seekbar_notification, 100, progress.toInt(), false)
            }

        val customNotification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayoutExpanded)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(123, customNotification)

        return customNotification
    }

    // 2. Destroy notification
    fun cancel(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(123)
    }
}