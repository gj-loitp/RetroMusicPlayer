package code.roy.retromusic.glide.playlistPreview

import code.roy.retromusic.db.PlaylistEntity
import code.roy.retromusic.db.PlaylistWithSongs
import code.roy.retromusic.db.toSongs
import code.roy.retromusic.model.Song

class PlaylistPreview(val playlistWithSongs: PlaylistWithSongs) {

    val playlistEntity: PlaylistEntity get() = playlistWithSongs.playlistEntity
    val songs: List<Song> get() = playlistWithSongs.songs.toSongs()

    override fun equals(other: Any?): Boolean {
        println("Glide equals $this $other")
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaylistPreview
        if (other.playlistEntity.playListId != playlistEntity.playListId) return false
        if (other.songs.size != songs.size) return false
        return true
    }

    override fun hashCode(): Int {
        var result = playlistEntity.playListId.hashCode()
        result = 31 * result + playlistWithSongs.songs.size
        println("Glide $result")
        return result
    }
}
