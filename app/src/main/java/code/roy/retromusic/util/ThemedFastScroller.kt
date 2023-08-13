package code.roy.retromusic.util

import android.view.ViewGroup
import code.roy.retromusic.views.PopupBackground
import code.roy.appthemehelper.ThemeStore.Companion.accentColor
import code.roy.appthemehelper.util.ColorUtil.isColorLight
import code.roy.appthemehelper.util.MaterialValueHelper.getPrimaryTextColor
import me.zhanghai.android.fastscroll.FastScroller
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import me.zhanghai.android.fastscroll.PopupStyles
import me.zhanghai.android.fastscroll.R

object ThemedFastScroller {
    fun create(view: ViewGroup): FastScroller {
        val context = view.context
        val color = accentColor(context)
        val textColor = getPrimaryTextColor(context, isColorLight(color))
        val fastScrollerBuilder = FastScrollerBuilder(view)
        fastScrollerBuilder.useMd2Style()
        fastScrollerBuilder.setPopupStyle { popupText ->
            PopupStyles.MD2.accept(popupText)
            popupText.background =
                PopupBackground(context, color)
            popupText.setTextColor(textColor)
        }

        fastScrollerBuilder.setThumbDrawable(
            code.roy.appthemehelper.util.TintHelper.createTintedDrawable(
                /* context = */ context,
                /* res = */ R.drawable.afs_md2_thumb,
                /* color = */ color
            )
        )
        return fastScrollerBuilder.build()
    }
}
