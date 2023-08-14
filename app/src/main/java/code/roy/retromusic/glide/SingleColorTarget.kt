package code.roy.retromusic.glide

import android.graphics.drawable.Drawable
import android.widget.ImageView
import code.roy.retromusic.glide.palette.BitmapPaletteTarget
import code.roy.retromusic.glide.palette.BitmapPaletteWrapper
import code.roy.retromusic.util.ColorUtil
import code.roy.appthemehelper.util.ATHUtil
import com.bumptech.glide.request.transition.Transition

abstract class SingleColorTarget(view: ImageView) : BitmapPaletteTarget(view) {

    private val defaultFooterColor: Int
        get() = ATHUtil.resolveColor(view.context, androidx.appcompat.R.attr.colorControlNormal)

    abstract fun onColorReady(color: Int)

    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        onColorReady(defaultFooterColor)
    }

    override fun onResourceReady(
        resource: BitmapPaletteWrapper,
        transition: Transition<in BitmapPaletteWrapper>?,
    ) {
        super.onResourceReady(resource, transition)
        onColorReady(
            ColorUtil.getColor(
                /* palette = */ resource.palette,
                /* fallback = */ ATHUtil.resolveColor(view.context, androidx.appcompat.R.attr.colorPrimary)
            )
        )
    }
}
