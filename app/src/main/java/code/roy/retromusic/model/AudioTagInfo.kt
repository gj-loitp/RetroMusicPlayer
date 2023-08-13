package code.roy.retromusic.model

import androidx.annotation.Keep
import org.jaudiotagger.tag.FieldKey

@Keep
class AudioTagInfo(
    val filePaths: List<String>?,
    val fieldKeyValueMap: Map<FieldKey, String>?,
    val artworkInfo: ArtworkInfo?,
)
