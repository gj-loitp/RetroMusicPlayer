package code.roy.retromusic.itf

import android.view.View

interface IArtistClickListener {
    fun onArtist(artistId: Long, view: View)
}
