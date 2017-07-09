package org.eurofurence.connavigator.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.pawegio.kandroid.textWatcher
import org.eurofurence.connavigator.R
import org.eurofurence.connavigator.pref.AppPreferences
import org.eurofurence.connavigator.database.*
import org.eurofurence.connavigator.tracking.Analytics
import org.eurofurence.connavigator.ui.communication.ContentAPI
import org.eurofurence.connavigator.ui.fragments.EventRecyclerFragment
import org.eurofurence.connavigator.util.delegators.view
import org.eurofurence.connavigator.util.extensions.applyOnRoot
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by David on 5/3/2016.
 */
class FragmentViewEvents : Fragment(), ContentAPI, HasDb {
    override val db by lazyLocateDb()

    inner class EventFragmentPagerAdapter : FragmentStatePagerAdapter(childFragmentManager) {
        override fun getPageTitle(position: Int): CharSequence? {
            if (AppPreferences.shortenDates) {
                return DateTime(days.asc { it.date }[position].date).dayOfWeek().asShortText
            } else {
                return DateTime(days.asc { it.date }[position].date).toString(DateTimeFormat.forPattern("MMM d"))
            }
        }

        override fun getItem(position: Int): Fragment? {
            return EventRecyclerFragment(filterEvents()
                    .onDay(days.asc { it.date }[position].id)
                    .sortByStartTime())
        }

        override fun getCount(): Int {
            return days.size
        }
    }

    val eventPager: ViewPager by view()
    val eventSearchBar: EditText by view()

    val searchEventFilter by lazy { filterEvents() }

    val searchFragment by lazy { EventRecyclerFragment(searchEventFilter) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fview_events_viewpager, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        Analytics.screen(activity, "Events Listing")

        configureViewpager()

        childFragmentManager.beginTransaction()
                .replace(R.id.eventSearch, searchFragment)
                .commitAllowingStateLoss()

        eventSearchBar.textWatcher {
            afterTextChanged { text ->
                searchEventFilter.byTitle(text.toString())
                searchFragment.dataUpdated()
            }
        }

        applyOnRoot { tabs.setupWithViewPager(eventPager) }
        applyOnRoot { changeTitle("Event Schedule") }
    }

    private fun configureViewpager() {
        eventPager.adapter = EventFragmentPagerAdapter()
        eventPager.offscreenPageLimit = 1

        eventPager.currentItem = db.eventDayNumber()
    }

    override fun dataUpdated() {
        eventPager.adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        applyOnRoot { tabs.setupWithViewPager(null) }
    }

    override fun onSearchButtonClick() {
        when (eventPager.visibility) {
            View.VISIBLE -> {
                eventPager.visibility = View.GONE
                activity.findViewById(R.id.searchLayout).visibility = View.VISIBLE
            }
            else -> {
                eventPager.visibility = View.VISIBLE
                activity.findViewById(R.id.searchLayout).visibility = View.GONE
            }
        }
    }
}