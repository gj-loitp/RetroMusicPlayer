package code.roy.appthemehelper.common.prefs.supportv7

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.widget.doAfterTextChanged
import androidx.preference.PreferenceViewHolder
import androidx.preference.SeekBarPreference
import code.roy.appthemehelper.R
import code.roy.appthemehelper.ThemeStore
import code.roy.appthemehelper.util.ATHUtil

class ATESeekBarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1,
) : SeekBarPreference(context, attrs, defStyleAttr, defStyleRes) {

    var unit: String = ""

    init {
        context.withStyledAttributes(attrs, R.styleable.ATESeekBarPreference, 0, 0) {
            getString(R.styleable.ATESeekBarPreference_ateKey_pref_unit)?.let {
                unit = it
            }
        }
        icon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            ATHUtil.resolveColor(
                context,
                android.R.attr.colorControlNormal
            ), BlendModeCompat.SRC_IN
        )
    }

    override fun onBindViewHolder(view: PreferenceViewHolder) {
        super.onBindViewHolder(view)
        val seekBar = view.findViewById(androidx.preference.R.id.seekbar) as SeekBar
        code.roy.appthemehelper.util.TintHelper.setTintAuto(
            /* view = */ seekBar, // Set MD3 accent if MD3 is enabled or in-app accent otherwise
            /* color = */ ThemeStore.accentColor(context), /* background = */ false
        )
        (view.findViewById(androidx.preference.R.id.seekbar_value) as TextView).apply {
            appendUnit(editableText)
            doAfterTextChanged {
                appendUnit(it)
            }
        }
    }

    private fun TextView.appendUnit(editable: Editable?) {
        if (!editable.toString().endsWith(unit)) {
            append(unit)
        }
    }
}
