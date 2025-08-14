package com.luckyfriday.spotifycloneapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luckyfriday.spotifycloneapp.adapter.MusicAdapter
import com.luckyfriday.spotifycloneapp.databinding.ActivityMainBinding
import com.luckyfriday.spotifycloneapp.model.MusicModel

class MainActivity : AppCompatActivity() {
    private var listMusic = ArrayList<MusicModel>()
    private val musicAdapter: MusicAdapter by lazy { MusicAdapter(listMusic) }
    private lateinit var mainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        listMusic.clear()
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
}