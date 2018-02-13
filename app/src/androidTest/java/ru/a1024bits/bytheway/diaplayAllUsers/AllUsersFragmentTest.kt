package ru.a1024bits.bytheway.diaplayAllUsers

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.DrawerActions
import android.support.test.espresso.contrib.DrawerMatchers.isClosed
import android.support.test.espresso.contrib.NavigationViewActions
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.Gravity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.DisplayAllUsersAdapter
import ru.a1024bits.bytheway.ui.activity.MenuActivity


@RunWith(AndroidJUnit4::class)
class AllUsersFragmentTest {
    @Rule
    @JvmField
    val mActivityRule = ActivityTestRule<MenuActivity>(MenuActivity::class.java)

    @Before
    fun init() {
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open())
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.all_users_item))
        onView(withId(R.id.displayAllUsers)).check(matches(isEnabled()))
    }

    @Test
    fun testShowUsers() {
        onView(withId(R.id.displayAllUsers))
                .check(matches(isDisplayed()))
                .perform(RecyclerViewActions.actionOnItemAtPosition<DisplayAllUsersAdapter.UserViewHolder>(1, click()))
        init()
    }
}
