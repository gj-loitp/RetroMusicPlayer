package code.roy.retromusic.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import code.roy.retromusic.databinding.LayoutBannerImageBinding
import code.roy.retromusic.databinding.VLayoutUserImageBinding
import code.roy.retromusic.util.PreferenceUtil

class HomeImageLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    private var userImageBinding: VLayoutUserImageBinding? = null
    private var bannerImageBinding: LayoutBannerImageBinding? = null

    init {
        if (isInEditMode || PreferenceUtil.isHomeBanner) {
            bannerImageBinding = LayoutBannerImageBinding.inflate(LayoutInflater.from(context), this, true)
        } else {
            userImageBinding = VLayoutUserImageBinding.inflate(LayoutInflater.from(context), this, true)
        }
    }

    val userImage: ImageView
        get() = if (PreferenceUtil.isHomeBanner) {
            bannerImageBinding!!.userImage
        } else {
            userImageBinding!!.userImage
        }

    val bannerImage: ImageView?
        get() = if (PreferenceUtil.isHomeBanner) {
            bannerImageBinding!!.bannerImage
        } else {
            null
        }

    val titleWelcome : TextView
        get() = if (PreferenceUtil.isHomeBanner) {
            bannerImageBinding!!.titleWelcome
        } else {
            userImageBinding!!.titleWelcome
        }
}