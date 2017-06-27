package org.eurofurence.connavigator.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import org.eurofurence.connavigator.BuildConfig
import org.eurofurence.connavigator.R
import org.eurofurence.connavigator.database.*
import org.eurofurence.connavigator.net.imageService
import org.eurofurence.connavigator.tracking.Analytics
import org.eurofurence.connavigator.util.delegators.view
import org.eurofurence.connavigator.util.extensions.booleans
import org.eurofurence.connavigator.util.extensions.get
import org.eurofurence.connavigator.util.extensions.localReceiver
import org.eurofurence.connavigator.util.extensions.logd

/**
 * Created by David on 28-4-2016.
 */
class ActivityStart : AppCompatActivity(), HasDb {
    val buttonStart: Button by view()
    val textHelp: TextView by view()
    val progressBar: ProgressBar by view()

    override val db by lazyLocateDb()

    val updateReceiver = localReceiver(UpdateIntentService.UPDATE_COMPLETE) {
        if (it.booleans["success"]) {
            val database = locateDb()

            /* TODO
            for (map in database.mapEntityDb.items) {
                logd { "Preloading map ${map.description}" }
                val image = database.imageDb[map.imageId]!!
                imageService.preload(image)
            }

            for (image in database.imageDb.items) {
                logd { "Preloading image ${image.title}" }
                imageService.preload(image)
            }
            */

            progressBar.visibility = View.GONE

            textHelp.text = "There, all done!"
            buttonStart.text = "Get Started!"
            buttonStart.visibility = View.VISIBLE

            buttonStart.setOnClickListener { startRootActivity() }
        } else {
            textHelp.text = "Failed to successfully get data. Are you connected to the internet?"
            progressBar.visibility = View.GONE
            buttonStart.visibility = View.VISIBLE
            buttonStart.text = "Retry"
            buttonStart.setOnClickListener {
                UpdateIntentService.dispatchUpdate(this)
                progressBar.visibility = View.VISIBLE
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        Analytics.screen("Start")

        // Version Check
        val version = version
        if (version != null && version.split(".")[1] != BuildConfig.VERSION_NAME.split(".")[1]) {
            // We're on an old version (v1.8 instead of v1.9 So we empty the database and exit
            AlertDialog.Builder(this)
                    .setTitle("Outdated version found")
                    .setMessage("Your version is outdated. Because you might be missing critical data in your synced versions. Sadly you will need a complete resync.")
                    .setPositiveButton("Clear Data and restart", { _, _ -> clear(); System.exit(1) })
                    .setNeutralButton("No, just exit", { _, _ -> System.exit(1) })
                    .show()
        } else {
            // Data is present, if a database has a backing file
            if (days.isPresent)
                startRootActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        updateReceiver.register()
    }

    override fun onPause() {
        super.onPause()
        updateReceiver.unregister()
    }

    private fun startRootActivity() {
        version = BuildConfig.VERSION_NAME

        startActivity(Intent(this, ActivityRoot::class.java))
        finish()
    }
}