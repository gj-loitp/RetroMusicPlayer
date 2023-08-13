package code.roy.retromusic.model

import androidx.annotation.Keep
import androidx.annotation.StringRes
import code.roy.retromusic.HomeSection

@Keep
data class Home(
    val arrayList: List<Any>,
    @HomeSection
    val homeSection: Int,
    @StringRes
    val titleRes: Int,
)
