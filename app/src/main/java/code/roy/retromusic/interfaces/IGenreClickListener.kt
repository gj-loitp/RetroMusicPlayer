package code.roy.retromusic.interfaces

import android.view.View
import code.roy.retromusic.model.Genre

interface IGenreClickListener {
    fun onClickGenre(genre: Genre, view: View)
}