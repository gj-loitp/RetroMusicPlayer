package code.roy.retromusic.interfaces

import android.view.View
import code.roy.retromusic.db.PlaylistWithSongs

interface IPlaylistClickListener {
    fun onPlaylistClick(playlistWithSongs: PlaylistWithSongs, view: View)
}