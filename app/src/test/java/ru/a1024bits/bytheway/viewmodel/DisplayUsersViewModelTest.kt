package ru.a1024bits.bytheway.viewmodel

import android.util.Log
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.START_DATE
import java.util.*

private val AGE_PARAMETER = "age"
private val ADD_INFORMATION_PARAMETER = "addInformation"
private val PHONE_PARAMETER = "phone"
private val EMAIL_PARAMETER = "email"
private val LAST_NAME_PARAMETER = "lastName"
private val NAME_PARAMETER = "name"
private val BUDGET_PARAMETER = "budget"

@RunWith(JUnit4::class)
class DisplayUsersViewModelTest {
    private lateinit var displayUsersViewModel: DisplayUsersViewModel
    val countAllUsers = 50
    val countSimilarUsers = 5

    @Before
    fun initialize() {
        displayUsersViewModel = DisplayUsersViewModel(null)
        Log.d("testing", "start fun \"initialize\" in DisplayUsersViewModelTest(junit)")
    }

    @Test
    fun testFilterUsersByFilters() {
        var filter = Filter()
        var filtrationValue = "15"
        filter.startAge = filtrationValue.toInt() - 1
        filter.endAge = filtrationValue.toInt() + 1
        var usersForFilterFiltration = generateUsersForFiltration(countAllUsers, countSimilarUsers, AGE_PARAMETER, filtrationValue)
        displayUsersViewModel.filterUsersByFilter(usersForFilterFiltration, filter)
        Assert.assertEquals(usersForFilterFiltration.size, countSimilarUsers)

        filter = Filter()
        filtrationValue = "13232"
        filter.startBudget = filtrationValue.toInt() - 1
        filter.endBudget = filtrationValue.toInt() + 1
        usersForFilterFiltration = generateUsersForFiltration(countAllUsers, countSimilarUsers, BUDGET_PARAMETER, filtrationValue)
        displayUsersViewModel.filterUsersByFilter(usersForFilterFiltration, filter)
        Assert.assertEquals(usersForFilterFiltration.size, countSimilarUsers)

        filter = Filter()
        filtrationValue = Calendar.getInstance().timeInMillis.toString()
        filter.endDate = filtrationValue.toLong() + 10
        usersForFilterFiltration = generateUsersForFiltration(countAllUsers, countSimilarUsers, END_DATE, filtrationValue)
        displayUsersViewModel.filterUsersByFilter(usersForFilterFiltration, filter)
        Assert.assertEquals(usersForFilterFiltration.size, countSimilarUsers)

        filter = Filter()
        filtrationValue = Calendar.getInstance().timeInMillis.toString()
        filter.startDate = filtrationValue.toLong() - 10
        usersForFilterFiltration = generateUsersForFiltration(countAllUsers, countSimilarUsers, START_DATE, filtrationValue)
        displayUsersViewModel.filterUsersByFilter(usersForFilterFiltration, filter)
        Assert.assertEquals(usersForFilterFiltration.size, countSimilarUsers)

        filter = Filter()
        filtrationValue = "CityOne"
        filter.startCity = filtrationValue
        usersForFilterFiltration = generateUsersForFiltration(countAllUsers, countSimilarUsers, FIRST_INDEX_CITY, filtrationValue)
        displayUsersViewModel.filterUsersByFilter(usersForFilterFiltration, filter)
        Assert.assertEquals(usersForFilterFiltration.size, countSimilarUsers)

        filter = Filter()
        filtrationValue = "CityTwo"
        filter.endCity = filtrationValue
        usersForFilterFiltration = generateUsersForFiltration(countAllUsers, countSimilarUsers, LAST_INDEX_CITY, filtrationValue)
        displayUsersViewModel.filterUsersByFilter(usersForFilterFiltration, filter)
        Assert.assertEquals(usersForFilterFiltration.size, countSimilarUsers)
    }

    @Test
    fun testFilterUsersByStringInRandomVariable() {
        var queryStringFiltration = "info"
        var variableName = ADD_INFORMATION_PARAMETER
        Assert.assertEquals(displayUsersViewModel.filterUsersByString(
                queryStringFiltration, generateUsersForFiltration(countAllUsers, countSimilarUsers, variableName, queryStringFiltration)
        ).size, countSimilarUsers)

        variableName = NAME_PARAMETER
        queryStringFiltration = "ExampleName"
        Assert.assertEquals(displayUsersViewModel.filterUsersByString(
                queryStringFiltration, generateUsersForFiltration(countAllUsers, countSimilarUsers, variableName, queryStringFiltration)
        ).size, countSimilarUsers)

        variableName = EMAIL_PARAMETER
        queryStringFiltration = "exampleEmail@mail.com"
        Assert.assertEquals(displayUsersViewModel.filterUsersByString(
                queryStringFiltration, generateUsersForFiltration(countAllUsers, countSimilarUsers, variableName, queryStringFiltration)
        ).size, countSimilarUsers)

        variableName = AGE_PARAMETER
        queryStringFiltration = "17"
        Assert.assertEquals(displayUsersViewModel.filterUsersByString(
                queryStringFiltration, generateUsersForFiltration(countAllUsers, countSimilarUsers, variableName, queryStringFiltration)
        ).size, countSimilarUsers)

        variableName = LAST_NAME_PARAMETER
        queryStringFiltration = "ExampleLastName"
        Assert.assertEquals(displayUsersViewModel.filterUsersByString(
                queryStringFiltration, generateUsersForFiltration(countAllUsers, countSimilarUsers, variableName, queryStringFiltration)
        ).size, countSimilarUsers)

        variableName = PHONE_PARAMETER
        queryStringFiltration = "380123456789"
        Assert.assertEquals(displayUsersViewModel.filterUsersByString(
                queryStringFiltration, generateUsersForFiltration(countAllUsers, countSimilarUsers, variableName, queryStringFiltration)
        ).size, countSimilarUsers)

        variableName = FIRST_INDEX_CITY
        queryStringFiltration = "CityFirst"
        Assert.assertEquals(displayUsersViewModel.filterUsersByString(
                queryStringFiltration, generateUsersForFiltration(countAllUsers, countSimilarUsers, variableName, queryStringFiltration)
        ).size, countSimilarUsers)

        variableName = LAST_INDEX_CITY
        queryStringFiltration = "CityLast"
        Assert.assertEquals(displayUsersViewModel.filterUsersByString(
                queryStringFiltration, generateUsersForFiltration(countAllUsers, countSimilarUsers, variableName, queryStringFiltration)
        ).size, countSimilarUsers)
    }

    private fun generateUsersForFiltration(countAllUsers: Int, countSimilarUsers: Int, currentFiltrationVariable: String, valueFiltrationVariable: String): MutableList<User> {
        val originalUsers = ArrayList<User>()
        for (i in 0 until countSimilarUsers) {
            val currentUser = User()
            when (currentFiltrationVariable) {
                AGE_PARAMETER -> currentUser.age = valueFiltrationVariable.toInt()
                ADD_INFORMATION_PARAMETER -> currentUser.addInformation = valueFiltrationVariable
                PHONE_PARAMETER -> currentUser.phone = valueFiltrationVariable
                EMAIL_PARAMETER -> currentUser.email = valueFiltrationVariable
                LAST_NAME_PARAMETER -> currentUser.lastName = valueFiltrationVariable
                FIRST_INDEX_CITY -> currentUser.cities[FIRST_INDEX_CITY] = valueFiltrationVariable
                LAST_INDEX_CITY -> currentUser.cities[LAST_INDEX_CITY] = valueFiltrationVariable
                NAME_PARAMETER -> currentUser.name = valueFiltrationVariable
                BUDGET_PARAMETER -> currentUser.budget = valueFiltrationVariable.toLong()
                END_DATE -> currentUser.dates[END_DATE] = valueFiltrationVariable.toLong()
                START_DATE -> currentUser.dates[START_DATE] = valueFiltrationVariable.toLong()

            }
            originalUsers.add(currentUser)
        }
        for (i in 0 until countAllUsers - countSimilarUsers) originalUsers.add(User())
        return originalUsers
    }
}