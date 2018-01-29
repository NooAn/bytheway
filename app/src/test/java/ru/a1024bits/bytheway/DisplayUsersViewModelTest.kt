package ru.a1024bits.bytheway;


import com.borax12.materialdaterangepicker.date.DatePickerDialog
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import ru.a1024bits.bytheway.viewmodel.DisplayUsersViewModel
import java.util.*


@RunWith(JUnit4::class)
class DisplayUsersViewModelTest {
    private val displayUsersViewModel = DisplayUsersViewModel(null)
    val currentTime = Calendar.getInstance()


    @Before
    fun initialize() {}

    @Test
    fun testUpdateDateDialog() {
//        val currentMaxDate = Calendar.getInstance()
        val dateDialog = displayUsersViewModel.updateDateDialog(null)

        dateDialog.registerOnDateChangedListener(object : DatePickerDialog.OnDateChangedListener {
            override fun onDateChanged() {
                Assert.assertTrue(dateDialog.maxDate.timeInMillis == currentTime.timeInMillis)
                dateDialog.unregisterOnDateChangedListener(this)
            }
        })
        dateDialog.maxDate = currentTime

//        val currentMinDate = Calendar.getInstance()
        currentTime.add(Calendar.MONTH, 2)
        dateDialog.registerOnDateChangedListener(object : DatePickerDialog.OnDateChangedListener {
            override fun onDateChanged() {
                Assert.assertTrue(dateDialog.minDate.timeInMillis == currentTime.timeInMillis)
                dateDialog.unregisterOnDateChangedListener(this)
            }
        })
        dateDialog.minDate = currentTime

        currentTime.set(Calendar.MONTH, currentTime.get(Calendar.MONTH) - 2)
    }

    @Test
    fun testGetTextFromDates() {
        Assert.assertTrue(displayUsersViewModel.getTextFromDates(currentTime.timeInMillis, 0L, 1).isNotEmpty())
    }
}
