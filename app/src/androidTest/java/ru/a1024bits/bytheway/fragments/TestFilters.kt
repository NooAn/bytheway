package ru.a1024bits.bytheway.fragments

import android.support.test.espresso.Espresso.*
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.*
import android.support.test.espresso.contrib.*
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.Gravity
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.ui.activity.MenuActivity

@RunWith(AndroidJUnit4::class)

class TestFilters {
    @Rule
    @JvmField
    val mActivityRule = ActivityTestRule<MenuActivity>(MenuActivity::class.java)

    //    @Before
    fun init() {
        onView(ViewMatchers.withId(R.id.drawer_layout))
                .check(ViewAssertions.matches(DrawerMatchers.isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open())
        onView(ViewMatchers.withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.all_users_item))
        onView(ViewMatchers.withId(R.id.displayAllUsers)).check(ViewAssertions.matches(ViewMatchers.isEnabled()))
        onView(withId(R.id.searchParametersText)).check(matches(isDisplayed())).perform(click())
//        onView(withId(R.id.block_search_parameters)).check(matches(isDisplayed()))
    }


    @Test
    fun testBubgets() {
        onView(withId(R.id.startBudget)).check(matches(isDisplayed()))
        onView(withId(R.id.endBudget)).check(matches(isDisplayed()))
    }

    @Test

    fun testAges() {
        onView(withId(R.id.startAge)).check(matches(withSpinnerText("0")))
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("12"))).perform(click())
        onView(withId(R.id.startAge)).check(matches(withSpinnerText(containsString("12"))))

        onView(withId(R.id.endAge)).check(matches(withSpinnerText(containsString("80"))))
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("80"))).perform(click())
        onView(withId(R.id.endAge)).check(matches(withSpinnerText(containsString("80"))))
    }

    @Test
    fun testSex() {
        onView(withId(R.id.sexAny)).check(matches(isChecked()))
    }

    @Test
    fun testCities() {
        onView(withId(R.id.startCity)).check(matches(withText("")))
        onView(withId(R.id.endCity)).check(matches(withText("")))
    }
}