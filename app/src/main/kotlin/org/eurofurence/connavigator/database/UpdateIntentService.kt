package org.eurofurence.connavigator.database

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.pawegio.kandroid.runOnUiThread
import org.eurofurence.connavigator.broadcast.EventFavoriteBroadcast
import org.eurofurence.connavigator.net.imageService
import org.eurofurence.connavigator.pref.DebugPreferences
import org.eurofurence.connavigator.pref.RemotePreferences
import org.eurofurence.connavigator.util.extensions.*
import org.eurofurence.connavigator.util.v2.convert
import org.eurofurence.connavigator.util.v2.internalSpec
import org.eurofurence.connavigator.webapi.apiService
import org.jetbrains.anko.longToast
import org.joda.time.DateTime
import java.util.*
import kotlin.serialization.Serializable

@Serializable
data class UpdateComplete(val success: Boolean, val time: Date?, val exception: Throwable?)

val updateComplete = internalSpec<UpdateComplete>()

/**
 * Updates the database on request.
 */
class UpdateIntentService : IntentService("UpdateIntentService"), HasDb {
    companion object {
        val UPDATE_COMPLETE = "org.eurofurence.connavigator.driver.UPDATE_COMPLETE"
        val REQUEST_CODE = 1337

        /**
         * Dispatches an update
         * @param context The host context for the service
         */
        fun dispatchUpdate(context: Context, showToastOnCompletion: Boolean = false) {
            logv("UIS") { "Dispatching update" }

            val intent = Intent(context, UpdateIntentService::class.java)
            intent.putExtra("showToastOnCompletion", showToastOnCompletion)

            context.startService(intent)
        }
    }

    override val db by lazyLocateDb()

    val updateCompleteMsg by updateComplete

    // TODO: Sticky intent since there should only be one pending update
    override fun onHandleIntent(intent: Intent?) {
        logv("UIS") { "Handling update intent service intent" }
        val showToastOnCompletion = intent?.getBooleanExtra("showToastOnCompletion", false) ?: false

        // Initialize the response, the following code is net and IO oriented, it could fail
        val (response, responseObject) = {
            logv("UIS") { "Updating remote preferences" }
            RemotePreferences.update()
            logv("UIS") { "Retrieving sync since $date" }

            // Get sync from server
            val sync = apiService.sync.apiV2SyncGet(date)

            logv("UIS") { sync }

            val shift = DebugPreferences.debugDates
            val shiftOffset = DebugPreferences.eventDateOffset

            if (shift) {
                logd { "Changing dates instead of updating" }
                // Get all dates explicitly
                val base = apiService.days.apiV2EventConferenceDaysGet()

                // Shift by offset
                val currentDate = DateTime.now()
                for ((i, day) in base.sortedBy { it.date }.withIndex())
                    day.date = currentDate.plusDays(i - shiftOffset).toDate()

                // Make a new sync package entry
                sync.eventConferenceDays.removeAllBeforeInsert = true
                sync.eventConferenceDays.changedEntities = base
                sync.eventConferenceDays.deletedEntities = emptyList()
            }

            for (image in sync.images.changedEntities)
                imageService.recache(image)


            // Apply sync
            announcements.apply(sync.announcements.convert())
            dealers.apply(sync.dealers.convert())
            days.apply(sync.eventConferenceDays.convert())
            rooms.apply(sync.eventConferenceRooms.convert())
            tracks.apply(sync.eventConferenceTracks.convert())
            events.apply(sync.events.convert())
            images.apply(sync.images.convert())
            knowledgeEntries.apply(sync.knowledgeEntries.convert())
            knowledgeGroups.apply(sync.knowledgeGroups.convert())
            maps.apply(sync.maps.convert())

            // Reconcile events with favorites
            faves = events.items.map { it.id}
                    .toSet()
                    .let {
                        eventsIds -> faves.filter { it in eventsIds }
                    }

            // Update notifications for favorites
            EventFavoriteBroadcast().updateNotificatons(applicationContext, faves)

            // Store new time
            date = sync.currentDateTimeUtc

            logv("UIS") { "Synced, current sync time is $date" }

            // Make the success response message
            logv("UIS") { "Completed update successfully" }

            if (showToastOnCompletion) {
                runOnUiThread {
                    longToast("Successfully updated all content.")
                }
            }

            UPDATE_COMPLETE.toIntent {
                booleans["success"] = true
                objects["time"] = date
            } to UpdateComplete(true, date, null)
        } catchAlternative { ex: Throwable ->
            // Make the fail response message, transfer exception
            loge("UIS", ex) { "Completed update with error" }
            UPDATE_COMPLETE.toIntent {
                booleans["success"] = false
                objects["time"] = date
                objects["reason"] = ex
            } to UpdateComplete(false, date, ex)
        }

        // Send a broadcast notifying completion of this action
        LocalBroadcastManager.getInstance(this).sendBroadcast(response)

        updateCompleteMsg.send(responseObject)
    }
}