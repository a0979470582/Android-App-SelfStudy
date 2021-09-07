package com.bu.selfstudy.book

import android.content.Context
import android.view.Gravity
import androidx.appcompat.view.menu.ActionMenuItem
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import com.bu.selfstudy.NavGraphDirections
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.tool.viewBinding
import com.bu.selfstudy.ui.book.BookFragment
import com.bu.selfstudy.ui.main.MainActivity
import com.bu.selfstudy.ui.search.SearchFragment
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import org.hamcrest.JavaLangMatcherAssert.that
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.regex.Matcher


@MediumTest
@RunWith(AndroidJUnit4::class)
class BookFragmentTest {

    @Rule
    @JvmField
    val scenario = ActivityScenarioRule(MainActivity::class.java)

    lateinit var navController: NavController

    @Before
    fun navigateBookFragment(){
        scenario.scenario.onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
            navController.navigate(R.id.bookFragment)
        }
    }

    @Test
    fun testClickHomeIcon(){
        onView(withContentDescription("開啟導覽匣")).perform(click())
        onView(withId(R.id.navView)).check(matches(isDisplayed()))
    }

    @Test
    fun testClickToolbar(){
        onView(withId(R.id.toolbar)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.searchFragment)
    }

    @Test
    fun testClickSearchIcon(){
        onView(withId(R.id.searchFragment)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.searchFragment)
    }

    @Test
    fun testClickMainFab(){
        onView(withId(R.id.speedDialView)).perform(click())
        onView(withId(R.id.book_fragment_fab_add_word)).check(matches(isDisplayed()))
        onView(withId(R.id.book_fragment_fab_add_book)).check(matches(isDisplayed()))
        //onView(withId(R.id.overlay3)).check(matches(isDisplayed()))

    }

    @Test
    fun testClickAddBook(){
        onView(withId(R.id.speedDialView)).perform(click())
        onView(withId(R.id.book_fragment_fab_add_book)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.addBookFragment)
    }

    @Test
    fun testClickAddWord(){
        onView(withId(R.id.speedDialView)).perform(click())
        onView(withId(R.id.book_fragment_fab_add_word)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.searchFragment)
    }

    @Test
    fun AddBookToDBThenRemove(){

    }

}