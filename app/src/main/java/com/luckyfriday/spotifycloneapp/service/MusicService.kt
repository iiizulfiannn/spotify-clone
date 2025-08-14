package com.luckyfriday.spotifycloneapp.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.CountDownTimer
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.luckyfriday.spotifycloneapp.model.MusicModel
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.Player
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
                        }
                        playMusic(position, exoPlayer.currentPosition)
                    }

                    Player.STATE_ENDED -> {}
                    Player.STATE_IDLE -> {}

                }
            }
        }

        exoPlayer.addListener(playerListener)
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
                    stopMusic(position, exoPlayer.currentPosition)
                    val dataMusic = musicList[position]
                    selectedMusicPlayed = dataMusic
                }

                Action.STOP_MODE -> {
                    exoPlayer.release()

                    // unregister if there is notification

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
                    countDownTimer.cancel()
                    if (exoPlayer.isPlaying) {
                        exoPlayer.playWhenReady = false
                    }

                    // remove notif
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

    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
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