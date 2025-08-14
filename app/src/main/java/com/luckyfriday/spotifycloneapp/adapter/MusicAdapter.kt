package com.luckyfriday.spotifycloneapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luckyfriday.spotifycloneapp.ItemMoveCallback
import com.luckyfriday.spotifycloneapp.databinding.ItemMusicBinding
import com.luckyfriday.spotifycloneapp.model.MusicModel
import java.util.Collections

class MusicAdapter(private var listData: MutableList<MusicModel>) :
    RecyclerView.Adapter<MusicAdapter.ViewHolder>(), ItemMoveCallback.ItemTouchHelperContract {

    inner class ViewHolder(val itemBinding: ItemMusicBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(data: MusicModel) {
            itemBinding.tvTitleSong.text = data.title
            itemBinding.tvDescriptionSong.text = data.description

            // handle when music is playing
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listData[position])
    }

    override fun getItemCount(): Int = listData.size

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(listData, i, i+1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(listData, i, i-1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: RecyclerView.ViewHolder) {
    }

    override fun onRowDelete(myViewHolder: RecyclerView.ViewHolder) {
    }
}