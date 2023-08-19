package code.roy.retromusic.model.playlist

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import kotlin.math.abs

@Keep
object PlaylistIdGenerator {

    operator fun invoke(name: String, @DrawableRes iconRes: Int): Long {
        return abs(31L * name.hashCode() + iconRes * name.hashCode() * 31L * 31L)
    }

}
