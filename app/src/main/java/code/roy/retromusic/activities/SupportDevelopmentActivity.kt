package code.roy.retromusic.activities

import android.os.Bundle
import android.view.MenuItem
import code.roy.appthemehelper.util.ToolbarContentTintHelper
import code.roy.retromusic.activities.base.AbsThemeActivity
import code.roy.retromusic.databinding.ADonationBinding
import code.roy.retromusic.extensions.openUrl
import code.roy.retromusic.extensions.setStatusBarColorAuto
import code.roy.retromusic.extensions.setTaskDescriptionColorAuto
import code.roy.retromusic.extensions.surfaceColor

class SupportDevelopmentActivity : AbsThemeActivity() {

    lateinit var binding: ADonationBinding
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ADonationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColorAuto()
        setTaskDescriptionColorAuto()

        setupToolbar()

        binding.paypal.setOnClickListener {
            openUrl(PAYPAL_URL)
        }
        binding.kofi.setOnClickListener {
            openUrl(KOFI_URL)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setBackgroundColor(surfaceColor())
        ToolbarContentTintHelper.colorBackButton(binding.toolbar)
        setSupportActionBar(binding.toolbar)
    }

    companion object {
        const val PAYPAL_URL = "https://paypal.me/quickersilver"
        const val KOFI_URL = "https://ko-fi.com/quickersilver"
    }
}
