package code.roy.retromusic

import androidx.room.Room
import code.roy.monkey.retromusic.cast.RetroWebServer
import code.roy.retromusic.auto.AutoMusicProvider
import code.roy.retromusic.db.MIGRATION_23_24
import code.roy.retromusic.db.RetroDatabase
import code.roy.retromusic.fragments.LibraryViewModel
import code.roy.retromusic.fragments.albums.AlbumDetailsViewModel
import code.roy.retromusic.fragments.artists.ArtistDetailsViewModel
import code.roy.retromusic.fragments.genres.GenreDetailsViewModel
import code.roy.retromusic.fragments.playlists.PlaylistDetailsViewModel
import code.roy.retromusic.model.Genre
import code.roy.retromusic.network.provideDefaultCache
import code.roy.retromusic.network.provideLastFmRest
import code.roy.retromusic.network.provideLastFmRetrofit
import code.roy.retromusic.network.provideOkHttp
import code.roy.retromusic.repository.AlbumRepository
import code.roy.retromusic.repository.ArtistRepository
import code.roy.retromusic.repository.GenreRepository
import code.roy.retromusic.repository.LastAddedRepository
import code.roy.retromusic.repository.LocalDataRepository
import code.roy.retromusic.repository.PlaylistRepository
import code.roy.retromusic.repository.RealAlbumRepository
import code.roy.retromusic.repository.RealArtistRepository
import code.roy.retromusic.repository.RealGenreRepository
import code.roy.retromusic.repository.RealLastAddedRepository
import code.roy.retromusic.repository.RealLocalDataRepository
import code.roy.retromusic.repository.RealPlaylistRepository
import code.roy.retromusic.repository.RealRepository
import code.roy.retromusic.repository.RealRoomRepository
import code.roy.retromusic.repository.RealSearchRepository
import code.roy.retromusic.repository.RealSongRepository
import code.roy.retromusic.repository.RealTopPlayedRepository
import code.roy.retromusic.repository.Repository
import code.roy.retromusic.repository.RoomRepository
import code.roy.retromusic.repository.SongRepository
import code.roy.retromusic.repository.TopPlayedRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {

    factory {
        provideDefaultCache()
    }
    factory {
        provideOkHttp(get(), get())
    }
    single {
        provideLastFmRetrofit(get())
    }
    single {
        provideLastFmRest(get())
    }
}

private val roomModule = module {

    single {
        Room.databaseBuilder(androidContext(), RetroDatabase::class.java, "playlist.db")
            .addMigrations(MIGRATION_23_24)
            .build()
    }

    factory {
        get<RetroDatabase>().playlistDao()
    }

    factory {
        get<RetroDatabase>().playCountDao()
    }

    factory {
        get<RetroDatabase>().historyDao()
    }

    single {
        RealRoomRepository(get(), get(), get())
    } bind RoomRepository::class
}
private val autoModule = module {
    single {
        AutoMusicProvider(
            androidContext(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
private val mainModule = module {
    single {
        androidContext().contentResolver
    }
    single {
        RetroWebServer(get())
    }
}
private val dataModule = module {
    single {
        RealRepository(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    } bind Repository::class

    single {
        RealSongRepository(get())
    } bind SongRepository::class

    single {
        RealGenreRepository(get(), get())
    } bind GenreRepository::class

    single {
        RealAlbumRepository(get())
    } bind AlbumRepository::class

    single {
        RealArtistRepository(get(), get())
    } bind ArtistRepository::class

    single {
        RealPlaylistRepository(get())
    } bind PlaylistRepository::class

    single {
        RealTopPlayedRepository(get(), get(), get(), get())
    } bind TopPlayedRepository::class

    single {
        RealLastAddedRepository(
            get(),
            get(),
            get()
        )
    } bind LastAddedRepository::class

    single {
        RealSearchRepository(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single {
        RealLocalDataRepository(get())
    } bind LocalDataRepository::class
}

private val viewModules = module {

    viewModel {
        LibraryViewModel(get())
    }

    viewModel { (albumId: Long) ->
        AlbumDetailsViewModel(
            get(),
            albumId
        )
    }

    viewModel { (artistId: Long?, artistName: String?) ->
        ArtistDetailsViewModel(
            get(),
            artistId,
            artistName
        )
    }

    viewModel { (playlistId: Long) ->
        PlaylistDetailsViewModel(
            get(),
            playlistId
        )
    }

    viewModel { (genre: Genre) ->
        GenreDetailsViewModel(
            get(),
            genre
        )
    }
}

val appModules = listOf(mainModule, dataModule, autoModule, viewModules, networkModule, roomModule)