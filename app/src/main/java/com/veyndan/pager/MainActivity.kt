package com.veyndan.pager

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*

// TODO Expose [TabLayout] and [ViewPager] so customisations can be done without the library needing to handle it
// I should basically just do the basic set up of the view pager and tabs and require the pages are described in Enums.
// If the user of the library decides to delete a page (for example) the library should gracefully handle it <-- Possibly? It contradicts what I was saying earlier with forcing them into using enums.
// Prefer extension functions as my implementation should be generic. In my opinion, the Android framework should've used enums to work with the ViewPager/TabLayout

enum class PagerAdapterType {
    FRAGMENT {
        override fun <T : Page> adapter(fm: FragmentManager, pages: Array<T>) =
                EnumFragmentPagerAdapter(fm, pages)
    },
    FRAGMENT_STATE {
        override fun <T : Page> adapter(fm: FragmentManager, pages: Array<T>) =
                EnumFragmentStatePagerAdapter(fm, pages)
    },
    VIEW {
        override fun <T : Page> adapter(fm: FragmentManager, pages: Array<T>) =
                TODO("EnumPagerAdapter(fm, pages)")
    };

    abstract fun <T : Page> adapter(fm: FragmentManager, pages: Array<T>): PagerAdapter
}

interface Page {
    val tabText: CharSequence
    val fragment: Fragment
}

inline fun <reified T> AppCompatActivity.paginateViews(
        pager: ViewPager,
        tabLayout: TabLayout,
        pagerAdapterType: PagerAdapterType = PagerAdapterType.FRAGMENT_STATE
) where T : Enum<T>, T : Page {
    pager.adapter = pagerAdapterType.adapter(supportFragmentManager, enumValues<T>())
    with(tabLayout) {
        enumValues<T>().forEach { addTab(newTab()) }
        setupWithViewPager(pager)
        enumValues<T>().forEachIndexed { index, page -> getTabAt(index)!!.text = page.tabText }
    }
}

class EnumFragmentPagerAdapter<T : Page>(
        fm: FragmentManager,
        private val pages: Array<T>
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int) = pages[position].fragment

    override fun getCount() = pages.size
}

class EnumFragmentStatePagerAdapter<S : Page>(
        fm: FragmentManager,
        private val pages: Array<S>
) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int) = pages[position].fragment

    override fun getCount() = pages.size
}

class MainActivity : AppCompatActivity() {

    enum class ArtistPage(override val tabText: CharSequence, override val fragment: Fragment) : Page {

        OVERVIEW("Overview", PageFragment.newInstance(0)),
        ALBUMS("Albums", PageFragment.newInstance(1)),
        SIMILAR_ARTISTS("Similar artists", PageFragment.newInstance(2)),
        SOCIAL_MEDIA("Social media", PageFragment.newInstance(3));
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        paginateViews<ArtistPage>(viewPager, tabLayout)
    }

    class PageFragment : Fragment() {

        private var position: Int? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (arguments != null) {
                position = arguments!!.getInt(ARG_POSITION)
            }
        }

        override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
        ): View? = inflater.inflate(R.layout.fragment_page, container, false)

        companion object {

            private const val ARG_POSITION = "position"

            fun newInstance(position: Int) = PageFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POSITION, position)
                }
            }
        }
    }
}
