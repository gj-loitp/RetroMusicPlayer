package code.roy.retromusic.extensions

import androidx.core.view.WindowInsetsCompat
import code.roy.retromusic.util.PreferenceUtil
import code.roy.retromusic.util.RetroUtil

fun WindowInsetsCompat?.getBottomInsets(): Int {
    return if (PreferenceUtil.isFullScreenMode) {
        return 0
    } else {
        this?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: RetroUtil.navigationBarHeight
    }
}
