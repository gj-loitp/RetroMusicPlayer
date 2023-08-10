package code.roy.appthemehelper

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.WindowInsetsController
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import androidx.annotation.ColorInt

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
object ATH {

    fun didThemeValuesChange(context: Context, since: Long): Boolean {
        return code.roy.appthemehelper.ThemeStore.Companion.isConfigured(context) && code.roy.appthemehelper.ThemeStore.Companion.prefs(
            context
        ).getLong(
            code.roy.appthemehelper.ThemeStorePrefKeys.Companion.VALUES_CHANGED,
            -1
        ) > since
    }

    fun setTint(view: View, @ColorInt color: Int) {
        code.roy.appthemehelper.util.TintHelper.setTintAuto(view, color, false)
    }

    fun setBackgroundTint(view: View, @ColorInt color: Int) {
        code.roy.appthemehelper.util.TintHelper.setTintAuto(view, color, true)
    }
}