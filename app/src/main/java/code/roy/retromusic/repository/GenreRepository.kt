package code.roy.retromusic.repository

import android.content.ContentResolver
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore.Audio.Genres
import code.roy.retromusic.Constants.IS_MUSIC
import code.roy.retromusic.Constants.baseProjection
import code.roy.retromusic.extensions.getLong
import code.roy.retromusic.extensions.getStringOrNull
import code.roy.retromusic.model.Genre
import code.roy.retromusic.model.Song
import code.roy.retromusic.util.PreferenceUtil

interface GenreRepository {
    fun genres(query: String): List<Genre>

    fun genres(): List<Genre>

    fun songs(genreId: Long): List<Song>

    fun song(genreId: Long): Song
}

class RealGenreRepository(
    private val contentResolver: ContentResolver,
    private val songRepository: RealSongRepository,
) : GenreRepository {

    override fun genres(query: String): List<Genre> {
        return getGenresFromCursor(makeGenreCursor(query))
    }

    override fun genres(): List<Genre> {
        return getGenresFromCursor(makeGenreCursor())
    }

    override fun songs(genreId: Long): List<Song> {
        // The genres table only stores songs that have a genre specified,
        // so we need to get songs without a genre a different way.
        return if (genreId == -1L) {
            getSongsWithNoGenre()
        } else songRepository.songs(makeGenreSongCursor(genreId))
    }

    override fun song(genreId: Long): Song {
        return songRepository.song(makeGenreSongCursor(genreId))
    }

    private fun getSongCount(genreId: Long): Int {
        contentResolver.query(
            /* uri = */ Genres.Members.getContentUri("external", genreId),
            /* projection = */ null,
            /* selection = */ null,
            /* selectionArgs = */ null,
            /* sortOrder = */ null
        ).use {
            return it?.count ?: 0
        }
    }

    private fun getGenreFromCursor(cursor: Cursor): Genre {
        val id = cursor.getLong(Genres._ID)
        val name = cursor.getStringOrNull(Genres.NAME)
        val songCount = getSongCount(id)
        return Genre(id, name ?: "", songCount)
    }

    private fun getSongsWithNoGenre(): List<Song> {
        val selection =
            BaseColumns._ID + " NOT IN " + "(SELECT " + Genres.Members.AUDIO_ID + " FROM audio_genres_map)"
        return songRepository.songs(songRepository.makeSongCursor(selection, null))
    }

    private fun makeGenreSongCursor(genreId: Long): Cursor? {
        return try {
            contentResolver.query(
                /* uri = */ Genres.Members.getContentUri("external", genreId),
                /* projection = */ baseProjection,
                /* selection = */ IS_MUSIC,
                /* selectionArgs = */ null,
                /* sortOrder = */ PreferenceUtil.songSortOrder
            )
        } catch (e: SecurityException) {
            return null
        }
    }

    private fun getGenresFromCursor(cursor: Cursor?): ArrayList<Genre> {
        val genres = arrayListOf<Genre>()
        cursor?.use {
            if (cursor.moveToFirst()) {
                do {
                    val genre = getGenreFromCursor(cursor)
                    if (genre.songCount > 0) {
                        genres.add(genre)
                    }
                } while (cursor.moveToNext())
            }
        }
        return genres
    }

    private fun makeGenreCursor(): Cursor? {
        val projection = arrayOf(Genres._ID, Genres.NAME)
        return try {
            contentResolver.query(
                /* uri = */ Genres.EXTERNAL_CONTENT_URI,
                /* projection = */ projection,
                /* selection = */ null,
                /* selectionArgs = */ null,
                /* sortOrder = */ PreferenceUtil.genreSortOrder
            )
        } catch (e: SecurityException) {
            return null
        }
    }

    private fun makeGenreCursor(query: String): Cursor? {
        val projection = arrayOf(Genres._ID, Genres.NAME)
        return try {
            contentResolver.query(
                /* uri = */ Genres.EXTERNAL_CONTENT_URI,
                /* projection = */ projection,
                /* selection = */ Genres.NAME + " = ?",
                /* selectionArgs = */ arrayOf(query),
                /* sortOrder = */ PreferenceUtil.genreSortOrder
            )
        } catch (e: SecurityException) {
            return null
        }
    }
}
