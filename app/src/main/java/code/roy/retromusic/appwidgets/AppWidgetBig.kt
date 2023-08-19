package code.roy.retromusic.appwidgets

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import code.roy.retromusic.service.MusicService
import code.roy.retromusic.service.MusicService.Companion.ACTION_REWIND
import code.roy.retromusic.service.MusicService.Companion.ACTION_SKIP
import code.roy.retromusic.service.MusicService.Companion.ACTION_TOGGLE_PAUSE
import code.roy.retromusic.util.PreferenceUtil
import code.roy.retromusic.util.RetroUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

class AppWidgetBig : BaseAppWidget() {
    private var target: Target<Bitmap>? = null // for cancellation

    /**
     * Initialize given widgets to default state, where we launch Music on default click and hide
     * actions if service not running.
     */
    @SuppressLint("RemoteViewLayout")
    override fun defaultAppWidget(context: Context, appWidgetIds: IntArray) {
        val appWidgetView = RemoteViews(
            /* packageName = */ context.packageName,
            /* layoutId = */ R.layout.v_app_widget_big
        )

        appWidgetView.setViewVisibility(
            /* viewId = */ R.id.media_titles,
            /* visibility = */ View.INVISIBLE
        )
        appWidgetView.setImageViewResource(R.id.image, R.drawable.default_audio_art)
        appWidgetView.setImageViewBitmap(
            R.id.button_next, context.getTintedDrawable(
                R.drawable.ic_skip_next,
                MaterialValueHelper.getPrimaryTextColor(
                    context = context,
                    dark = false
                )
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_prev,
            context.getTintedDrawable(
                R.drawable.ic_skip_previous,
                MaterialValueHelper.getPrimaryTextColor(context = context, dark = false)
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_toggle_play_pause,
            context.getTintedDrawable(
                R.drawable.ic_play_arrow_white_32dp,
                MaterialValueHelper.getPrimaryTextColor(context = context, dark = false)
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
            /* layoutId = */ R.layout.v_app_widget_big
        )

        val isPlaying = service.isPlaying
        val song = service.currentSong

        // Set the titles and artwork
        if (song.title.isEmpty() && song.artistName.isEmpty()) {
            appWidgetView.setViewVisibility(
                /* viewId = */ R.id.media_titles,
                /* visibility = */ View.INVISIBLE
            )
        } else {
            appWidgetView.setViewVisibility(
                /* viewId = */ R.id.media_titles,
                /* visibility = */ View.VISIBLE
            )
            appWidgetView.setTextViewText(R.id.title, song.title)
            appWidgetView.setTextViewText(
                /* viewId = */ R.id.text,
                /* text = */ getSongArtistAndAlbum(song)
            )
        }

        val primaryColor = MaterialValueHelper.getPrimaryTextColor(service, false)
        // Set correct drawable for pause state
        val playPauseRes =
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow_white_32dp
        appWidgetView.setImageViewBitmap(
            R.id.button_toggle_play_pause,
            service.getTintedDrawable(
                id = playPauseRes,
                color = primaryColor
            ).toBitmap()
        )

        // Set prev/next button drawables
        appWidgetView.setImageViewBitmap(
            R.id.button_next,
            service.getTintedDrawable(
                id = R.drawable.ic_skip_next,
                color = primaryColor
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_prev,
            service.getTintedDrawable(
                id = R.drawable.ic_skip_previous,
                color = primaryColor
            ).toBitmap()
        )

        // Link actions buttons to intents
        linkButtons(service, appWidgetView)

        // Load the album cover async and push the update on completion
        val p = RetroUtil.getScreenSize(service)
        val widgetImageSize = p.x.coerceAtMost(p.y)
        val appContext = service.applicationContext
        service.runOnUiThread {
            if (target != null) {
                Glide.with(service).clear(target)
            }
            target = Glide.with(appContext)
                .asBitmap()
                //.checkIgnoreMediaStore()
                .load(RetroGlideExtension.getSongModel(song))
                .into(object : CustomTarget<Bitmap>(widgetImageSize, widgetImageSize) {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?,
                    ) {
                        update(resource)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        update(bitmap = null)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}

                    private fun update(bitmap: Bitmap?) {
                        if (bitmap == null) {
                            appWidgetView.setImageViewResource(
                                /* viewId = */ R.id.image,
                                /* srcId = */ R.drawable.default_audio_art
                            )
                        } else {
                            appWidgetView.setImageViewBitmap(R.id.image, bitmap)
                        }
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

        val serviceName = ComponentName(context, MusicService::class.java)

        // Home
        action.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        var pendingIntent =
            PendingIntent.getActivity(
                context, 0, action, if (VersionUtils.hasMarshmallow())
                    PendingIntent.FLAG_IMMUTABLE
                else 0
            )
        views.setOnClickPendingIntent(R.id.clickable_area, pendingIntent)

        // Previous track
        pendingIntent = buildPendingIntent(context, ACTION_REWIND, serviceName)
        views.setOnClickPendingIntent(R.id.button_prev, pendingIntent)

        // Play and pause
        pendingIntent = buildPendingIntent(context, ACTION_TOGGLE_PAUSE, serviceName)
        views.setOnClickPendingIntent(R.id.button_toggle_play_pause, pendingIntent)

        // Next track
        pendingIntent = buildPendingIntent(context, ACTION_SKIP, serviceName)
        views.setOnClickPendingIntent(R.id.button_next, pendingIntent)
    }

    companion object {

        const val NAME: String = "app_widget_big"
        private var mInstance: AppWidgetBig? = null

        val instance: AppWidgetBig
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = AppWidgetBig()
                }
                return mInstance!!
            }
    }
}
