package com.luckyfriday.spotifycloneapp.listener

import com.luckyfriday.spotifycloneapp.model.MusicModel

interface MusicListener {
    fun onClicked(music: MusicModel)
    fun onPositionChanged(newList: MutableList<MusicModel>, oldPosition: Int, newPosition: Int) = Unit
}