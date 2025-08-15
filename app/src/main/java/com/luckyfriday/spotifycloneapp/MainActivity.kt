package com.luckyfriday.spotifycloneapp

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.Player
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luckyfriday.spotifycloneapp.adapter.MusicAdapter
import com.luckyfriday.spotifycloneapp.databinding.ActivityMainBinding
import com.luckyfriday.spotifycloneapp.listener.ItemMoveCallback
import com.luckyfriday.spotifycloneapp.listener.MusicActionServiceListener
import com.luckyfriday.spotifycloneapp.listener.MusicListener
import com.luckyfriday.spotifycloneapp.model.MusicModel
import com.luckyfriday.spotifycloneapp.notification.NotificationReceiver
import com.luckyfriday.spotifycloneapp.service.MusicService.INTENT.PENDING_DURATION
import com.luckyfriday.spotifycloneapp.service.MusicService.INTENT.PENDING_DURATION_TOTAL
import com.luckyfriday.spotifycloneapp.service.MusicService.INTENT.PENDING_POSITION
import com.luckyfriday.spotifycloneapp.service.MusicService.INTENT.PENDING_PROGRESS

class MainActivity : AppCompatActivity(), MusicListener {
    private lateinit var listener: MusicListener
    private lateinit var mainBinding: ActivityMainBinding
    private var listMusic = ArrayList<MusicModel>()
    private val musicAdapter: MusicAdapter by lazy { MusicAdapter(listMusic, listener) }
    private val notificationListener: MusicActionServiceListener by lazy {
        NotificationReceiver()
    }
    private var repeatMode: Int = Player.REPEAT_MODE_OFF
    private var isShuffleEnable: Boolean = false
    private var isPlaying = false
    private var currentPosition = -1
    private var progress = -1f
    private var duration = ""
    private var durationTotal = ""
    private var selectedMusicPlayed: MusicModel? = null
    private lateinit var dataFromService: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        listMusic.clear()
        listMusic.addAll(MusicModel.getListMap())

        listener = this
        checkNotificationPermission()

        mainBinding.tvTitleListSong.text =
            getString(R.string.list_song_title, listMusic.size.toString())
        mainBinding.layoutButtonHandle.btnPlay.setOnClickListener {
            if (isPlaying) {
                isPlaying = false
                pauseMusic()
            } else {
                isPlaying = true
                playMusic()
            }
            playButton()
        }

        mainBinding.musicPlayingNowFullScreen.layoutBtnDetailScreen.btnPlay.setOnClickListener {
            if (isPlaying) pauseMusic() else playMusic()
            isPlaying = !isPlaying
            playButton()
        }

        mainBinding.layoutButtonHandle.btnNext.setOnClickListener {
            nextButtonListener()
        }

        mainBinding.musicPlayingNowFullScreen.layoutBtnDetailScreen.btnNext.setOnClickListener {
            nextButtonListener()
        }

        mainBinding.layoutButtonHandle.btnPrevious.setOnClickListener {
            previousButtonListener()
        }

        mainBinding.musicPlayingNowFullScreen.layoutBtnDetailScreen.btnPrevious.setOnClickListener {
            previousButtonListener()
        }

        mainBinding.layoutButtonHandle.btnShuffle.setOnClickListener {
            shuffleButtonListener()
        }

        mainBinding.musicPlayingNowFullScreen.layoutBtnDetailScreen.btnShuffle.setOnClickListener {
            shuffleButtonListener()
        }

        mainBinding.layoutButtonHandle.btnRepeat.setOnClickListener {
            repeatButtonListener()
        }

        mainBinding.musicPlayingNowFullScreen.layoutBtnDetailScreen.btnRepeat.setOnClickListener {
            repeatButtonListener()
        }

        mainBinding.layoutMusicNowPlaying.root.setOnClickListener {
            showFullScreenMusic()
        }

        mainBinding.musicPlayingNowFullScreen.ivArrow.setOnClickListener {
            hideFullScreen()
        }

        registerBroadcast()

        setSelectedValue()

        recyclerViewSetup()
    }

    private fun checkNotificationPermission(onGranted: () -> Unit = {}) {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            showDialogNotificationPermission(this)
        } else {
            onGranted()
        }
    }

    private fun showDialogNotificationPermission(activity: Activity, onCancel: () -> Unit = {}) {
        AlertDialog.Builder(activity)
            .setTitle("Hi user!")
            .setMessage("For better experience please enable notification permission on your device")
            .setPositiveButton("Allow") { _, _ ->
                openNotificationSettings(activity)
            }
            .setNegativeButton("Cancel") { _, _ ->
                onCancel()
            }
            .show()
    }

    private fun openNotificationSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
        activity.startActivity(intent)
    }

    private fun setSelectedValue() {
        currentPosition = intent?.getIntExtra(PENDING_POSITION, -1) ?: -1
        progress = intent?.getFloatExtra(PENDING_PROGRESS, 0f) ?: 0f
        duration = intent?.getStringExtra(PENDING_DURATION) ?: ""
        durationTotal = intent?.getStringExtra(PENDING_DURATION_TOTAL) ?: ""

        if (currentPosition == -1) return
        selectedMusicPlayed = listMusic[currentPosition]
        musicAdapter.setSelectedMusic(selectedMusicPlayed)
        updateDurationAndProgress()
        showHideLinePlaying()
        showMusicPlayer()
        showFullScreenMusic()
    }

    private fun registerBroadcast() {
        dataFromService = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val bundle = intent?.extras
                if (bundle != null) {
                    if (bundle.containsKey(PENDING_POSITION)) {
                        val oldPosition = bundle.getInt(PENDING_POSITION)
                        if (currentPosition == oldPosition) return
                        currentPosition = oldPosition
                        selectedMusicPlayed = listMusic[currentPosition]
                        musicAdapter.setSelectedMusic(selectedMusicPlayed)
                    }
                    if (bundle.containsKey(PENDING_PROGRESS)) {
                        progress = bundle.getFloat(PENDING_PROGRESS)
                        updateDurationAndProgress()
                    }
                    if (bundle.containsKey(PENDING_DURATION_TOTAL)) {
                        durationTotal = bundle.getString(PENDING_DURATION_TOTAL) ?: ""
                        updateDurationAndProgress()
                    }
                    if (bundle.containsKey(PENDING_DURATION)) {
                        duration = bundle.getString(PENDING_DURATION) ?: ""
                        updateDurationAndProgress()
                    }

                }
            }
        }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(dataFromService, IntentFilter("musicBroadcast"))

    }

    private fun updateDurationAndProgress() {
        mainBinding.musicPlayingNowFullScreen.seekbar.progress = progress.toInt()
        mainBinding.musicPlayingNowFullScreen.tvTimerCurrent.text = duration
        mainBinding.musicPlayingNowFullScreen.tvTimerTotal.text = durationTotal

    }

    private fun hideFullScreen() {
        mainBinding.musicPlayingNowFullScreen.root.visibility = View.GONE
        mainBinding.layoutChooseSong.visibility = View.VISIBLE
    }

    private fun showFullScreenMusic() {
        mainBinding.musicPlayingNowFullScreen.root.visibility = View.VISIBLE
        mainBinding.layoutChooseSong.visibility = View.GONE
    }

    private fun repeatButtonListener() {
        repeatMode = when (repeatMode) {
            Player.REPEAT_MODE_OFF -> {
                setRepeatType("ONE")
                Player.REPEAT_MODE_ONE
            }

            Player.REPEAT_MODE_ONE -> {
                setRepeatType("ALL")
                Player.REPEAT_MODE_ALL
            }

            Player.REPEAT_MODE_ALL -> {
                setRepeatType("OFF")
                Player.REPEAT_MODE_OFF
            }

            else -> Player.REPEAT_MODE_OFF
        }
        notificationListener.onRepeat(this, repeatMode)
    }

    private fun setRepeatType(mode: String = "OFF") {
        mainBinding.layoutButtonHandle.tvRepeatIndicator.text = mode
        mainBinding.musicPlayingNowFullScreen.layoutBtnDetailScreen.tvRepeatIndicator.text = mode
    }

    private fun shuffleButtonListener() {
        isShuffleEnable = !isShuffleEnable
        notificationListener.onShuffle(this, isShuffleEnable)
        updateShuffleButton()
    }

    private fun updateShuffleButton() {
        val iconResource = if (isShuffleEnable) {
            R.drawable.ic_shuffle_on
        } else {
            R.drawable.ic_shuffle_off
        }
        mainBinding.layoutButtonHandle.btnShuffle.setImageResource(iconResource)
        mainBinding.musicPlayingNowFullScreen.layoutBtnDetailScreen.btnShuffle.setImageResource(
            iconResource
        )
    }

    private fun previousButtonListener() {
        selectedMusicPlayed = if (currentPosition <= 0) {
            currentPosition = listMusic.size - 1
            listMusic[currentPosition]
        } else {
            currentPosition -= 1
            listMusic[currentPosition]
        }
        musicAdapter.setSelectedMusic(selectedMusicPlayed)
        showHideLinePlaying()
        notificationListener.onRestart(this, currentPosition)
    }

    private fun nextButtonListener() {
        selectedMusicPlayed = if (currentPosition >= listMusic.size - 1) {
            currentPosition = 0
            listMusic[currentPosition]
        } else {
            currentPosition += 1
            listMusic[currentPosition]
        }
        musicAdapter.setSelectedMusic(selectedMusicPlayed)
        showHideLinePlaying()
        notificationListener.onRestart(this, currentPosition)
    }

    private fun playMusic() {
        notificationListener.onPlay(this, currentPosition)
    }

    private fun pauseMusic() {
        notificationListener.onPause(this)
    }

    private fun recyclerViewSetup() {
        // attached item list
        mainBinding.rvListSong.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
            adapter = musicAdapter
        }

        // drag drop position
        val callback = ItemMoveCallback(musicAdapter)
        ItemTouchHelper(callback).attachToRecyclerView(mainBinding.rvListSong)

        musicAdapter.setSelectedMusic(selectedMusicPlayed)
    }

    override fun onClicked(music: MusicModel) {
        currentPosition = listMusic.indexOf(music)
        selectedMusicPlayed = listMusic[currentPosition]
        isPlaying = true

        // restart music
        restartMusic()

        // play button
        playButton()
    }

    private fun playButton() {
        // show hide now playing
        showHideLinePlaying()

        // show handle player
        showMusicPlayer()

        if (currentPosition == -1) return

        if (isPlaying) {
            mainBinding.layoutButtonHandle.btnPlay.setImageResource(R.drawable.ic_paused)
            mainBinding.musicPlayingNowFullScreen.layoutBtnDetailScreen.btnPlay.setImageResource(R.drawable.ic_paused)
        } else {
            mainBinding.layoutButtonHandle.btnPlay.setImageResource(R.drawable.ic_play)
            mainBinding.musicPlayingNowFullScreen.layoutBtnDetailScreen.btnPlay.setImageResource(R.drawable.ic_play)
        }

        musicAdapter.setSelectedMusic(selectedMusicPlayed)
    }

    private fun showMusicPlayer() {
        mainBinding.layoutButtonHandle.root.isVisible = true
    }

    private fun showHideLinePlaying() {
        // now playing
        mainBinding.layoutMusicNowPlaying.root.isVisible = true
        mainBinding.layoutMusicNowPlaying.tvTitleNowPlaying.text = selectedMusicPlayed?.title
        mainBinding.layoutMusicNowPlaying.tvDescriptionNowPlaying.text =
            selectedMusicPlayed?.description
        mainBinding.layoutMusicNowPlaying.ivNowPlaying.loadImage(
            this,
            selectedMusicPlayed?.imageCover.orEmpty()
        )
        //full screen
        mainBinding.musicPlayingNowFullScreen.tvTitleSong.text = selectedMusicPlayed?.title
        mainBinding.musicPlayingNowFullScreen.tvDescriptionSong.text =
            selectedMusicPlayed?.description
        mainBinding.musicPlayingNowFullScreen.ivAlbumCover.loadImage(
            this,
            selectedMusicPlayed?.imageCover.orEmpty()
        )
    }

    private fun restartMusic() {
        notificationListener.onRestart(this, currentPosition)
    }

    override fun onDestroy() {
        notificationListener.onStop(this)
        unregisterBroadcast()
        super.onDestroy()
    }

    private fun unregisterBroadcast() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataFromService)
    }
}