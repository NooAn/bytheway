package ru.a1024bits.bytheway.viewmodel

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class DisplayUsersViewModelInstrumentalTest {
    private lateinit var displayUsersViewModel: DisplayUsersViewModel
    private lateinit var currentTime: Calendar
    private lateinit var context: Context


    @Before
    fun initialize() {
        context = InstrumentationRegistry.getTargetContext().applicationContext
        displayUsersViewModel = DisplayUsersViewModel(null)
        displayUsersViewModel.context = context
        currentTime = Calendar.getInstance()
        Log.d("testing", "start fun \"initialize\" in DisplayUsersViewModelTest(instrumental)")
    }

    @Test
    fun testGetTextFromDates() {
        Assert.assertTrue(displayUsersViewModel.getTextFromDates(currentTime.timeInMillis, 0L).isNotEmpty())
    }

//    @Test
//    fun testFilterUsersByString() {
//        val countAllUsers = 50
//        val countSimilarUsers = 5
//
//        val queury = "user name 1"
//        val resultUsers = displayUsersViewModel.filterUsersByString(queury, generateUsers(countAllUsers, countSimilarUsers, queury))
//        Assert.assertEquals(resultUsers.size, countSimilarUsers)
//    }
//
//    private fun generateUsers(countAllUsers: Int, countSimilarUsers: Int, queury: String): MutableList<User> {
//        val originalUsers = ArrayList<User>()
//        val random = Random()
//        for (i in 0 until countSimilarUsers) {
//            val currentUser = User()
//            when (random.nextInt(6)) {
//                0 -> currentUser.addInformation = queury
//                1 -> currentUser.phone = queury
//                2 -> currentUser.cities.put("firstCity", queury)
//                3 -> currentUser.email = queury
//                4 -> currentUser.lastName = queury
//                5 -> currentUser.name = queury
//            }
//            originalUsers.add(currentUser)
//        }
//        for (i in 0 until countAllUsers - countSimilarUsers) originalUsers.add(User())
//        return originalUsers
//    }
}