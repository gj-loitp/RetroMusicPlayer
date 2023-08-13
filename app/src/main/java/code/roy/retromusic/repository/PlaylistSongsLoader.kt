package code.roy.retromusic.repository

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore.Audio.AudioColumns
import android.provider.MediaStore.Audio.Playlists.Members
import code.roy.retromusic.Constants
import code.roy.retromusic.Constants.IS_MUSIC
import code.roy.retromusic.extensions.getInt
import code.roy.retromusic.extensions.getLong
import code.roy.retromusic.extensions.getString
import code.roy.retromusic.extensions.getStringOrNull
import code.roy.retromusic.model.PlaylistSong
import code.roy.retromusic.model.Song

@Suppress("Deprecation")
object PlaylistSongsLoader {

    @JvmStatic
    fun getPlaylistSongList(context: Context, playlistId: Long): List<Song> {
        val songs = mutableListOf<Song>()
        val cursor =
            makePlaylistSongCursor(
                context = context,
                playlistId = playlistId
            )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(
                    getPlaylistSongFromCursorImpl(
                        cursor = cursor,
                        playlistId = playlistId
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    // TODO duplicated in [PlaylistRepository.kt]
    private fun getPlaylistSongFromCursorImpl(cursor: Cursor, playlistId: Long): PlaylistSong {
        val id = cursor.getLong(Members.AUDIO_ID)
        val title = cursor.getString(AudioColumns.TITLE)
        val trackNumber = cursor.getInt(AudioColumns.TRACK)
        val year = cursor.getInt(AudioColumns.YEAR)
        val duration = cursor.getLong(AudioColumns.DURATION)
        val data = cursor.getString(Constants.DATA)
        val dateModified = cursor.getLong(AudioColumns.DATE_MODIFIED)
        val albumId = cursor.getLong(AudioColumns.ALBUM_ID)
        val albumName = cursor.getString(AudioColumns.ALBUM)
        val artistId = cursor.getLong(AudioColumns.ARTIST_ID)
        val artistName = cursor.getString(AudioColumns.ARTIST)
        val idInPlaylist = cursor.getLong(Members._ID)
        val composer = cursor.getStringOrNull(AudioColumns.COMPOSER)
        val albumArtist = cursor.getStringOrNull("album_artist")
        return PlaylistSong(
            id = id,
            title = title,
            trackNumber = trackNumber,
            year = year,
            duration = duration,
            data = data,
            dateModified = dateModified,
            albumId = albumId,
            albumName = albumName,
            artistId = artistId,
            artistName = artistName,
            playlistId = playlistId,
            idInPlayList = idInPlaylist,
            composer = composer,
            albumArtist = albumArtist
        )
    }

    private fun makePlaylistSongCursor(context: Context, playlistId: Long): Cursor? {
        try {
            return context.contentResolver.query(
                /* uri = */ Members.getContentUri("external", playlistId),
                /* projection = */
                arrayOf(
                    Members.AUDIO_ID, // 0
                    AudioColumns.TITLE, // 1
                    AudioColumns.TRACK, // 2
                    AudioColumns.YEAR, // 3
                    AudioColumns.DURATION, // 4
                    Constants.DATA, // 5
                    AudioColumns.DATE_MODIFIED, // 6
                    AudioColumns.ALBUM_ID, // 7
                    AudioColumns.ALBUM, // 8
                    AudioColumns.ARTIST_ID, // 9
                    AudioColumns.ARTIST, // 10
                    Members._ID,//11
                    AudioColumns.COMPOSER,//12
                    "album_artist"//13
                ), /* selection = */
                IS_MUSIC, /* selectionArgs = */
                null, /* sortOrder = */
                Members.DEFAULT_SORT_ORDER
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
            return null
        }
    }
}
