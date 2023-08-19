package code.roy.retromusic.activities.bugreport

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import code.roy.appthemehelper.util.TintHelper
import code.roy.appthemehelper.util.ToolbarContentTintHelper
import code.roy.retromusic.R
import code.roy.retromusic.activities.base.AbsThemeActivity
import code.roy.retromusic.activities.bugreport.model.DeviceInfo
import code.roy.retromusic.databinding.ABugReportBinding
import code.roy.retromusic.extensions.accentColor
import code.roy.retromusic.extensions.setTaskDescriptionColorAuto
import code.roy.retromusic.extensions.showToast

open class BugReportActivity : AbsThemeActivity() {

    private lateinit var binding: ABugReportBinding
    private var deviceInfo: DeviceInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ABugReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTaskDescriptionColorAuto()

        initViews()

        if (title.isNullOrEmpty()) setTitle(R.string.report_an_issue)

        deviceInfo = DeviceInfo(this)
        binding.cardDeviceInfo.airTextDeviceInfo.text = deviceInfo.toString()
    }

    private fun initViews() {
        val accentColor = accentColor()
        setSupportActionBar(binding.toolbar)
        ToolbarContentTintHelper.colorBackButton(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.cardDeviceInfo.airTextDeviceInfo.setOnClickListener {
            copyDeviceInfoToClipBoard()
        }

        TintHelper.setTintAuto(
            /* view = */ binding.sendFab,
            /* color = */ accentColor,
            /* background = */ true
        )
        binding.sendFab.setOnClickListener { reportIssue() }
    }

    private fun reportIssue() {
        copyDeviceInfoToClipBoard()
        val i = Intent(Intent.ACTION_VIEW)
        i.data = ISSUE_TRACKER_LINK.toUri()
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun copyDeviceInfoToClipBoard() {
        val clipboard = getSystemService<ClipboardManager>()
        val clip = ClipData.newPlainText(getString(R.string.device_info), deviceInfo?.toMarkdown())
        clipboard?.setPrimaryClip(clip)
        showToast(R.string.copied_device_info_to_clipboard)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val ISSUE_TRACKER_LINK =
            "https://github.com/RetroMusicPlayer/RetroMusicPlayer/issues"
    }
}
