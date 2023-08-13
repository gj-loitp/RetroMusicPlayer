package code.roy.retromusic.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
class Contributor(
    @SerializedName("name") val name: String = "",
    @SerializedName("summary") val summary: String = "",
    @SerializedName("link") val link: String = "",
    @SerializedName("image") val image: String = "",
) : Parcelable
