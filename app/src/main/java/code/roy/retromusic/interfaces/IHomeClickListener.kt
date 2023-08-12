package code.roy.retromusic.interfaces

import code.roy.retromusic.model.Album
import code.roy.retromusic.model.Artist
import code.roy.retromusic.model.Genre

interface IHomeClickListener {
    fun onAlbumClick(album: Album)

    fun onArtistClick(artist: Artist)

    fun onGenreClick(genre: Genre)
}