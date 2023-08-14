package code.roy.retromusic.glide

import android.graphics.drawable.Drawable
import android.widget.ImageView
import code.roy.retromusic.App
import code.roy.retromusic.extensions.colorControlNormal
import code.roy.retromusic.glide.palette.BitmapPaletteTarget
import code.roy.retromusic.glide.palette.BitmapPaletteWrapper
import code.roy.retromusic.util.color.MediaNotificationProcessor
import com.bumptech.glide.request.transition.Transition

abstract class MusicColoredTarget(view: ImageView) : BitmapPaletteTarget(view) {

    protected val defaultFooterColor: Int
        get() = getView().context.colorControlNormal()

    abstract fun onColorReady(colors: MediaNotificationProcessor)

    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        onColorReady(
            MediaNotificationProcessor.errorColor(
                App.getContext()
            )
        )
    }

    override fun onResourceReady(
        resource: BitmapPaletteWrapper,
        transition: Transition<in BitmapPaletteWrapper>?,
    ) {
        super.onResourceReady(resource, transition)
        MediaNotificationProcessor(
            App.getContext()
        ).getPaletteAsync({
            onColorReady(it)
        }, resource.bitmap)
    }
}
