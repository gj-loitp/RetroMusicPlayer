package code.roy.retromusic.glide.artistimage

import code.roy.retromusic.model.Artist

class ArtistImage(val artist: Artist) {
    override fun equals(other: Any?): Boolean {
        if (other is ArtistImage) {
            return other.artist == artist
        }
        return false
    }

    override fun hashCode(): Int {
        return artist.hashCode()
    }
}
