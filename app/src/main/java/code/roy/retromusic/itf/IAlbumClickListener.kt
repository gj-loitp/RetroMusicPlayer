package code.roy.retromusic.itf

import android.view.View

interface IAlbumClickListener {
    fun onAlbumClick(albumId: Long, view: View)
}
