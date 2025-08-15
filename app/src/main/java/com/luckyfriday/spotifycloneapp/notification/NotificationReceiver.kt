package com.luckyfriday.spotifycloneapp.notification

import android.content.Context
import android.content.Intent
import com.luckyfriday.spotifycloneapp.listener.MusicActionServiceListener
import com.luckyfriday.spotifycloneapp.model.MusicModel
import com.luckyfriday.spotifycloneapp.service.MusicService

class NotificationReceiver : MusicActionServiceListener {

    private inline fun executeServiceAction(
        context: Context,
        action: String,
        intentBuilder: Intent.() -> Unit = {}
    ) {
        Intent(context, MusicService::class.java).apply {
            putExtra(MusicService.TAG.ACTION, action)
            setAction(MusicService.Action.START_FOREGROUND_ACTION)
            intentBuilder()
        }.also { context.startService(it) }
    }

    override fun onListPositionChanged(
        context: Context,
        list: MutableList<MusicModel>,
        position: Int
    ) {
        executeServiceAction(context, MusicService.Action.CHANGE_POSITION) {
            putExtra(MusicService.TAG.POSITION, position)
            putParcelableArrayListExtra(MusicService.TAG.NEW_LIST, ArrayList(list))
        }
    }

    override fun onPlay(context: Context, position: Int) {
        executeServiceAction(context, MusicService.Action.START_MODE) {
            putExtra(MusicService.TAG.POSITION, position)
        }
    }

    override fun onRestart(context: Context, position: Int) {
        executeServiceAction(context, MusicService.Action.RESTART_MODE) {
            putExtra(MusicService.TAG.POSITION, position)
        }
    }

    override fun onPause(context: Context) {
        executeServiceAction(context, MusicService.Action.PAUSE_MODE)
    }

    override fun onStop(context: Context) {
        executeServiceAction(context, MusicService.Action.STOP_MODE)
    }

    override fun onShuffle(context: Context, isEnable: Boolean) {
        executeServiceAction(context, MusicService.Action.SHUFFLE_MODE) {
            putExtra(MusicService.TAG.IS_SHUFFLE, isEnable)
        }
    }

    override fun onRepeat(context: Context, repeatMode: Int) {
        executeServiceAction(context, MusicService.Action.RESTART_MODE) {
            putExtra(MusicService.TAG.REPEAT_MODE, repeatMode)
        }
    }
}