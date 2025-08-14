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

class MusicService : Service() {
    private var musicList = ArrayList<MusicModel>()
    private lateinit var exoPlayer: ExoPlayer
    private val mediaItem = ArrayList<MediaItem>()
    private var currentState: String = ""
    private var totalDuration: Long = 0
    private var countDownTimer: CountDownTimer? = null
    private var selectedMusicPlayed: MusicModel? = null
    private var position: Int = -1

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
                }
            }
        }

        exoPlayer.addListener(playerListener)
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
        const val START_MODE = "START_MODE"
        const val PAUSE_MODE = "PAUSE_MODE"
        const val RESTART_MODE = "RESTART_MODE"
        const val STOP_MODE = "STOP_MODE"
        const val SHUFFLE_MODE = "SHUFFLE_MODE"
        const val REPEAT_MODE = "REPEAT_MODE"
    }

}