package code.roy.retromusic.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import code.roy.retromusic.FAVOURITES
import code.roy.retromusic.GENRES
import code.roy.retromusic.PLAYLISTS
import code.roy.retromusic.R
import code.roy.retromusic.RECENT_ALBUMS
import code.roy.retromusic.RECENT_ARTISTS
import code.roy.retromusic.TOP_ALBUMS
import code.roy.retromusic.TOP_ARTISTS
import code.roy.retromusic.db.HistoryEntity
import code.roy.retromusic.db.PlayCountEntity
import code.roy.retromusic.db.PlaylistEntity
import code.roy.retromusic.db.PlaylistWithSongs
import code.roy.retromusic.db.SongEntity
import code.roy.retromusic.db.fromHistoryToSongs
import code.roy.retromusic.db.toSong
import code.roy.retromusic.model.AbsCustomPlaylist
import code.roy.retromusic.model.Album
import code.roy.retromusic.model.Artist
import code.roy.retromusic.model.Contributor
import code.roy.retromusic.model.Genre
import code.roy.retromusic.model.Home
import code.roy.retromusic.model.Playlist
import code.roy.retromusic.model.Song
import code.roy.retromusic.model.smartplaylist.NotPlayedPlaylist
import code.roy.retromusic.network.LastFMService
import code.roy.retromusic.network.Result
import code.roy.retromusic.network.Result.Error
import code.roy.retromusic.network.Result.Success
import code.roy.retromusic.network.model.LastFmAlbum
import code.roy.retromusic.network.model.LastFmArtist
import code.roy.retromusic.util.logE
import code.roy.retromusic.fragments.search.Filter

interface Repository {

    fun historySong(): List<HistoryEntity>
    fun favorites(): LiveData<List<SongEntity>>
    fun observableHistorySongs(): LiveData<List<Song>>
    fun albumById(albumId: Long): Album
    fun playlistSongs(playListId: Long): LiveData<List<SongEntity>>
    suspend fun fetchAlbums(): List<Album>
    suspend fun albumByIdAsync(albumId: Long): Album
    suspend fun allSongs(): List<Song>
    suspend fun fetchArtists(): List<Artist>
    suspend fun albumArtists(): List<Artist>
    suspend fun fetchLegacyPlaylist(): List<Playlist>
    suspend fun fetchGenres(): List<Genre>
    suspend fun search(query: String?, filter: Filter): MutableList<Any>
    suspend fun getPlaylistSongs(playlist: Playlist): List<Song>
    suspend fun getGenre(genreId: Long): List<Song>
    suspend fun artistInfo(name: String, lang: String?, cache: String?): Result<LastFmArtist>
    suspend fun albumInfo(artist: String, album: String): Result<LastFmAlbum>
    suspend fun artistById(artistId: Long): Artist
    suspend fun albumArtistByName(name: String): Artist
    suspend fun recentArtists(): List<Artist>
    suspend fun topArtists(): List<Artist>
    suspend fun topAlbums(): List<Album>
    suspend fun recentAlbums(): List<Album>
    suspend fun recentArtistsHome(): Home
    suspend fun topArtistsHome(): Home
    suspend fun topAlbumsHome(): Home
    suspend fun recentAlbumsHome(): Home
    suspend fun favoritePlaylistHome(): Home
    suspend fun suggestions(): List<Song>
    suspend fun genresHome(): Home
    suspend fun playlists(): Home
    suspend fun homeSections(): List<Home>
    suspend fun playlist(playlistId: Long): Playlist
    suspend fun fetchPlaylistWithSongs(): List<PlaylistWithSongs>
    suspend fun playlistSongs(playlistWithSongs: PlaylistWithSongs): List<Song>
    suspend fun insertSongs(songs: List<SongEntity>)
    suspend fun checkPlaylistExists(playlistName: String): List<PlaylistEntity>
    suspend fun createPlaylist(playlistEntity: PlaylistEntity): Long
    suspend fun fetchPlaylists(): List<PlaylistEntity>
    suspend fun deleteRoomPlaylist(playlists: List<PlaylistEntity>)
    suspend fun renameRoomPlaylist(playlistId: Long, name: String)
    suspend fun deleteSongsInPlaylist(songs: List<SongEntity>)
    suspend fun removeSongFromPlaylist(songEntity: SongEntity)
    suspend fun deletePlaylistSongs(playlists: List<PlaylistEntity>)
    suspend fun favoritePlaylist(): PlaylistEntity
    suspend fun isFavoriteSong(songEntity: SongEntity): List<SongEntity>
    suspend fun upsertSongInHistory(currentSong: Song)
    suspend fun favoritePlaylistSongs(): List<SongEntity>
    suspend fun recentSongs(): List<Song>
    suspend fun topPlayedSongs(): List<Song>
    suspend fun upsertSongInPlayCount(playCountEntity: PlayCountEntity)
    suspend fun deleteSongInPlayCount(playCountEntity: PlayCountEntity)
    suspend fun deleteSongInHistory(songId: Long)
    suspend fun clearSongHistory()
    suspend fun findSongExistInPlayCount(songId: Long): PlayCountEntity?
    suspend fun playCountSongs(): List<PlayCountEntity>
    suspend fun deleteSongs(songs: List<Song>)
    suspend fun contributor(): List<Contributor>
    suspend fun searchArtists(query: String): List<Artist>
    suspend fun searchSongs(query: String): List<Song>
    suspend fun searchAlbums(query: String): List<Album>
    suspend fun isSongFavorite(songId: Long): Boolean
    fun getSongByGenre(genreId: Long): Song
    fun checkPlaylistExists(playListId: Long): LiveData<Boolean>
    fun getPlaylist(playlistId: Long): LiveData<PlaylistWithSongs>
}

class RealRepository(
    private val context: Context,
    private val lastFMService: LastFMService,
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val genreRepository: GenreRepository,
    private val lastAddedRepository: LastAddedRepository,
    private val playlistRepository: PlaylistRepository,
    private val searchRepository: RealSearchRepository,
    private val topPlayedRepository: TopPlayedRepository,
    private val roomRepository: RoomRepository,
    private val localDataRepository: LocalDataRepository,
) : Repository {

    override suspend fun deleteSongs(songs: List<Song>) = roomRepository.deleteSongs(songs)

    override suspend fun contributor(): List<Contributor> = localDataRepository.contributors()

    override suspend fun searchSongs(query: String): List<Song> = songRepository.songs(query)

    override suspend fun searchAlbums(query: String): List<Album> = albumRepository.albums(query)

    override suspend fun isSongFavorite(songId: Long): Boolean =
        roomRepository.isSongFavorite(context, songId)

    override fun getSongByGenre(genreId: Long): Song = genreRepository.song(genreId)

    override suspend fun searchArtists(query: String): List<Artist> =
        artistRepository.artists(query)

    override suspend fun fetchAlbums(): List<Album> = albumRepository.albums()

    override suspend fun albumByIdAsync(albumId: Long): Album = albumRepository.album(albumId)

    override fun albumById(albumId: Long): Album = albumRepository.album(albumId)

    override suspend fun fetchArtists(): List<Artist> = artistRepository.artists()

    override suspend fun albumArtists(): List<Artist> = artistRepository.albumArtists()

    override suspend fun artistById(artistId: Long): Artist = artistRepository.artist(artistId)

    override suspend fun albumArtistByName(name: String): Artist =
        artistRepository.albumArtist(name)

    override suspend fun recentArtists(): List<Artist> = lastAddedRepository.recentArtists()

    override suspend fun recentAlbums(): List<Album> = lastAddedRepository.recentAlbums()

    override suspend fun topArtists(): List<Artist> = topPlayedRepository.topArtists()

    override suspend fun topAlbums(): List<Album> = topPlayedRepository.topAlbums()

    override suspend fun fetchLegacyPlaylist(): List<Playlist> = playlistRepository.playlists()

    override suspend fun fetchGenres(): List<Genre> = genreRepository.genres()

    override suspend fun allSongs(): List<Song> = songRepository.songs()

    override suspend fun search(query: String?, filter: Filter): MutableList<Any> =
        searchRepository.searchAll(context, query, filter)

    override suspend fun getPlaylistSongs(playlist: Playlist): List<Song> =
        if (playlist is AbsCustomPlaylist) {
            playlist.songs()
        } else {
            PlaylistSongsLoader.getPlaylistSongList(context, playlist.id)
        }

    override suspend fun getGenre(genreId: Long): List<Song> = genreRepository.songs(genreId)

    override suspend fun artistInfo(
        name: String,
        lang: String?,
        cache: String?,
    ): Result<LastFmArtist> {
        return try {
            Success(lastFMService.artistInfo(name, lang, cache))
        } catch (e: Exception) {
            logE(e)
            Error(e)
        }
    }

    override suspend fun albumInfo(
        artist: String,
        album: String,
    ): Result<LastFmAlbum> {
        return try {
            val lastFmAlbum = lastFMService.albumInfo(artist, album)
            Success(lastFmAlbum)
        } catch (e: Exception) {
            logE(e)
            Error(e)
        }
    }

    override suspend fun homeSections(): List<Home> {
        val homeSections = mutableListOf<Home>()
        val sections: List<Home> = listOf(
            topArtistsHome(),
            topAlbumsHome(),
            recentArtistsHome(),
            recentAlbumsHome(),
            favoritePlaylistHome()
        )
        for (section in sections) {
            if (section.arrayList.isNotEmpty()) {
                homeSections.add(section)
            }
        }
        return homeSections
    }


    override suspend fun playlist(playlistId: Long) =
        playlistRepository.playlist(playlistId)

    override suspend fun fetchPlaylistWithSongs(): List<PlaylistWithSongs> =
        roomRepository.playlistWithSongs()

    override fun getPlaylist(playlistId: Long): LiveData<PlaylistWithSongs> =
        roomRepository.getPlaylist(playlistId)

    override suspend fun playlistSongs(playlistWithSongs: PlaylistWithSongs): List<Song> =
        playlistWithSongs.songs.map {
            it.toSong()
        }

    override fun playlistSongs(playListId: Long): LiveData<List<SongEntity>> =
        roomRepository.getSongs(playListId)

    override suspend fun insertSongs(songs: List<SongEntity>) =
        roomRepository.insertSongs(songs)

    override suspend fun checkPlaylistExists(playlistName: String): List<PlaylistEntity> =
        roomRepository.checkPlaylistExists(playlistName)

    override fun checkPlaylistExists(playListId: Long): LiveData<Boolean> =
        roomRepository.checkPlaylistExists(playListId)

    override suspend fun createPlaylist(playlistEntity: PlaylistEntity): Long =
        roomRepository.createPlaylist(playlistEntity)

    override suspend fun fetchPlaylists(): List<PlaylistEntity> = roomRepository.playlists()

    override suspend fun deleteRoomPlaylist(playlists: List<PlaylistEntity>) =
        roomRepository.deletePlaylistEntities(playlists)

    override suspend fun renameRoomPlaylist(playlistId: Long, name: String) =
        roomRepository.renamePlaylistEntity(playlistId, name)

    override suspend fun deleteSongsInPlaylist(songs: List<SongEntity>) =
        roomRepository.deleteSongsInPlaylist(songs)

    override suspend fun removeSongFromPlaylist(songEntity: SongEntity) =
        roomRepository.removeSongFromPlaylist(songEntity)

    override suspend fun deletePlaylistSongs(playlists: List<PlaylistEntity>) =
        roomRepository.deletePlaylistSongs(playlists)

    override suspend fun favoritePlaylist(): PlaylistEntity =
        roomRepository.favoritePlaylist(context.getString(R.string.favorites))

    override suspend fun isFavoriteSong(songEntity: SongEntity): List<SongEntity> =
        roomRepository.isFavoriteSong(songEntity)

    override suspend fun upsertSongInHistory(currentSong: Song) =
        roomRepository.upsertSongInHistory(currentSong)

    override suspend fun favoritePlaylistSongs(): List<SongEntity> =
        roomRepository.favoritePlaylistSongs(context.getString(R.string.favorites))

    override suspend fun recentSongs(): List<Song> = lastAddedRepository.recentSongs()

    override suspend fun topPlayedSongs(): List<Song> = topPlayedRepository.topTracks()

    override suspend fun upsertSongInPlayCount(playCountEntity: PlayCountEntity) =
        roomRepository.upsertSongInPlayCount(playCountEntity)

    override suspend fun deleteSongInPlayCount(playCountEntity: PlayCountEntity) =
        roomRepository.deleteSongInPlayCount(playCountEntity)

    override suspend fun deleteSongInHistory(songId: Long) =
        roomRepository.deleteSongInHistory(songId)

    override suspend fun clearSongHistory() {
        roomRepository.clearSongHistory()
    }

    override suspend fun findSongExistInPlayCount(songId: Long): PlayCountEntity? =
        roomRepository.findSongExistInPlayCount(songId)

    override suspend fun playCountSongs(): List<PlayCountEntity> =
        roomRepository.playCountSongs()

    override fun observableHistorySongs(): LiveData<List<Song>> =
        roomRepository.observableHistorySongs().map {
            it.fromHistoryToSongs()
        }

    override fun historySong(): List<HistoryEntity> =
        roomRepository.historySongs()

    override fun favorites(): LiveData<List<SongEntity>> =
        roomRepository.favoritePlaylistLiveData(context.getString(R.string.favorites))

    override suspend fun suggestions(): List<Song> {
        return NotPlayedPlaylist().songs().shuffled().takeIf {
            it.size > 9
        } ?: emptyList()
    }

    override suspend fun genresHome(): Home {
        val genres = genreRepository.genres().shuffled()
        return Home(arrayList = genres, homeSection = GENRES, titleRes = R.string.genres)
    }

    override suspend fun playlists(): Home {
        val playlist = playlistRepository.playlists()
        return Home(arrayList = playlist, homeSection = PLAYLISTS, titleRes = R.string.playlists)
    }

    override suspend fun recentArtistsHome(): Home {
        val artists = lastAddedRepository.recentArtists().take(5)
        return Home(
            arrayList = artists,
            homeSection = RECENT_ARTISTS,
            titleRes = R.string.recent_artists
        )
    }

    override suspend fun recentAlbumsHome(): Home {
        val albums = lastAddedRepository.recentAlbums().take(5)
        return Home(
            arrayList = albums,
            homeSection = RECENT_ALBUMS,
            titleRes = R.string.recent_albums
        )
    }

    override suspend fun topAlbumsHome(): Home {
        val albums = topPlayedRepository.topAlbums().take(5)
        return Home(arrayList = albums, homeSection = TOP_ALBUMS, titleRes = R.string.top_albums)
    }

    override suspend fun topArtistsHome(): Home {
        val artists = topPlayedRepository.topArtists().take(5)
        return Home(arrayList = artists, homeSection = TOP_ARTISTS, titleRes = R.string.top_artists)
    }

    override suspend fun favoritePlaylistHome(): Home {
        val songs = favoritePlaylistSongs().map {
            it.toSong()
        }
        return Home(arrayList = songs, homeSection = FAVOURITES, titleRes = R.string.favorites)
    }
}