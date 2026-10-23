package com.uniremote

import android.app.Application
import com.uniremote.data.IdentityStore
import com.uniremote.data.Settings
import com.uniremote.data.ShortcutStore
import com.uniremote.data.TvDetails
import com.uniremote.data.TvStore
import com.uniremote.net.AppLauncher
import com.uniremote.net.Receivers
import com.uniremote.net.RemoteController
import com.uniremote.net.SendController
import com.uniremote.net.TvWatcher
import com.uniremote.ui.TvApps

/** Holds the app-wide pieces; the remote connection and file transfers outlive activity restarts such as rotation. */
class UniRemoteApp : Application() {
    lateinit var tvs: TvStore
        private set
    lateinit var identity: IdentityStore
        private set
    lateinit var settings: Settings
        private set
    lateinit var remote: RemoteController
        private set
    lateinit var sends: SendController
        private set
    lateinit var receivers: Receivers
        private set

    // Shortcuts, About this TV.
    lateinit var shortcuts: ShortcutStore
        private set
    lateinit var tvDetails: TvDetails
        private set
    lateinit var launcher: AppLauncher
        private set

    override fun onCreate() {
        super.onCreate()
        tvs = TvStore(this)
        identity = IdentityStore(this)
        settings = Settings(this)
        receivers = Receivers(this)
        remote = RemoteController(this)
        sends = SendController(this)

        shortcuts = ShortcutStore(this, TvApps.template, TvApps::isIgnored)
        tvDetails = TvDetails(this)
        launcher = AppLauncher(this)
        // Forget what TVs that were unpaired since the last start said or showed.
        val paired = tvs.all().map { it.pin }.toSet()
        shortcuts.keepOnly(paired)
        tvDetails.keepOnly(paired)
        remote.addListener(TvWatcher(this))
    }
}

val android.content.Context.libremote get() = applicationContext as UniRemoteApp

val android.content.Context.uniremote get() = applicationContext as UniRemoteApp
