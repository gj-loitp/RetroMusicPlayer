package code.roy.retromusic.repository

import android.content.ContentResolver
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore.Audio.AudioColumns
import android.provider.MediaStore.Audio.Playlists.*
import android.provider.MediaStore.Audio.PlaylistsColumns
import code.roy.retromusic.Constants
import code.roy.retromusic.extensions.getInt
import code.roy.retromusic.extensions.getLong
import code.roy.retromusic.extensions.getString
import code.roy.retromusic.extensions.getStringOrNull
import code.roy.retromusic.model.Playlist
import code.roy.retromusic.model.PlaylistSong
import code.roy.retromusic.model.Song

interface PlaylistRepository {
    fun playlist(cursor: Cursor?): Playlist

    fun searchPlaylist(query: String): List<Playlist>

    fun playlist(playlistName: String): Playlist

    fun playlists(): List<Playlist>

    fun playlists(cursor: Cursor?): List<Playlist>

    fun favoritePlaylist(playlistName: String): List<Playlist>

    fun deletePlaylist(playlistId: Long)

    fun playlist(playlistId: Long): Playlist

    fun playlistSongs(playlistId: Long): List<Song>
}

@Suppress("Deprecation")
class RealPlaylistRepository(
    private val contentResolver: ContentResolver,
) : PlaylistRepository {

    override fun playlist(cursor: Cursor?): Playlist {
        return cursor.use {
            if (cursor?.moveToFirst() == true) {
                getPlaylistFromCursorImpl(cursor)
            } else {
                Playlist.empty
            }
        }
    }

    override fun playlist(playlistName: String): Playlist {
        return playlist(
            makePlaylistCursor(
                selection = PlaylistsColumns.NAME + "=?",
                values = arrayOf(playlistName)
            )
        )
    }

    override fun playlist(playlistId: Long): Playlist {
        return playlist(
            makePlaylistCursor(
                selection = BaseColumns._ID + "=?",
                values = arrayOf(playlistId.toString())
            )
        )
    }

    override fun searchPlaylist(query: String): List<Playlist> {
        return playlists(
            makePlaylistCursor(
                selection = PlaylistsColumns.NAME + "=?",
                values = arrayOf(query)
            )
        )
    }

    override fun playlists(): List<Playlist> {
        return playlists(makePlaylistCursor(selection = null, values = null))
    }

    override fun playlists(cursor: Cursor?): List<Playlist> {
        val playlists = mutableListOf<Playlist>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                playlists.add(getPlaylistFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return playlists
    }

    override fun favoritePlaylist(playlistName: String): List<Playlist> {
        return playlists(
            makePlaylistCursor(
                selection = PlaylistsColumns.NAME + "=?",
                values = arrayOf(playlistName)
            )
        )
    }

    override fun deletePlaylist(playlistId: Long) {
        val localUri = EXTERNAL_CONTENT_URI
        val localStringBuilder = StringBuilder()
        localStringBuilder.append("_id IN (")
        localStringBuilder.append(playlistId)
        localStringBuilder.append(")")
        contentResolver.delete(localUri, localStringBuilder.toString(), null)
    }

    private fun getPlaylistFromCursorImpl(
        cursor: Cursor,
    ): Playlist {
        val id = cursor.getLong(0)
        val name = cursor.getString(1)
        return if (name != null) {
            Playlist(id, name)
        } else {
            Playlist.empty
        }
    }

    override fun playlistSongs(playlistId: Long): List<Song> {
        val songs = arrayListOf<Song>()
        if (playlistId == -1L) return songs
        val cursor = makePlaylistSongCursor(playlistId)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getPlaylistSongFromCursorImpl(cursor, playlistId))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

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
            composer = composer ?: "",
            albumArtist = albumArtist
        )
    }

    private fun makePlaylistCursor(
        selection: String?,
        values: Array<String>?,
    ): Cursor? {
        return contentResolver.query(
            /* uri = */ EXTERNAL_CONTENT_URI,
            /* projection = */ arrayOf(
                BaseColumns._ID, /* 0 */
                PlaylistsColumns.NAME /* 1 */
            ),
            /* selection = */ selection,
            /* selectionArgs = */ values,
            /* sortOrder = */ DEFAULT_SORT_ORDER
        )
    }


    private fun makePlaylistSongCursor(playlistId: Long): Cursor? {
        return contentResolver.query(
            Members.getContentUri("external", playlistId),
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
            ), Constants.IS_MUSIC, null, Members.DEFAULT_SORT_ORDER
        )
    }
}
