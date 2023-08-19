package code.roy.retromusic.activities

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.text.parseAsHtml
import androidx.core.view.isVisible
import code.roy.appthemehelper.util.VersionUtils
import code.roy.retromusic.R
import code.roy.retromusic.activities.base.AbsMusicServiceActivity
import code.roy.retromusic.databinding.APermissionBinding
import code.roy.retromusic.extensions.accentBackgroundColor
import code.roy.retromusic.extensions.accentColor
import code.roy.retromusic.extensions.setStatusBarColorAuto
import code.roy.retromusic.extensions.setTaskDescriptionColorAuto
import code.roy.retromusic.extensions.show

class PermissionActivity : AbsMusicServiceActivity() {
    private lateinit var binding: APermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = APermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColorAuto()
        setTaskDescriptionColorAuto()
        setupTitle()

        binding.storagePermission.setButtonClick {
            requestPermissions()
        }
        if (VersionUtils.hasMarshmallow()) {
            binding.audioPermission.show()
            binding.audioPermission.setButtonClick {
                if (!hasAudioPermission()) {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    intent.data = ("package:" + applicationContext.packageName).toUri()
                    startActivity(intent)
                }
            }
        }

        if (VersionUtils.hasS()) {
            binding.bluetoothPermission.show()
            binding.bluetoothPermission.setButtonClick {
                ActivityCompat.requestPermissions(
                    /* activity = */ this,
                    /* permissions = */ arrayOf(BLUETOOTH_CONNECT),
                    /* requestCode = */ BLUETOOTH_PERMISSION_REQUEST
                )
            }
        } else {
            binding.audioPermission.setNumber("2")
        }

        binding.finish.accentBackgroundColor()
        binding.finish.setOnClickListener {
            if (hasPermissions()) {
                startActivity(
                    Intent(this, MainActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                    )
                )
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
                remove()
            }
        })
    }

    private fun setupTitle() {
        val color = accentColor()
        val hexColor = String.format("#%06X", 0xFFFFFF and color)
        val appName =
            getString(
                R.string.message_welcome,
                "<b>Retro <span  style='color:$hexColor';>Music</span></b>"
            )
                .parseAsHtml()
        binding.appNameText.text = appName
    }

    override fun onResume() {
        super.onResume()
        binding.finish.isEnabled = hasStoragePermission()
        if (hasStoragePermission()) {
            binding.storagePermission.checkImage.isVisible = true
            binding.storagePermission.checkImage.imageTintList =
                ColorStateList.valueOf(accentColor())
        }
        if (VersionUtils.hasMarshmallow()) {
            if (hasAudioPermission()) {
                binding.audioPermission.checkImage.isVisible = true
                binding.audioPermission.checkImage.imageTintList =
                    ColorStateList.valueOf(accentColor())
            }
        }
        if (VersionUtils.hasS()) {
            if (hasBluetoothPermission()) {
                binding.bluetoothPermission.checkImage.isVisible = true
                binding.bluetoothPermission.checkImage.imageTintList =
                    ColorStateList.valueOf(accentColor())
            }
        }
    }

    private fun hasStoragePermission(): Boolean {
        return hasPermissions()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun hasBluetoothPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasAudioPermission(): Boolean {
        return Settings.System.canWrite(this)
    }
}
