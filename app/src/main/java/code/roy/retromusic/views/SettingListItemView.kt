package code.roy.retromusic.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import code.roy.retromusic.R
import code.roy.retromusic.databinding.VListSettingItemViewBinding

class SettingListItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    init {
        val binding: VListSettingItemViewBinding =
            VListSettingItemViewBinding.inflate(LayoutInflater.from(context), this, true)
        context.withStyledAttributes(attrs, R.styleable.SettingListItemView) {
            if (hasValue(R.styleable.SettingListItemView_settingListItemIcon)) {
                binding.icon.setImageDrawable(getDrawable(R.styleable.SettingListItemView_settingListItemIcon))
            }
            binding.icon.setIconBackgroundColor(
                getColor(R.styleable.SettingListItemView_settingListItemIconColor, Color.WHITE)
            )
            binding.title.text = getText(R.styleable.SettingListItemView_settingListItemTitle)
            binding.text.text = getText(R.styleable.SettingListItemView_settingListItemText)
        }
    }
}
