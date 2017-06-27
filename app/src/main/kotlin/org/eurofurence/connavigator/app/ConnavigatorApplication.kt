package org.eurofurence.connavigator.app

import android.support.multidex.MultiDexApplication
import com.chibatching.kotpref.Kotpref
import net.danlew.android.joda.JodaTimeAndroid
import nl.komponents.kovenant.android.startKovenant
import org.eurofurence.connavigator.database.UpdateIntentService
import org.eurofurence.connavigator.gcm.PushListenerService
import org.eurofurence.connavigator.net.imageService
import org.eurofurence.connavigator.pref.RemotePreferences
import org.eurofurence.connavigator.tracking.Analytics
import org.eurofurence.connavigator.util.extensions.logd
import org.eurofurence.connavigator.util.extensions.logv
import org.eurofurence.connavigator.webapi.apiService

/**
 * The application initialization point.
 */
class ConnavigatorApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        // This configures the timezone database for JODA time, that way timezone info can be used without having the
        // impractical database of JODAgst
        JodaTimeAndroid.init(this)

        // Preferences
        Kotpref.init(this)

        // Initialize some services
        imageService.initialize(this)
        logService.initialize(this)

        RemotePreferences.init()

        apiService.initialize(this)
        Analytics.init(this)

        // Promises
        startKovenant()

        // Listen to cloud updates
        PushListenerService().subscribe()

        // Update every 5 minutes
        UpdateIntentService.dispatchUpdate(this)
    }
}