package code.roy.retromusic.appshortcuts

import android.content.Context
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.TypedValue
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import code.roy.appthemehelper.ThemeStore
import code.roy.retromusic.R
import code.roy.retromusic.extensions.getTintedDrawable
import code.roy.retromusic.util.PreferenceUtil

@RequiresApi(Build.VERSION_CODES.N_MR1)
object AppShortcutIconGenerator {
    fun generateThemedIcon(context: Context, iconId: Int): Icon {
        return if (PreferenceUtil.isColoredAppShortcuts) {
            generateUserThemedIcon(context = context, iconId = iconId)
        } else {
            generateDefaultThemedIcon(context = context, iconId = iconId)
        }
    }

    private fun generateDefaultThemedIcon(context: Context, iconId: Int): Icon {
        // Return an Icon of iconId with default colors
        return generateThemedIcon(
            context = context,
            iconId = iconId,
            foregroundColor = context.getColor(R.color.app_shortcut_default_foreground),
            backgroundColor = context.getColor(R.color.app_shortcut_default_background)
        )
    }

    private fun generateUserThemedIcon(context: Context, iconId: Int): Icon {
        // Get background color from context's theme
        val typedColorBackground = TypedValue()
        context.theme.resolveAttribute(
            /* resid = */ android.R.attr.colorBackground,
            /* outValue = */ typedColorBackground,
            /* resolveRefs = */ true
        )

        // Return an Icon of iconId with those colors
        return generateThemedIcon(
            context = context,
            iconId = iconId,
            foregroundColor = ThemeStore.accentColor(context),
            backgroundColor = typedColorBackground.data
        )
    }

    private fun generateThemedIcon(
        context: Context,
        iconId: Int,
        foregroundColor: Int,
        backgroundColor: Int,
    ): Icon {
        // Get and tint foreground and background drawables
        val vectorDrawable = context.getTintedDrawable(iconId, foregroundColor)
        val backgroundDrawable =
            context.getTintedDrawable(R.drawable.ic_app_shortcut_background, backgroundColor)

        // Squash the two drawables together
        val layerDrawable = LayerDrawable(arrayOf(backgroundDrawable, vectorDrawable))

        // Return as an Icon
        return Icon.createWithBitmap(layerDrawable.toBitmap())
    }
}
