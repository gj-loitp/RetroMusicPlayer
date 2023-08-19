package code.roy.retromusic.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import code.roy.retromusic.R
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class CategoryInfo(
    val category: Category,
    var visible: Boolean,
) : Parcelable {

    @Keep
    enum class Category(
        val id: Int,
        @StringRes val stringRes: Int,
        @DrawableRes val icon: Int,
    ) {
        Home(id = R.id.action_home, stringRes = R.string.for_you, icon = R.drawable.asld_face),
        Songs(id = R.id.action_song, stringRes = R.string.songs, icon = R.drawable.asld_music_note),
        Albums(id = R.id.action_album, stringRes = R.string.albums, icon = R.drawable.asld_album),
        Artists(
            id = R.id.action_artist,
            stringRes = R.string.artists,
            icon = R.drawable.asld_artist
        ),
        Playlists(
            id = R.id.action_playlist,
            stringRes = R.string.playlists,
            icon = R.drawable.asld_playlist
        ),
        Genres(id = R.id.action_genre, stringRes = R.string.genres, icon = R.drawable.asld_guitar),
        Folder(
            id = R.id.action_folder,
            stringRes = R.string.folders,
            icon = R.drawable.asld_folder
        ),
        Search(
            id = R.id.action_search,
            stringRes = R.string.action_search,
            icon = R.drawable.ic_search
        );
    }
}
