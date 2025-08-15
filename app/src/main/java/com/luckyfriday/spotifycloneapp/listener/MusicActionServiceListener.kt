package com.luckyfriday.spotifycloneapp.listener

import android.content.Context
import com.luckyfriday.spotifycloneapp.model.MusicModel

interface MusicActionServiceListener {
    fun onListPositionChanged(context: Context, list: MutableList<MusicModel>, position: Int)
    fun onPlay(context: Context, position: Int)
    fun onRestart(context: Context, position: Int)
    fun onPause(context: Context)
    fun onStop(context: Context)
    fun onShuffle(context: Context, isEnable: Boolean)
    fun onRepeat(context: Context, repeatMode: Int)
}