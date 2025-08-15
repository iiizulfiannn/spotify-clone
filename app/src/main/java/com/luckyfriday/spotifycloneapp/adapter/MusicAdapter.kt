package com.luckyfriday.spotifycloneapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luckyfriday.spotifycloneapp.R
import com.luckyfriday.spotifycloneapp.listener.ItemMoveCallback
import com.luckyfriday.spotifycloneapp.databinding.ItemMusicBinding
import com.luckyfriday.spotifycloneapp.listener.MusicListener
import com.luckyfriday.spotifycloneapp.model.MusicModel
import java.util.Collections

class MusicAdapter(
    private var listData: MutableList<MusicModel>,
    private var listener: MusicListener
) :
    RecyclerView.Adapter<MusicAdapter.ViewHolder>(), ItemMoveCallback.ItemTouchHelperContract {
    private var selectedMusicPlayed: MusicModel? = null

    inner class ViewHolder(val itemBinding: ItemMusicBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(data: MusicModel, selectedMusic: MusicModel?, listener: MusicListener) {
            itemBinding.tvTitleSong.text = data.title
            itemBinding.tvDescriptionSong.text = data.description

            // handle when music is playing
            itemBinding.ivSelectedButton.setOnClickListener {
                listener.onClicked(data)
            }
            if (data.title == selectedMusic?.title) {
                itemBinding.tvTitleSong.setTextColor(itemBinding.root.context.getColor(R.color.green_1ed760))
                itemBinding.tvDescriptionSong.setTextColor(itemBinding.root.context.getColor(R.color.green_1ed760))
            } else {
                itemBinding.tvTitleSong.setTextColor(itemBinding.root.context.getColor(R.color.white))
                itemBinding.tvDescriptionSong.setTextColor(itemBinding.root.context.getColor(R.color.white))
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    internal fun setSelectedMusic(selectedMusic: MusicModel?) {
        this.selectedMusicPlayed = selectedMusic
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listData[position], selectedMusicPlayed, listener)
    }

    override fun getItemCount(): Int = listData.size

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(listData, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(listData, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        listener.onPositionChanged(listData, fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: RecyclerView.ViewHolder) {
    }

    override fun onRowDelete(myViewHolder: RecyclerView.ViewHolder) {
    }
}