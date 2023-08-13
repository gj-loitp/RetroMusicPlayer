package code.roy.retromusic.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import code.roy.appthemehelper.util.ATHUtil
import code.roy.retromusic.extensions.accentColor
import code.roy.retromusic.extensions.addAlpha
import code.roy.retromusic.extensions.setItemColors
import code.roy.retromusic.util.PreferenceUtil
import com.google.android.material.navigationrail.NavigationRailView

class TintedNavigationRailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : NavigationRailView(context, attrs, defStyleAttr) {

    init {
        if (!isInEditMode) {
            labelVisibilityMode = PreferenceUtil.tabTitleMode

            if (!PreferenceUtil.materialYou) {
                val iconColor = ATHUtil.resolveColor(context, android.R.attr.colorControlNormal)
                val accentColor = context.accentColor()
                setItemColors(iconColor, accentColor)
                itemRippleColor = ColorStateList.valueOf(accentColor.addAlpha(0.08F))
                itemActiveIndicatorColor = ColorStateList.valueOf(accentColor.addAlpha(0.12F))
            }
        }
    }
}
