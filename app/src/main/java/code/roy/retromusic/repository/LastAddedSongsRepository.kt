package code.roy.retromusic.repository

import android.database.Cursor
import android.provider.MediaStore
import code.roy.retromusic.model.Album
import code.roy.retromusic.model.Artist
import code.roy.retromusic.model.Song
import code.roy.retromusic.util.PreferenceUtil

interface LastAddedRepository {
    fun recentSongs(): List<Song>

    fun recentAlbums(): List<Album>

    fun recentArtists(): List<Artist>
}

class RealLastAddedRepository(
    private val songRepository: RealSongRepository,
    private val albumRepository: RealAlbumRepository,
    private val artistRepository: RealArtistRepository,
) : LastAddedRepository {
    override fun recentSongs(): List<Song> {
        return songRepository.songs(makeLastAddedCursor())
    }

    override fun recentAlbums(): List<Album> {
        return albumRepository.splitIntoAlbums(songs = recentSongs(), sorted = false)
    }

    override fun recentArtists(): List<Artist> {
        return artistRepository.splitIntoArtists(recentAlbums())
    }

    private fun makeLastAddedCursor(): Cursor? {
        val cutoff = PreferenceUtil.lastAddedCutoff
        return songRepository.makeSongCursor(
            selection = MediaStore.Audio.Media.DATE_ADDED + ">?",
            selectionValues = arrayOf(cutoff.toString()),
            sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC"
        )
    }
}
