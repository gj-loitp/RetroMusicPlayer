package code.roy.retromusic.appwidgets

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import code.roy.retromusic.service.MusicService

class BootReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val widgetManager = AppWidgetManager.getInstance(context)

        // Start music service if there are any existing widgets
        if (widgetManager.getAppWidgetIds(
                ComponentName(
                    /* pkg = */ context,
                    /* cls = */ AppWidgetBig::class.java
                )
            ).isNotEmpty() || widgetManager.getAppWidgetIds(
                ComponentName(
                    /* pkg = */ context,
                    /* cls = */ AppWidgetClassic::class.java
                )
            ).isNotEmpty() || widgetManager.getAppWidgetIds(
                ComponentName(
                    /* pkg = */ context,
                    /* cls = */ AppWidgetSmall::class.java
                )
            ).isNotEmpty() || widgetManager.getAppWidgetIds(
                ComponentName(
                    /* pkg = */ context,
                    /* cls = */ AppWidgetCard::class.java
                )
            ).isNotEmpty()
        ) {
            val serviceIntent = Intent(/* packageContext = */ context, /* cls = */ MusicService::class.java)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { // not allowed on Oreo
                context.startService(serviceIntent)
            }
        }
    }
}
