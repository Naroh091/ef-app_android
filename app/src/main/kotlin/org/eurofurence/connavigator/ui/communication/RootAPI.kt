package org.eurofurence.connavigator.ui.communication

import android.content.SharedPreferences
import android.support.design.widget.TabLayout
import io.swagger.client.model.Dealer
import io.swagger.client.model.EventEntry
import io.swagger.client.model.Info
import org.eurofurence.connavigator.database.Database

/**
 * Created by Pazuzu on 12.04.2016.
 */
interface RootAPI {
    val database: Database

    val tabs: TabLayout

    val preferences: SharedPreferences

    fun makeSnackbar(text: String)

    fun navigateToEvent(eventEntry: EventEntry)

    fun navigateToInfo(info: Info)

    fun navigateToDealer(dealer: Dealer)

    fun changeTitle(text: String)
}