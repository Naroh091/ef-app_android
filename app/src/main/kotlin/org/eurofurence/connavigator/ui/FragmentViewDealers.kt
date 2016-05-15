package org.eurofurence.connavigator.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import io.swagger.client.model.Dealer
import org.eurofurence.connavigator.R
import org.eurofurence.connavigator.database.Database
import org.eurofurence.connavigator.tracking.Analytics
import org.eurofurence.connavigator.ui.adapter.DealerRecyclerAdapter
import org.eurofurence.connavigator.util.delegators.view
import org.eurofurence.connavigator.util.extensions.letRoot

/**
 * Created by David on 15-5-2016.
 */
class FragmentViewDealers : Fragment(), TextWatcher {
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // pass
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val query = dealersSearch.text

        effective_dealers = database.dealerDb.items.filter { it.attendeeNickname.contains(query, true) }

        dealersRecycler.adapter = DealerRecyclerAdapter(effective_dealers)

        dealersRecycler.adapter.notifyDataSetChanged()
    }

    val database: Database get() = letRoot { it.database }!!
    val dealersRecycler by view(RecyclerView::class.java)
    val dealersSearch by view(EditText::class.java)

    var effective_dealers = emptyList<Dealer>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fview_dealers, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        Analytics.changeScreenName("View About")

        dealersSearch.setSingleLine()
        effective_dealers = database.dealerDb.items.toList()

        dealersRecycler.adapter = DealerRecyclerAdapter(effective_dealers)
        dealersRecycler.layoutManager = LinearLayoutManager(activity)
        dealersRecycler.itemAnimator = DefaultItemAnimator()

        dealersSearch.addTextChangedListener(this)
    }
}