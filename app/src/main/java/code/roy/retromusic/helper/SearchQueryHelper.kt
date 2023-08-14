package code.roy.retromusic.helper

import android.app.SearchManager
import android.os.Bundle
import android.provider.MediaStore
import code.roy.retromusic.model.Song
import code.roy.retromusic.repository.RealSongRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object SearchQueryHelper : KoinComponent {
    private const val TITLE_SELECTION = "lower(" + MediaStore.Audio.AudioColumns.TITLE + ") = ?"
    private const val ALBUM_SELECTION = "lower(" + MediaStore.Audio.AudioColumns.ALBUM + ") = ?"
    private const val ARTIST_SELECTION = "lower(" + MediaStore.Audio.AudioColumns.ARTIST + ") = ?"
    private const val AND = " AND "
    private val songRepository by inject<RealSongRepository>()
    var songs = ArrayList<Song>()

    @JvmStatic
    fun getSongs(extras: Bundle): List<Song> {
        val query = extras.getString(SearchManager.QUERY, null)
        val artistName = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST, null)
        val albumName = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM, null)
        val titleName = extras.getString(MediaStore.EXTRA_MEDIA_TITLE, null)

        var songs = listOf<Song>()
        if (artistName != null && albumName != null && titleName != null) {
            songs = songRepository.songs(
                songRepository.makeSongCursor(
                    selection = ARTIST_SELECTION + AND + ALBUM_SELECTION + AND + TITLE_SELECTION,
                    selectionValues = arrayOf(
                        artistName.lowercase(),
                        albumName.lowercase(),
                        titleName.lowercase()
                    )
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        if (artistName != null && titleName != null) {
            songs = songRepository.songs(
                songRepository.makeSongCursor(
                    selection = ARTIST_SELECTION + AND + TITLE_SELECTION,
                    selectionValues = arrayOf(
                        artistName.lowercase(),
                        titleName.lowercase()
                    )
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        if (albumName != null && titleName != null) {
            songs = songRepository.songs(
                songRepository.makeSongCursor(
                    selection = ALBUM_SELECTION + AND + TITLE_SELECTION,
                    selectionValues = arrayOf(
                        albumName.lowercase(),
                        titleName.lowercase()
                    )
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        if (artistName != null) {
            songs = songRepository.songs(
                songRepository.makeSongCursor(
                    selection = ARTIST_SELECTION,
                    selectionValues = arrayOf(artistName.lowercase())
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        if (albumName != null) {
            songs = songRepository.songs(
                songRepository.makeSongCursor(
                    selection = ALBUM_SELECTION,
                    selectionValues = arrayOf(albumName.lowercase())
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        if (titleName != null) {
            songs = songRepository.songs(
                songRepository.makeSongCursor(
                    selection = TITLE_SELECTION,
                    selectionValues = arrayOf(titleName.lowercase())
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        songs = songRepository.songs(
            songRepository.makeSongCursor(
                selection = ARTIST_SELECTION,
                selectionValues = arrayOf(query.lowercase())
            )
        )

        if (songs.isNotEmpty()) {
            return songs
        }
        songs = songRepository.songs(
            songRepository.makeSongCursor(
                selection = ALBUM_SELECTION,
                selectionValues = arrayOf(query.lowercase())
            )
        )
        if (songs.isNotEmpty()) {
            return songs
        }
        songs = songRepository.songs(
            songRepository.makeSongCursor(
                selection = TITLE_SELECTION,
                selectionValues = arrayOf(query.lowercase())
            )
        )
        return songs.ifEmpty { ArrayList() }
    }
}
