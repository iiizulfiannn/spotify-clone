package com.luckyfriday.spotifycloneapp

import android.content.BroadcastReceiver
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
        listener = this
        listMusic.addAll(MusicModel.getListMap())
        recyclerViewSetup()
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
        mainBinding.layoutButtonHandle.apply {
            root.isVisible = true
        }
    }

    private fun showHideLinePlaying() {
        // now playing
        mainBinding.layoutMusicNowPlaying.apply {
            root.isVisible = true
            tvTitleNowPlaying.text = selectedMusicPlayed?.title
            tvDescriptionNowPlaying.text = selectedMusicPlayed?.description
            ivNowPlaying.loadImage(this@MainActivity, selectedMusicPlayed?.imageCover.orEmpty())
        }

        // fullscreen
        mainBinding.musicPlayingNowFullScreen.apply {
            tvTitleSong.text = selectedMusicPlayed?.title
            tvDescriptionSong.text = selectedMusicPlayed?.description
            ivAlbumCover.loadImage(this@MainActivity, selectedMusicPlayed?.imageCover.orEmpty())
        }
    }

    private fun restartMusic() {
        notificationListener.onRestart(this, currentPosition)
    }
}