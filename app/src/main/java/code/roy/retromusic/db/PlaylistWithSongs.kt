package code.roy.retromusic.db

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class PlaylistWithSongs(
    @Embedded val playlistEntity: PlaylistEntity,
    @Relation(
        parentColumn = "playlist_id",
        entityColumn = "playlist_creator_id"
    )
    val songs: List<SongEntity>,
) : Parcelable
