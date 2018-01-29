package ru.a1024bits.bytheway.viewmodel

import android.content.Context
import android.util.Log
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import ru.a1024bits.bytheway.model.User
import java.util.*

@RunWith(JUnit4::class)
class DisplayUsersViewModelTest {
    private lateinit var displayUsersViewModel: DisplayUsersViewModel
    private lateinit var originalUsers: MutableList<User>
    val queury = "user name 1"
    val countAllUsers = 50
    val countSimilarUsers = 5

    @Before
    fun initialize() {
        displayUsersViewModel = DisplayUsersViewModel(null)
        Log.d("testing", "start fun \"initialize\" in DisplayUsersViewModelTest(junit)")

        originalUsers = generateUsers(countAllUsers, countSimilarUsers, queury)
    }

    @Test
    fun testFilterUsersByStringRandomVariable() {
        val resultUsers = displayUsersViewModel.filterUsersByString(queury, originalUsers)
        Assert.assertEquals(resultUsers.size, countSimilarUsers)
    }

    private fun generateUsers(countAllUsers: Int, countSimilarUsers: Int, queury: String): MutableList<User> {
        val originalUsers = ArrayList<User>()
        val random = Random()
        for (i in 0 until countSimilarUsers) {
            val currentUser = User()
            when (random.nextInt(6)) {
                0 -> currentUser.addInformation = queury
                1 -> currentUser.phone = queury
                2 -> currentUser.cities.put("firstCity", queury)
                3 -> currentUser.email = queury
                4 -> currentUser.lastName = queury
                5 -> currentUser.name = queury
            }
            originalUsers.add(currentUser)
        }
        for (i in 0 until countAllUsers - countSimilarUsers) originalUsers.add(User())
        return originalUsers
    }
}