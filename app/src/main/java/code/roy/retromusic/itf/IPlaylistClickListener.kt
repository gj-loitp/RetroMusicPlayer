package code.roy.retromusic.itf

import android.view.View
import code.roy.retromusic.db.PlaylistWithSongs

interface IPlaylistClickListener {
    fun onPlaylistClick(
        playlistWithSongs: PlaylistWithSongs,
        view: View,
    )
}
