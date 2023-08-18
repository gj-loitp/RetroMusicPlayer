package code.roy.retromusic.db

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity
class LyricsEntity(
    @PrimaryKey val songId: Int,
    val lyrics: String,
)
