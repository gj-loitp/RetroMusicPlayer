package code.roy.retromusic.glide

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import code.roy.appthemehelper.util.TintHelper
import code.roy.retromusic.App.Companion.getContext
import code.roy.retromusic.Constants.USER_BANNER
import code.roy.retromusic.Constants.USER_PROFILE
import code.roy.retromusic.R
import code.roy.retromusic.extensions.accentColor
import code.roy.retromusic.glide.artistimage.ArtistImage
import code.roy.retromusic.glide.audiocover.AudioFileCover
import code.roy.retromusic.glide.palette.BitmapPaletteWrapper
import code.roy.retromusic.model.Artist
import code.roy.retromusic.model.Song
import code.roy.retromusic.util.ArtistSignatureUtil
import code.roy.retromusic.util.CustomArtistImageUtil.Companion.getFile
import code.roy.retromusic.util.CustomArtistImageUtil.Companion.getInstance
import code.roy.retromusic.util.MusicUtil.getMediaStoreAlbumCoverUri
import code.roy.retromusic.util.PreferenceUtil
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.MediaStoreSignature
import java.io.File

object RetroGlideExtension {

    private const val DEFAULT_ARTIST_IMAGE = R.drawable.default_artist_art
    private const val DEFAULT_SONG_IMAGE: Int = R.drawable.default_audio_art
    private const val DEFAULT_ALBUM_IMAGE = R.drawable.default_album_art
    private const val DEFAULT_ERROR_IMAGE_BANNER = R.drawable.material_design_default

    private val DEFAULT_DISK_CACHE_STRATEGY_ARTIST = DiskCacheStrategy.RESOURCE
    private val DEFAULT_DISK_CACHE_STRATEGY = DiskCacheStrategy.NONE

    private const val DEFAULT_ANIMATION = android.R.anim.fade_in

    fun RequestManager.asBitmapPalette(): RequestBuilder<BitmapPaletteWrapper> {
        return this.`as`(BitmapPaletteWrapper::class.java)
    }

    private fun getSongModel(
        song: Song,
        ignoreMediaStore: Boolean,
    ): Any {
        return if (ignoreMediaStore) {
            AudioFileCover(song.data)
        } else {
            getMediaStoreAlbumCoverUri(song.albumId)
        }
    }

    fun getSongModel(song: Song): Any {
        return getSongModel(
            song = song,
            ignoreMediaStore = PreferenceUtil.isIgnoreMediaStoreArtwork
        )
    }

    fun getArtistModel(artist: Artist): Any {
        return getArtistModel(
            artist = artist,
            hasCustomImage = getInstance(getContext()).hasCustomArtistImage(artist),
            forceDownload = false
        )
    }

    fun getArtistModel(artist: Artist, forceDownload: Boolean): Any {
        return getArtistModel(
            artist = artist,
            hasCustomImage = getInstance(getContext()).hasCustomArtistImage(artist),
            forceDownload = forceDownload
        )
    }

    private fun getArtistModel(
        artist: Artist,
        hasCustomImage: Boolean,
        forceDownload: Boolean,
    ): Any {
        return if (!hasCustomImage) {
            ArtistImage(artist)
        } else {
            getFile(artist)
        }
    }

    fun <T> RequestBuilder<T>.artistImageOptions(
        artist: Artist,
    ): RequestBuilder<T> {
        return diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY_ARTIST)
            .priority(Priority.LOW)
            .error(getDrawable(DEFAULT_ARTIST_IMAGE))
            .placeholder(getDrawable(DEFAULT_ARTIST_IMAGE))
            .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
            .signature(createSignature(artist))
    }

    fun <T> RequestBuilder<T>.songCoverOptions(
        song: Song,
    ): RequestBuilder<T> {
        return diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
            .error(getDrawable(DEFAULT_SONG_IMAGE))
            .placeholder(getDrawable(DEFAULT_SONG_IMAGE))
            .signature(createSignature(song))
    }

    fun <T> RequestBuilder<T>.simpleSongCoverOptions(
        song: Song,
    ): RequestBuilder<T> {
        return diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
            .signature(createSignature(song))
    }

    fun <T> RequestBuilder<T>.albumCoverOptions(
        song: Song,
    ): RequestBuilder<T> {
        return diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
            .error(ContextCompat.getDrawable(getContext(), DEFAULT_ALBUM_IMAGE))
            .placeholder(ContextCompat.getDrawable(getContext(), DEFAULT_ALBUM_IMAGE))
            .signature(createSignature(song))
    }

    fun <T> RequestBuilder<T>.userProfileOptions(
        file: File,
        context: Context,
    ): RequestBuilder<T> {
        return diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
            .error(getErrorUserProfile(context))
            .signature(createSignature(file))
    }

    fun <T> RequestBuilder<T>.profileBannerOptions(
        file: File,
    ): RequestBuilder<T> {
        return diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
            .placeholder(DEFAULT_ERROR_IMAGE_BANNER)
            .error(DEFAULT_ERROR_IMAGE_BANNER)
            .signature(createSignature(file))
    }

    fun <T> RequestBuilder<T>.playlistOptions(): RequestBuilder<T> {
        return diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(getDrawable(DEFAULT_ALBUM_IMAGE))
            .error(getDrawable(DEFAULT_ALBUM_IMAGE))
    }

    private fun createSignature(song: Song): Key {
        return MediaStoreSignature(
            /* mimeType = */ "",
            /* dateModified = */ song.dateModified,
            /* orientation = */ 0
        )
    }

    private fun createSignature(file: File): Key {
        return MediaStoreSignature(
            /* mimeType = */ "",
            /* dateModified = */ file.lastModified(),
            /* orientation = */ 0
        )
    }

    private fun createSignature(artist: Artist): Key {
        return ArtistSignatureUtil.getInstance(getContext())
            .getArtistSignature(artist.name)
    }

    fun getUserModel(): File {
        val dir = getContext().filesDir
        return File(dir, USER_PROFILE)
    }

    fun getBannerModel(): File {
        val dir = getContext().filesDir
        return File(dir, USER_BANNER)
    }

    private fun getErrorUserProfile(context: Context): Drawable {
        return TintHelper.createTintedDrawable(
            /* context = */ context,
            /* res = */ R.drawable.ic_account,
            /* color = */ context.accentColor()
        )
    }

    fun <TranscodeType> getDefaultTransition(): GenericTransitionOptions<TranscodeType> {
        return GenericTransitionOptions<TranscodeType>().transition(DEFAULT_ANIMATION)
    }

    fun getDrawable(@DrawableRes id: Int): Drawable? {
        return ContextCompat.getDrawable(getContext(), id)
    }
}

// https://github.com/bumptech/glide/issues/527#issuecomment-148840717
fun RequestBuilder<Drawable>.crossfadeListener(): RequestBuilder<Drawable> {
    return listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean,
        ): Boolean {
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean,
        ): Boolean {
            return if (isFirstResource) {
                false // thumbnail was not shown, do as usual
            } else DrawableCrossFadeFactory.Builder()
                .setCrossFadeEnabled(true).build()
                .build(dataSource, isFirstResource)
                .transition(resource, target as Transition.ViewAdapter)
        }
    })
}
