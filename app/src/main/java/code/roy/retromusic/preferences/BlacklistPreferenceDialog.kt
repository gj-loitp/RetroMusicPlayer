package code.roy.retromusic.preferences

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.core.text.parseAsHtml
import androidx.fragment.app.DialogFragment
import code.roy.retromusic.providers.BlacklistStore
import code.roy.appthemehelper.common.prefs.supportv7.ATEDialogPreference
import code.roy.retromusic.R
import code.roy.retromusic.dialogs.BlacklistFolderChooserDialog
import code.roy.retromusic.extensions.accentTextColor
import code.roy.retromusic.extensions.colorButtons
import code.roy.retromusic.extensions.colorControlNormal
import code.roy.retromusic.extensions.materialDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class BlacklistPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1,
) : ATEDialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        icon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                /* color = */ context.colorControlNormal(),
                /* blendModeCompat = */ SRC_IN
            )
    }
}

class BlacklistPreferenceDialog : DialogFragment(), BlacklistFolderChooserDialog.FolderCallback {
    companion object {
        fun newInstance(): BlacklistPreferenceDialog {
            return BlacklistPreferenceDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val chooserDialog =
            childFragmentManager.findFragmentByTag("FOLDER_CHOOSER") as BlacklistFolderChooserDialog?
        chooserDialog?.setCallback(this)
        val context = requireActivity()

        refreshBlacklistData(context)
        return materialDialog(R.string.blacklist)
            .setPositiveButton(R.string.done) { _, _ ->
                dismiss()
            }
            .setNeutralButton(R.string.clear_action) { _, _ ->
                materialDialog(R.string.clear_blacklist)
                    .setMessage(R.string.do_you_want_to_clear_the_blacklist)
                    .setPositiveButton(R.string.clear_action) { _, _ ->
                        BlacklistStore.getInstance(context).clear()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
                    .colorButtons()
                    .show()
            }
            .setNegativeButton(R.string.add_action) { _, _ ->
                val dialog = BlacklistFolderChooserDialog.create()
                dialog.setCallback(this@BlacklistPreferenceDialog)
                dialog.show(requireActivity().supportFragmentManager, "FOLDER_CHOOSER")
            }
            .setItems(paths.toTypedArray()) { _, which ->
                materialDialog(R.string.remove_from_blacklist)
                    .setMessage(
                        String.format(
                            getString(R.string.do_you_want_to_remove_from_the_blacklist),
                            paths[which]
                        ).parseAsHtml()
                    )
                    .setPositiveButton(R.string.remove_action) { _, _ ->
                        BlacklistStore.getInstance(context).removePath(File(paths[which]))
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
                    .colorButtons()
                    .show()
            }
            .create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).accentTextColor()
                    getButton(AlertDialog.BUTTON_NEGATIVE).accentTextColor()
                    getButton(AlertDialog.BUTTON_NEUTRAL).accentTextColor()
                }
            }
    }

    private lateinit var paths: ArrayList<String>

    private fun refreshBlacklistData(context: Context?) {
        if (context == null) return
        this.paths = BlacklistStore.getInstance(context).paths
        val dialog = dialog as MaterialAlertDialogBuilder?
        dialog?.setItems(paths.toTypedArray(), null)
    }

    override fun onFolderSelection(context: Context, folder: File) {
        BlacklistStore.getInstance(context).addPath(folder)
    }
}
