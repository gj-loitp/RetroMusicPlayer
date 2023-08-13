package code.roy.retromusic.util

import code.roy.retromusic.db.PlaylistWithSongs
import code.roy.retromusic.helper.M3UWriter.writeIO
import java.io.File
import java.io.IOException

object PlaylistsUtil {
    @Throws(IOException::class)
    fun savePlaylistWithSongs(playlist: PlaylistWithSongs?): File {
        return writeIO(
            dir = File(getExternalStorageDirectory(), "Playlists"), playlistWithSongs = playlist!!
        )
    }
}
