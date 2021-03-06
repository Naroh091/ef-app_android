package org.eurofurence.connavigator.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.eurofurence.connavigator.R
import org.eurofurence.connavigator.database.HasDb
import org.eurofurence.connavigator.database.lazyLocateDb
import org.eurofurence.connavigator.ui.communication.ContentAPI
import org.eurofurence.connavigator.ui.fragments.FragmentMap
import org.eurofurence.connavigator.util.extensions.applyOnRoot
import org.eurofurence.connavigator.util.extensions.multitouchViewPager
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.viewPager

/**
 * Created by david on 8/3/16.
 */
class FragmentViewMaps : Fragment(), ContentAPI, HasDb, NavRepresented {

    val ui by lazy { MapsUi() }
    override val db by lazyLocateDb()
    override val drawerItemId: Int
        get() = R.id.navMaps

    inner class MapFragmentPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {
        override fun getPageTitle(position: Int) =
                browseableMaps[position].description

        override fun getItem(position: Int) =
                FragmentMap().withArguments(browseableMaps[position])


        override fun getCount() =
                browseableMaps.size
    }

    val browseableMaps by lazy { maps.items.filter { it.isBrowseable == true }.sortedBy { it.description } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            UI { ui.createView(this) }.view

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui.mapViewPager.adapter = MapFragmentPagerAdapter(childFragmentManager)

        applyOnRoot {
            tabs.setupWithViewPager(ui.mapViewPager)
            changeTitle("Maps")
        }
    }
}


class MapsUi : AnkoComponent<Fragment> {
    lateinit var mapViewPager: ViewPager
    override fun createView(ui: AnkoContext<Fragment>) = with(ui) {
        frameLayout() {
            mapViewPager = multitouchViewPager {
                id = View.generateViewId()
            }.lparams(matchParent, matchParent)
        }
    }
}
