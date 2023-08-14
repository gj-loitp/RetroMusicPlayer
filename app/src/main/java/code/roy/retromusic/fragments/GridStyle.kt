package code.roy.retromusic.fragments

import androidx.annotation.LayoutRes
import code.roy.retromusic.R

enum class GridStyle constructor(
    @param:LayoutRes @field:LayoutRes val layoutResId: Int,
    val id: Int,
) {
    Grid(layoutResId = R.layout.v_item_grid, id = 0),
    Card(layoutResId = R.layout.v_item_card, id = 1),
    ColoredCard(layoutResId = R.layout.v_item_card_color, id = 2),
    Circular(layoutResId = R.layout.v_item_grid_circle, id = 3),
    Image(layoutResId = R.layout.v_image, id = 4),
    GradientImage(layoutResId = R.layout.v_item_image_gradient, id = 5)
}
