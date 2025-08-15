package com.luckyfriday.spotifycloneapp.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.CountDownTimer
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.luckyfriday.spotifycloneapp.model.MusicModel
import androidx.core.net.toUri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.C
import androidx.media3.common.Player
import com.luckyfriday.spotifycloneapp.loadImageNotification
import com.luckyfriday.spotifycloneapp.notification.NotificationBuilders
import com.luckyfriday.spotifycloneapp.service.MusicService.Action.NOTIFICATION_ID
import kotlin.math.abs

class MusicService : Service() {
    private var musicList = ArrayList<MusicModel>()
    private lateinit var exoPlayer: ExoPlayer
    private val mediaItem = ArrayList<MediaItem>()
    private var currentState: String = ""
    private var totalDuration: Long = 0
    private var countDownTimer: CountDownTimer? = null
    private var selectedMusicPlayed: MusicModel? = null
    private var position: Int = -1
    private var isShuffleEnable: Boolean? = false
    private var lastDuration: Long = 0
    private var notification: Notification? = null

    override fun onCreate() {
        super.onCreate()
        musicList.clear()
        musicList.addAll(MusicModel.getListMap())

        // init exoplayer
        exoPlayer = ExoPlayer.Builder(this).build()

        // track item list song will play by player
        musicList.map {
            val uri = "android.resource://${packageName}/${it.musicFile}".toUri()
            mediaItem.add(MediaItem.fromUri(uri))
        }
        exoPlayer.setMediaItems(mediaItem)
        exoPlayer.volume = 1f
        exoPlayer.prepare()

        // player listener
        val playerListener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        val duration = exoPlayer.duration
                        if (duration != C.TIME_UNSET && currentState == Action.RESTART_MODE) {
                            totalDuration = duration
                            playMusic(position, exoPlayer.currentPosition)
                        }
                    }

                    Player.STATE_ENDED -> {}
                    Player.STATE_IDLE -> {}
                    Player.STATE_BUFFERING -> {}
                }
            }
        }

        exoPlayer.addListener(playerListener)
    }

    private fun createNotification(
        context: Context,
        position: Int,
        progress: Float = 0F,
        title: String,
        duration: String = "00:00",
        description: String,
        durationTotal: String = "00:00",
        image: Bitmap
    ): Notification {
        return NotificationBuilders.showNotification(
            context = context,
            progress = progress,
            title = title,
            duration = duration,
            descriptions = description,
            durationTotal = durationTotal,
            position = position,
            image = image
        )
    }

    private fun removeNotification() {
        NotificationBuilders.cancel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val newPosition = intent?.getIntExtra(TAG.POSITION, 0)
        val isShuffle = intent?.getBooleanExtra(TAG.IS_SHUFFLE, false)
        val repeatMode = intent?.getIntExtra(TAG.REPEAT_MODE, -1)
        val newList = intent?.getParcelableArrayListExtra<MusicModel>(TAG.NEW_LIST)
        if (intent?.action == Action.START_FOREGROUND_ACTION) {
            currentState = intent.getStringExtra(TAG.ACTION) ?: ""
            when (currentState) {
                Action.CHANGE_POSITION -> {
                    if (newPosition != null) {
                        position = newPosition
                    }
                    musicList.clear()
                    musicList.addAll(newList?.toList() ?: listOf())
                }

                Action.START_MODE -> {
                    if (newPosition != null) {
                        position = newPosition
                    }
                    totalDuration = abs(exoPlayer.duration)
                    playMusic(position, exoPlayer.currentPosition)
                }

                Action.RESTART_MODE -> {
                    if (newPosition != null) {
                        position = newPosition
                    }

                    // stop music
//                    stopMusic(position, exoPlayer.currentPosition)
                    stopMusic(position, 0)
                    val dataMusic = musicList[position]
                    selectedMusicPlayed = dataMusic
                }

                Action.STOP_MODE -> {
                    exoPlayer.release()

                    // unregister if there is notification
                    removeNotification()

                    // kill the service
                    killService()
                }

                Action.SHUFFLE_MODE -> {
                    isShuffleEnable = isShuffle
                    if (isShuffle != null) {
                        exoPlayer.shuffleModeEnabled = isShuffle
                    }
                }

                Action.REPEAT_MODE -> {
                    exoPlayer.repeatMode = repeatMode ?: Player.REPEAT_MODE_OFF
                }

                Action.PAUSE_MODE -> {
                    // countdown timer cancel
                    countDownTimer?.cancel()
                    if (exoPlayer.isPlaying) {
                        exoPlayer.playWhenReady = false
                    }

                    // remove notif
                    removeNotification()
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun killService() {
        stopSelf()
        stopForeground(true)
    }

    private fun stopMusic(position: Int, duration: Long) {
        exoPlayer.seekTo(position, duration)
    }

    private fun playMusic(position: Int, lastPosition: Long) {
        if (!exoPlayer.isPlaying) {
            exoPlayer.seekTo(position, lastPosition)
            exoPlayer.playWhenReady = true
            val dataMusic = musicList[position]
            selectedMusicPlayed = dataMusic
            countDownTimer?.cancel()
            startCountDown()
        }
    }

    private fun startCountDown() {
        countDownTimer = object : CountDownTimer(totalDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                lastDuration = exoPlayer.currentPosition

                val minutes = lastDuration / 60000
                val seconds = (lastDuration % 60000) / 1000
                val formattedTime = String.format("%02d:%02d", minutes, seconds)
                val progress = ((lastDuration * 100) / totalDuration).toFloat()

                val minutesTotal = totalDuration / 60000
                val secondsTotal = (totalDuration % 60000) / 1000
                val formattedTimeTotal = String.format("%02d:%02d", minutesTotal, secondsTotal)

                if (selectedMusicPlayed?.title.isNullOrEmpty()) {
                    removeNotification()
                    killService()
                } else {
                    loadImageNotification(
                        this@MusicService,
                        selectedMusicPlayed?.imageCover.orEmpty()
                    ) {
                        notification = createNotification(
                            this@MusicService,
                            progress = progress,
                            position = position,
                            duration = formattedTime,
                            durationTotal = formattedTimeTotal,
                            title = selectedMusicPlayed?.title.orEmpty(),
                            description = selectedMusicPlayed?.description.orEmpty(),
                            image = it
                        )
                        //send activity
                        sendValueToActivity(
                            progress,
                            formattedTime,
                            formattedTimeTotal,
                            selectedMusicPlayed?.title.orEmpty(),
                            selectedMusicPlayed?.description.orEmpty()
                        )

                        // start foreground on notification
                        startForeground(NOTIFICATION_ID, notification)
                    }
                }
            }

            override fun onFinish() {
                sendValueToActivity(
                    progress = 100f,
                    duration = "00:00",
                    durationTotal = "00:00",
                    title = selectedMusicPlayed?.title.orEmpty(),
                    description = selectedMusicPlayed?.description.orEmpty()
                )

                stopMusic(position, 0)
                if (isShuffleEnable == true) setRandomPlaylistPosition() else setNextPosition()
                totalDuration = abs(exoPlayer.duration)
                playMusic(position, exoPlayer.currentPosition)

            }

        }
        countDownTimer?.start()
    }


    private fun setNextPosition() {
        if (position >= musicList.size - 1) {
            position = 0
        } else {
            position += 1
        }
    }

    private fun setRandomPlaylistPosition() {
        position = (0..musicList.size).random()
    }

    private fun sendValueToActivity(
        progress: Float,
        duration: String,
        durationTotal: String,
        title: String,
        description: String
    ) {
        val intent = Intent("musicBroadcast")
        intent.putExtra(INTENT.PENDING_POSITION, position)
        intent.putExtra(INTENT.PENDING_PROGRESS, progress)
        intent.putExtra(INTENT.PENDING_DURATION, duration)
        intent.putExtra(INTENT.PENDING_DURATION_TOTAL, durationTotal)
        intent.putExtra(INTENT.PENDING_TITLE, title)
        intent.putExtra(INTENT.PENDING_DESCRIPTION, description)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        selectedMusicPlayed = null
        exoPlayer.release()
        killService()
        super.onDestroy()
    }

    object Action {
        const val NOTIFICATION_ID = 123
        const val START_FOREGROUND_ACTION = "START_FOREGROUND_ACTION"
        const val START_MODE = "START_MODE"
        const val PAUSE_MODE = "PAUSE_MODE"
        const val RESTART_MODE = "RESTART_MODE"
        const val STOP_MODE = "STOP_MODE"
        const val SHUFFLE_MODE = "SHUFFLE_MODE"
        const val REPEAT_MODE = "REPEAT_MODE"
        const val CHANGE_POSITION = "CHANGE_POSITION"
    }

    object INTENT {
        const val PENDING_POSITION =
            "com.luckyfriday.spotifycloneapp.service.action.START_FOREGROUND_ACTION"
        const val PENDING_PROGRESS = "PENDING_PROGRESS"
        const val PENDING_DURATION = "PENDING_DURATION"
        const val PENDING_DURATION_TOTAL = "PENDING_DURATION_TOTAL"
        const val PENDING_TITLE = "PENDING_TITLE"
        const val PENDING_DESCRIPTION = "PENDING_DESCRIPTION"
    }

    object TAG {
        const val REPEAT_MODE = "REPEAT_MODE"
        const val IS_SHUFFLE = "IS_SHUFFLE"
        const val POSITION = "POSITION"
        const val NEW_LIST = "NEW_LIST"
        const val ACTION = "ACTION"
    }

}