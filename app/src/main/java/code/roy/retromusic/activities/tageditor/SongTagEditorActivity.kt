package code.roy.retromusic.activities.tageditor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import code.roy.appthemehelper.util.MaterialValueHelper
import code.roy.retromusic.R
import code.roy.retromusic.databinding.ActivitySongTagEditorBinding
import code.roy.retromusic.extensions.appHandleColor
import code.roy.retromusic.extensions.defaultFooterColor
import code.roy.retromusic.extensions.isColorLight
import code.roy.retromusic.extensions.setTint
import code.roy.retromusic.extensions.showToast
import code.roy.retromusic.glide.RetroGlideExtension.asBitmapPalette
import code.roy.retromusic.glide.palette.BitmapPaletteWrapper
import code.roy.retromusic.model.ArtworkInfo
import code.roy.retromusic.repository.SongRepository
import code.roy.retromusic.util.ImageUtil
import code.roy.retromusic.util.MusicUtil
import code.roy.retromusic.util.RetroColorUtil
import code.roy.retromusic.util.logD
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.shape.MaterialShapeDrawable
import org.jaudiotagger.tag.FieldKey
import org.koin.android.ext.android.inject
import java.util.*

class SongTagEditorActivity : AbsTagEditorActivity<ActivitySongTagEditorBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivitySongTagEditorBinding =
        ActivitySongTagEditorBinding::inflate

    private val songRepository by inject<SongRepository>()

    private var albumArtBitmap: Bitmap? = null
    private var deleteAlbumArt: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpViews()
        setSupportActionBar(binding.toolbar)
        binding.appBarLayout?.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpViews() {
        fillViewsWithFileTags()
        binding.songTextContainer.setTint(false)
        binding.composerContainer.setTint(false)
        binding.albumTextContainer.setTint(false)
        binding.artistContainer.setTint(false)
        binding.albumArtistContainer.setTint(false)
        binding.yearContainer.setTint(false)
        binding.genreContainer.setTint(false)
        binding.trackNumberContainer.setTint(false)
        binding.discNumberContainer.setTint(false)
        binding.lyricsContainer.setTint(false)

        binding.songText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.albumText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.albumArtistText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.artistText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.genreText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.yearText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.trackNumberText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.discNumberText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.lyricsText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.songComposerText.appHandleColor().doAfterTextChanged { dataChanged() }
    }

    private fun fillViewsWithFileTags() {
        binding.songText.setText(songTitle)
        binding.albumArtistText.setText(albumArtist)
        binding.albumText.setText(albumTitle)
        binding.artistText.setText(artistName)
        binding.genreText.setText(genreName)
        binding.yearText.setText(songYear)
        binding.trackNumberText.setText(trackNumber)
        binding.discNumberText.setText(discNumber)
        binding.lyricsText.setText(lyrics)
        binding.songComposerText.setText(composer)
        logD(songTitle + songYear)
    }

    override fun loadCurrentImage() {
        val bitmap = albumArt
        setImageBitmap(
            bitmap = bitmap,
            bgColor = RetroColorUtil.getColor(
                /* palette = */ RetroColorUtil.generatePalette(bitmap),
                /* fallback = */ defaultFooterColor()
            )
        )
        deleteAlbumArt = false
    }

    override fun searchImageOnWeb() {
        searchWebFor(binding.songText.text.toString(), binding.artistText.text.toString())
    }

    override fun deleteImage() {
        setImageBitmap(
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_audio_art),
            bgColor = defaultFooterColor()
        )
        deleteAlbumArt = true
        dataChanged()
    }

    override fun setColors(color: Int) {
        super.setColors(color)
        saveFab.backgroundTintList = ColorStateList.valueOf(color)
        ColorStateList.valueOf(
            /* color = */ MaterialValueHelper.getPrimaryTextColor(
                context = this,
                dark = color.isColorLight
            )
        ).also {
            saveFab.iconTint = it
            saveFab.setTextColor(it)
        }
    }

    override fun save() {
        val fieldKeyValueMap = EnumMap<FieldKey, String>(FieldKey::class.java)
        fieldKeyValueMap[FieldKey.TITLE] = binding.songText.text.toString()
        fieldKeyValueMap[FieldKey.ALBUM] = binding.albumText.text.toString()
        fieldKeyValueMap[FieldKey.ARTIST] = binding.artistText.text.toString()
        fieldKeyValueMap[FieldKey.GENRE] = binding.genreText.text.toString()
        fieldKeyValueMap[FieldKey.YEAR] = binding.yearText.text.toString()
        fieldKeyValueMap[FieldKey.TRACK] = binding.trackNumberText.text.toString()
        fieldKeyValueMap[FieldKey.DISC_NO] = binding.discNumberText.text.toString()
        fieldKeyValueMap[FieldKey.LYRICS] = binding.lyricsText.text.toString()
        fieldKeyValueMap[FieldKey.ALBUM_ARTIST] = binding.albumArtistText.text.toString()
        fieldKeyValueMap[FieldKey.COMPOSER] = binding.songComposerText.text.toString()
        writeValuesToFiles(
            fieldKeyValueMap = fieldKeyValueMap, artworkInfo = when {
                deleteAlbumArt -> ArtworkInfo(id, null)
                albumArtBitmap == null -> null
                else -> ArtworkInfo(id, albumArtBitmap!!)
            }
        )
    }

    override fun getSongPaths(): List<String> = listOf(songRepository.song(id).data)

    override fun getSongUris(): List<Uri> = listOf(MusicUtil.getSongFileUri(id))

    override fun loadImageFromFile(selectedFile: Uri?) {
        Glide.with(this@SongTagEditorActivity)
            .asBitmapPalette()
            .load(selectedFile)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(object : ImageViewTarget<BitmapPaletteWrapper>(binding.editorImage) {
                override fun onResourceReady(
                    resource: BitmapPaletteWrapper,
                    transition: Transition<in BitmapPaletteWrapper>?,
                ) {
                    RetroColorUtil.getColor(resource.palette, Color.TRANSPARENT)
                    albumArtBitmap = resource.bitmap?.let { ImageUtil.resizeBitmap(it, 2048) }
                    setImageBitmap(
                        bitmap = albumArtBitmap,
                        bgColor = RetroColorUtil.getColor(
                            /* palette = */ resource.palette,
                            /* fallback = */ defaultFooterColor()
                        )
                    )
                    deleteAlbumArt = false
                    dataChanged()
                    setResult(Activity.RESULT_OK)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    showToast(R.string.error_load_failed, Toast.LENGTH_LONG)
                }

                override fun setResource(resource: BitmapPaletteWrapper?) {}
            })
    }

    companion object {
        val TAG: String = SongTagEditorActivity::class.java.simpleName
    }

    override val editorImage: ImageView
        get() = binding.editorImage
}
