package com.luckyfriday.spotifycloneapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luckyfriday.spotifycloneapp.databinding.ItemMusicBinding

class MusicAdapter(private var listData: MutableList<MusicModel>) :
    RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

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
}