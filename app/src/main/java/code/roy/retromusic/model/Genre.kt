package code.roy.retromusic.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Genre(
    val id: Long,
    val name: String,
    val songCount: Int,
) : Parcelable
