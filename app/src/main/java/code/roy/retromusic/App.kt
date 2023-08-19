package code.roy.retromusic

import android.app.Application
import androidx.preference.PreferenceManager
import cat.ereza.customactivityoncrash.config.CaocConfig
import code.roy.monkey.retromusic.billing.BillingManager
import code.roy.appthemehelper.ThemeStore
import code.roy.appthemehelper.util.VersionUtils
import code.roy.retromusic.activities.ErrorActivity
import code.roy.retromusic.activities.MainActivity
import code.roy.retromusic.appshortcuts.DynamicShortcutManager
import code.roy.retromusic.helper.WallpaperAccentManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

//TODO applovin
//TODO firebase

//TODO ic_launcher
//TODO more app
//TODO policy
//TODO keystore

//done
//leak canary
//proguard
//manifest ad id
//share app
//rate app

class App : Application() {

    private lateinit var billingManager: BillingManager
    private val wallpaperAccentManager = WallpaperAccentManager(this)

    override fun onCreate() {
        super.onCreate()
        instance = this

        startKoin {
            androidContext(this@App)
            modules(appModules)
        }
        // default theme
        if (!ThemeStore.isConfigured(this, 3)) {
            ThemeStore.editTheme(this)
                .accentColorRes(code.roy.appthemehelper.R.color.md_deep_purple_A200)
                .coloredNavigationBar(true)
                .commit()
        }
        wallpaperAccentManager.init()

        if (VersionUtils.hasNougatMR())
            DynamicShortcutManager(this).initDynamicShortcuts()

        billingManager = BillingManager(this)

        // setting Error activity
        CaocConfig.Builder.create().errorActivity(ErrorActivity::class.java)
            .restartActivity(MainActivity::class.java).apply()

        // Set Default values for now playing preferences
        // This will reduce startup time for now playing settings fragment as Preference listener of AbsSlidingMusicPanelActivity won't be called
        PreferenceManager.setDefaultValues(
            /* context = */ this,
            /* resId = */ R.xml.pref_now_playing_screen,
            /* readAgain = */ false
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        billingManager.release()
        wallpaperAccentManager.release()
    }

    companion object {
        private var instance: App? = null

        fun getContext(): App {
            return instance!!
        }

        fun isProVersion(): Boolean {
//            return BuildConfig.DEBUG || instance?.billingManager!!.isProVersion
            return true
        }
    }
}
