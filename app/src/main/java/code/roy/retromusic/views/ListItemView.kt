package code.roy.retromusic.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import code.roy.retromusic.R
import code.roy.retromusic.databinding.VListItemViewNoCardBinding
import code.roy.retromusic.extensions.hide
import code.roy.retromusic.extensions.show

class ListItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var binding =
        VListItemViewNoCardBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.withStyledAttributes(attrs, R.styleable.ListItemView) {
            if (hasValue(R.styleable.ListItemView_listItemIcon)) {
                binding.icon.setImageDrawable(getDrawable(R.styleable.ListItemView_listItemIcon))
            } else {
                binding.icon.hide()
            }

            binding.title.text = getText(R.styleable.ListItemView_listItemTitle)
            if (hasValue(R.styleable.ListItemView_listItemSummary)) {
                binding.summary.text = getText(R.styleable.ListItemView_listItemSummary)
            } else {
                binding.summary.hide()
            }
        }
    }

    fun setSummary(appVersion: String) {
        binding.summary.show()
        binding.summary.text = appVersion
    }
}
