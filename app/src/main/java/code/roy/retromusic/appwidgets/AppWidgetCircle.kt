package code.roy.retromusic.appwidgets

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
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
import code.roy.retromusic.service.MusicService.Companion.ACTION_TOGGLE_PAUSE
import code.roy.retromusic.service.MusicService.Companion.TOGGLE_FAVORITE
import code.roy.retromusic.util.MusicUtil
import code.roy.retromusic.util.PreferenceUtil
import code.roy.retromusic.util.RetroUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class AppWidgetCircle : BaseAppWidget() {
    private var target: Target<BitmapPaletteWrapper>? = null // for cancellation

    /**
     * Initialize given widgets to default state, where we launch Music on default click and hide
     * actions if service not running.
     */
    override fun defaultAppWidget(context: Context, appWidgetIds: IntArray) {
        val appWidgetView = RemoteViews(
            /* packageName = */ context.packageName,
            /* layoutId = */ R.layout.v_app_widget_circle
        )

        appWidgetView.setImageViewResource(R.id.image, R.drawable.default_audio_art)
        val secondaryColor = MaterialValueHelper.getSecondaryTextColor(
            context = context,
            dark = true
        )
        appWidgetView.setImageViewBitmap(
            /* viewId = */ R.id.button_toggle_play_pause,
            /* bitmap = */ context.getTintedDrawable(
                id = R.drawable.ic_play_arrow,
                color = secondaryColor
            ).toBitmap()
        )

        linkButtons(context = context, views = appWidgetView)
        pushUpdate(context = context, appWidgetIds = appWidgetIds, views = appWidgetView)
    }

    /**
     * Update all active widget instances by pushing changes
     */
    @SuppressLint("RemoteViewLayout")
    override fun performUpdate(service: MusicService, appWidgetIds: IntArray?) {
        val appWidgetView = RemoteViews(
            /* packageName = */ service.packageName,
            /* layoutId = */ R.layout.v_app_widget_circle
        )

        val isPlaying = service.isPlaying
        val song = service.currentSong

        // Set correct drawable for pause state
        val playPauseRes =
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
        appWidgetView.setImageViewBitmap(
            /* viewId = */ R.id.button_toggle_play_pause,
            /* bitmap = */ service.getTintedDrawable(
                id = playPauseRes,
                color = MaterialValueHelper.getSecondaryTextColor(service, true)
            ).toBitmap()
        )
        val isFavorite = runBlocking(Dispatchers.IO) {
            return@runBlocking MusicUtil.isFavorite(song)
        }
        val favoriteRes =
            if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        appWidgetView.setImageViewBitmap(
            R.id.button_toggle_favorite,
            service.getTintedDrawable(
                id = favoriteRes,
                color = MaterialValueHelper.getSecondaryTextColor(service, true)
            ).toBitmap()
        )

        // Link actions buttons to intents
        linkButtons(service, appWidgetView)

        if (imageSize == 0) {
            val p = RetroUtil.getScreenSize(service)
            imageSize = p.x.coerceAtMost(p.y)
        }

        // Load the album cover async and push the update on completion
        service.runOnUiThread {
            if (target != null) {
                Glide.with(service).clear(target)
            }
            target = Glide.with(service)
                .asBitmapPalette()
                .songCoverOptions(song)
                .load(RetroGlideExtension.getSongModel(song))
                .apply(RequestOptions.circleCropTransform())
                .into(object : CustomTarget<BitmapPaletteWrapper>(imageSize, imageSize) {
                    override fun onResourceReady(
                        resource: BitmapPaletteWrapper,
                        transition: Transition<in BitmapPaletteWrapper>?,
                    ) {
                        val palette = resource.palette
                        update(
                            bitmap = resource.bitmap, color = palette.getVibrantColor(
                                palette.getMutedColor(
                                    /* defaultColor = */ MaterialValueHelper.getSecondaryTextColor(
                                        context = service, dark = true
                                    )
                                )
                            )
                        )
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        update(
                            bitmap = null,
                            color = MaterialValueHelper.getSecondaryTextColor(
                                context = service,
                                dark = true
                            )
                        )
                    }

                    private fun update(bitmap: Bitmap?, color: Int) {
                        // Set correct drawable for pause state
                        appWidgetView.setImageViewBitmap(
                            /* viewId = */ R.id.button_toggle_play_pause,
                            /* bitmap = */ service.getTintedDrawable(
                                playPauseRes, color
                            ).toBitmap()
                        )

                        // Set favorite button drawables
                        appWidgetView.setImageViewBitmap(
                            /* viewId = */ R.id.button_toggle_favorite,
                            /* bitmap = */ service.getTintedDrawable(
                                id = favoriteRes, color = color
                            ).toBitmap()
                        )
                        if (bitmap != null) {
                            appWidgetView.setImageViewBitmap(R.id.image, bitmap)
                        }

                        pushUpdate(
                            context = service,
                            appWidgetIds = appWidgetIds,
                            views = appWidgetView
                        )
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
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

        val serviceName = ComponentName(context, MusicService::class.java)

        // Home
        action.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        var pendingIntent =
            PendingIntent.getActivity(
                /* context = */ context,
                /* requestCode = */ 0,
                /* intent = */ action, /* flags = */ if (VersionUtils.hasMarshmallow())
                    PendingIntent.FLAG_IMMUTABLE
                else 0
            )
        views.setOnClickPendingIntent(R.id.image, pendingIntent)
        // Favorite track
        pendingIntent = buildPendingIntent(context, TOGGLE_FAVORITE, serviceName)
        views.setOnClickPendingIntent(R.id.button_toggle_favorite, pendingIntent)

        // Play and pause
        pendingIntent = buildPendingIntent(context, ACTION_TOGGLE_PAUSE, serviceName)
        views.setOnClickPendingIntent(R.id.button_toggle_play_pause, pendingIntent)
    }

    companion object {

        const val NAME = "app_widget_circle"

        private var mInstance: AppWidgetCircle? = null
        private var imageSize = 0

        val instance: AppWidgetCircle
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = AppWidgetCircle()
                }
                return mInstance!!
            }
    }
}
