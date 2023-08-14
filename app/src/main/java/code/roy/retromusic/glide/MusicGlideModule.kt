package code.roy.retromusic.glide

import android.content.Context
import android.graphics.Bitmap
import code.roy.retromusic.glide.artistimage.ArtistImage
import code.roy.retromusic.glide.artistimage.Factory
import code.roy.retromusic.glide.audiocover.AudioFileCover
import code.roy.retromusic.glide.audiocover.AudioFileCoverLoader
import code.roy.retromusic.glide.palette.BitmapPaletteTranscoder
import code.roy.retromusic.glide.palette.BitmapPaletteWrapper
import code.roy.retromusic.glide.playlistPreview.PlaylistPreview
import code.roy.retromusic.glide.playlistPreview.PlaylistPreviewLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import java.io.InputStream

@GlideModule
class MusicGlideModule : AppGlideModule() {
    override fun registerComponents(
        context: Context,
        glide: Glide,
        registry: Registry,
    ) {
        registry.prepend(
            /* modelClass = */ PlaylistPreview::class.java,
            /* dataClass = */ Bitmap::class.java,
            /* factory = */ PlaylistPreviewLoader.Factory(context)
        )
        registry.prepend(
            /* modelClass = */ AudioFileCover::class.java,
            /* dataClass = */ InputStream::class.java,
            /* factory = */ AudioFileCoverLoader.Factory()
        )
        registry.prepend(ArtistImage::class.java, InputStream::class.java, Factory(context))
        registry.register(
            /* resourceClass = */ Bitmap::class.java,
            /* transcodeClass = */ BitmapPaletteWrapper::class.java,
            /* transcoder = */ BitmapPaletteTranscoder()
        )
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}
