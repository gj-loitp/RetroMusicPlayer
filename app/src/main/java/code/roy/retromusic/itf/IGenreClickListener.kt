package code.roy.retromusic.itf

import android.view.View
import code.roy.retromusic.model.Genre

interface IGenreClickListener {
    fun onClickGenre(
        genre: Genre,
        view: View,
    )
}
