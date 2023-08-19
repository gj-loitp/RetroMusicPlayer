package code.roy.retromusic.appwidgets

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import code.roy.appthemehelper.util.MaterialValueHelper
import code.roy.appthemehelper.util.VersionUtils
import code.roy.retromusic.R
import code.roy.retromusic.activities.MainActivity
import code.roy.retromusic.appwidgets.base.BaseAppWidget
import code.roy.retromusic.extensions.getTintedDrawable
import code.roy.retromusic.glide.RetroGlideExtension
import code.roy.retromusic.glide.RetroGlideExtension.asBitmapPalette
import code.roy.retromusic.glide.RetroGlideExtension.songCoverOptions
import code.roy.retromusic.glide.palette.BitmapPaletteWrapper
import code.roy.retromusic.service.MusicService
import code.roy.retromusic.service.MusicService.Companion.ACTION_REWIND
import code.roy.retromusic.service.MusicService.Companion.ACTION_SKIP
import code.roy.retromusic.service.MusicService.Companion.ACTION_TOGGLE_PAUSE
import code.roy.retromusic.util.PreferenceUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

class AppWidgetClassic : BaseAppWidget() {
    private var target: Target<BitmapPaletteWrapper>? = null // for cancellation

    /**
     * Initialize given widgets to default state, where we launch Music on default click and hide
     * actions if service not running.
     */
    @SuppressLint("RemoteViewLayout")
    override fun defaultAppWidget(context: Context, appWidgetIds: IntArray) {
        val appWidgetView = RemoteViews(
            /* packageName = */ context.packageName,
            /* layoutId = */ R.layout.v_app_widget_classic
        )

        appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE)
        appWidgetView.setImageViewResource(R.id.image, R.drawable.default_audio_art)
        appWidgetView.setImageViewBitmap(
            R.id.button_next,

            context.getTintedDrawable(
                id = R.drawable.ic_skip_next,
                color = MaterialValueHelper.getSecondaryTextColor(context, true)
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_prev,

            context.getTintedDrawable(
                id = R.drawable.ic_skip_previous,
                color = MaterialValueHelper.getSecondaryTextColor(context, true)
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_toggle_play_pause,

            context.getTintedDrawable(
                id = R.drawable.ic_play_arrow_white_32dp,
                color = MaterialValueHelper.getSecondaryTextColor(context, true)
            ).toBitmap()
        )

        linkButtons(context, appWidgetView)
        pushUpdate(context, appWidgetIds, appWidgetView)
    }

    /**
     * Update all active widget instances by pushing changes
     */
    @SuppressLint("RemoteViewLayout")
    override fun performUpdate(service: MusicService, appWidgetIds: IntArray?) {
        val appWidgetView = RemoteViews(
            /* packageName = */ service.packageName,
            /* layoutId = */ R.layout.v_app_widget_classic
        )

        val isPlaying = service.isPlaying
        val song = service.currentSong

        // Set the titles and artwork
        if (song.title.isEmpty() && song.artistName.isEmpty()) {
            appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE)
        } else {
            appWidgetView.setViewVisibility(R.id.media_titles, View.VISIBLE)
            appWidgetView.setTextViewText(R.id.title, song.title)
            appWidgetView.setTextViewText(R.id.text, getSongArtistAndAlbum(song))
        }

        // Link actions buttons to intents
        linkButtons(service, appWidgetView)

        if (imageSize == 0) {
            imageSize =
                service.resources.getDimensionPixelSize(R.dimen.app_widget_classic_image_size)
        }
        if (cardRadius == 0f) {
            cardRadius = service.resources.getDimension(R.dimen.app_widget_card_radius)
        }

        // Load the album cover async and push the update on completion
        val appContext = service.applicationContext
        service.runOnUiThread {
            if (target != null) {
                Glide.with(service).clear(target)
            }
            target = Glide.with(service)
                .asBitmapPalette()
                .songCoverOptions(song)
                .load(RetroGlideExtension.getSongModel(song))
                //.checkIgnoreMediaStore()
                .centerCrop()
                .into(object : CustomTarget<BitmapPaletteWrapper>(imageSize, imageSize) {
                    override fun onResourceReady(
                        resource: BitmapPaletteWrapper,
                        transition: Transition<in BitmapPaletteWrapper>?,
                    ) {
                        val palette = resource.palette
                        update(
                            bitmap = resource.bitmap,
                            color = palette.getVibrantColor(
                                /* defaultColor = */ palette.getMutedColor(
                                    MaterialValueHelper.getSecondaryTextColor(
                                        context = service,
                                        dark = true
                                    )
                                )
                            )
                        )
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        update(bitmap = null, color = Color.WHITE)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}

                    private fun update(bitmap: Bitmap?, color: Int) {
                        // Set correct drawable for pause state
                        val playPauseRes =
                            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
                        appWidgetView.setImageViewBitmap(
                            R.id.button_toggle_play_pause,
                            service.getTintedDrawable(
                                id = playPauseRes,
                                color = color
                            ).toBitmap()
                        )

                        // Set prev/next button drawables
                        appWidgetView.setImageViewBitmap(
                            R.id.button_next,
                            service.getTintedDrawable(
                                id = R.drawable.ic_skip_next,
                                color = color
                            ).toBitmap()
                        )
                        appWidgetView.setImageViewBitmap(
                            R.id.button_prev,
                            service.getTintedDrawable(
                                id = R.drawable.ic_skip_previous,
                                color = color
                            ).toBitmap()
                        )

                        val image = getAlbumArtDrawable(service, bitmap)
                        val roundedBitmap =
                            createRoundedBitmap(
                                drawable = image,
                                width = imageSize,
                                height = imageSize,
                                tl = cardRadius,
                                tr = 0F,
                                bl = cardRadius,
                                br = 0F
                            )
                        appWidgetView.setImageViewBitmap(R.id.image, roundedBitmap)

                        pushUpdate(
                            context = appContext,
                            appWidgetIds = appWidgetIds,
                            views = appWidgetView
                        )
                    }
                })
        }
    }

    /**
     * Link up various button actions using [PendingIntent].
     */
    private fun linkButtons(context: Context, views: RemoteViews) {
        val action = Intent(context, MainActivity::class.java)
            .putExtra(
                MainActivity.EXPAND_PANEL,
                PreferenceUtil.isExpandPanel
            )

        val serviceName = ComponentName(/* pkg = */ context, /* cls = */ MusicService::class.java)

        // Home
        action.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        var pendingIntent = PendingIntent.getActivity(
            /* context = */ context,
            /* requestCode = */ 0,
            /* intent = */ action, /* flags = */ if (VersionUtils.hasMarshmallow())
                PendingIntent.FLAG_IMMUTABLE
            else 0
        )
        views.setOnClickPendingIntent(R.id.image, pendingIntent)
        views.setOnClickPendingIntent(R.id.media_titles, pendingIntent)

        // Previous track
        pendingIntent = buildPendingIntent(
            context = context,
            action = ACTION_REWIND,
            serviceName = serviceName
        )
        views.setOnClickPendingIntent(R.id.button_prev, pendingIntent)

        // Play and pause
        pendingIntent = buildPendingIntent(
            context = context,
            action = ACTION_TOGGLE_PAUSE,
            serviceName = serviceName
        )
        views.setOnClickPendingIntent(R.id.button_toggle_play_pause, pendingIntent)

        // Next track
        pendingIntent = buildPendingIntent(
            context = context,
            action = ACTION_SKIP,
            serviceName = serviceName
        )
        views.setOnClickPendingIntent(R.id.button_next, pendingIntent)
    }

    companion object {

        const val NAME = "app_widget_classic"

        private var mInstance: AppWidgetClassic? = null
        private var imageSize = 0
        private var cardRadius = 0f

        val instance: AppWidgetClassic
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = AppWidgetClassic()
                }
                return mInstance!!
            }
    }
}
