package code.roy.retromusic.model

import android.graphics.Bitmap
import androidx.annotation.Keep

@Keep
class ArtworkInfo constructor(val albumId: Long, val artwork: Bitmap?)
