package ru.a1024bits.bytheway.viewmodel

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.a1024bits.bytheway.R
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
        currentTime = Calendar.getInstance()
        Log.d("testing", "start fun \"initialize\" in DisplayUsersViewModelTest(instrumental)")
    }

    @Test
    fun testGetTextFromDates() {
        Assert.assertTrue(displayUsersViewModel.getTextFromDates(currentTime.timeInMillis, 0L, context.resources.getStringArray(R.array.months_array)).isNotEmpty())
    }

}