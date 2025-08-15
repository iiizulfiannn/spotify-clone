package com.luckyfriday.spotifycloneapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.luckyfriday.spotifycloneapp.service.MusicService.Action.NOTIFICATION_ID
import com.luckyfriday.spotifycloneapp.service.MusicService.INTENT.PENDING_DESCRIPTION
import com.luckyfriday.spotifycloneapp.service.MusicService.INTENT.PENDING_DURATION
import com.luckyfriday.spotifycloneapp.service.MusicService.INTENT.PENDING_DURATION_TOTAL
import com.luckyfriday.spotifycloneapp.service.MusicService.INTENT.PENDING_POSITION
import com.luckyfriday.spotifycloneapp.service.MusicService.INTENT.PENDING_PROGRESS
import com.luckyfriday.spotifycloneapp.service.MusicService.INTENT.PENDING_TITLE

object NotificationBuilders {

    // 1. Show notification
    @SuppressLint("RemoteViewLayout")
    fun showNotification(
        context: Context,
        progress: Float,
        title: String,
        duration: String,
        descriptions: String,
        totalDuration: String,
        position: Int,
        image: Bitmap
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
        val pendingIntent = Intent(context, MainActivity::class.java)
        pendingIntent.putExtra(PENDING_PROGRESS, progress)
        pendingIntent.putExtra(PENDING_DURATION, duration)
        pendingIntent.putExtra(PENDING_DURATION_TOTAL, totalDuration)
        pendingIntent.putExtra(PENDING_TITLE, title)
        pendingIntent.putExtra(PENDING_DESCRIPTION, descriptions)
        pendingIntent.putExtra(PENDING_POSITION, position)
        pendingIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val contentIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            pendingIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationLayout =
            RemoteViews(context.packageName, R.layout.layout_notification).apply {
                setTextViewText(R.id.tv_title_notification, title)
                setTextViewText(R.id.tv_timer_notification, duration)
                setProgressBar(R.id.seekbar_notification, 100, progress.toInt(), false)
                setImageViewBitmap(R.id.iv_notification, image)
                setImageViewBitmap(R.id.iv_notification_bg, image)
            }

        val notificationLayoutExpanded =
            RemoteViews(context.packageName, R.layout.layout_notification).apply {
                setTextViewText(R.id.tv_title_notification, title)
                setTextViewText(R.id.tv_subtitle_notification, descriptions)
                setTextViewText(R.id.tv_timer_notification, duration)
                setProgressBar(R.id.seekbar_notification, 100, progress.toInt(), false)
                setImageViewBitmap(R.id.iv_notification, image)
                setImageViewBitmap(R.id.iv_notification_bg, image)
            }

        val customNotification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(contentIntent)
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayoutExpanded)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, customNotification)

        return customNotification
    }

    // 2. Destroy notification
    fun cancel(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}