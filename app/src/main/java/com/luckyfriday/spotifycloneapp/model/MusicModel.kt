package com.luckyfriday.spotifycloneapp.model

import android.os.Parcelable
import androidx.annotation.RawRes
import com.luckyfriday.spotifycloneapp.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class MusicModel(
    var title: String,
    var description: String,
    @RawRes var musicFile: Int,
    var imageCover: String
) : Parcelable {
    companion object {
        fun getListMap(): ArrayList<MusicModel> {
            val list = ArrayList<MusicModel>()
            list.add(
                MusicModel(
                    "Song 1",
                    "This is song 1 description",
                    musicFile = R.raw.song_01,
                    "https://i.pinimg.com/474x/a5/e1/88/a5e18832d3bb6b750cc4c116903f5a27.jpg"
                )
            )
            list.add(
                MusicModel(
                    "Song 2",
                    "This is song 2 description",
                    musicFile = R.raw.song_02,
                    "https://upload.wikimedia.org/wikipedia/en/0/0f/Radiohead.pablohoney.albumart.jpg"
                )
            )
            list.add(
                MusicModel(
                    "Song 3",
                    "This is song 3 description",
                    musicFile = R.raw.song_03,
                    "https://www.billboard.com/wp-content/uploads/2023/09/diddy-billboard-cover-2-09132023-1500.jpg ?w=788"
                )
            )
            list.add(
                MusicModel(
                    "Song 4",
                    "This is song 4 description",
                    musicFile = R.raw.song_04,
                    "https://upload.wikimedia.org/wikipedia/en/0/04/A_Kind_Of_Hush_%28Carpenters_album%29.jpg"
                )
            )
            list.add(
                MusicModel(
                    "Song 5",
                    "This is song 5 description",
                    musicFile = R.raw.song_05,
                    "https://cdn-p.smehost.net/sites/28d35d54a3c64e2b851790a18a1c4c18/wp-content/uploads/2017/04/19 870831_bad_album.jpg"
                )
            )
            return list
        }
    }
}