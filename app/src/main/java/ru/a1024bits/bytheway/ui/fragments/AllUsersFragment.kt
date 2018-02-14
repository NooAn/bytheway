package ru.a1024bits.bytheway.ui.fragments

import android.animation.LayoutTransition
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import com.codetroopers.betterpickers.calendardatepicker.MonthAdapter
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crash.FirebaseCrash
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_display_all_users.*
import kotlinx.android.synthetic.main.searching_parameters_block.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.DisplayAllUsersAdapter
import ru.a1024bits.bytheway.model.Response
import ru.a1024bits.bytheway.model.Status
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.repository.M_SEX
import ru.a1024bits.bytheway.repository.W_SEX
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.util.DateUtils
import ru.a1024bits.bytheway.util.DecimalInputFilter
import ru.a1024bits.bytheway.viewmodel.DisplayUsersViewModel
import java.util.*
import javax.inject.Inject


class AllUsersFragment : BaseFragment<DisplayUsersViewModel>() {
    companion object {
        const val SIZE_INITIAL_ELEMENTS = 2
        const val TAG_ANALYTICS = "AllUsersFragment_"
        fun newInstance(): AllUsersFragment = AllUsersFragment()
    }

    private lateinit var filter: Filter
    private lateinit var displayUsersAdapter: DisplayAllUsersAdapter
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private var sortString = ""
    private var countInitialElements = 0
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var dateDialog: CalendarDatePickerDialogFragment

    private val usersObservers: Observer<Response<List<User>>> = Observer { response ->
        when (response?.status) {
            Status.SUCCESS -> if (response.data == null) showErrorLoading() else {
                if (response.data.isNotEmpty())
                    displayUsersAdapter.setItems(response.data)
                updateViewsAfterSearch(isNotEmptyListUsers = response.data.isNotEmpty())
            }
            Status.ERROR -> {
                Log.e("LOG", "log e:" + response.error)
                showErrorLoading()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        analytics = FirebaseAnalytics.getInstance(this.context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        App.component.inject(this)
        super.onActivityCreated(savedInstanceState)
        (activity as MenuActivity).toolbar.findViewById<EditText>(R.id.searchUsers).visibility = View.VISIBLE
        analytics.setCurrentScreen(this.activity, "AllUsersFragment", this.javaClass.simpleName)

        try {
            viewModel?.let {
                filter = it.filter
            }

            displayUsersAdapter = DisplayAllUsersAdapter(this.context, viewModel ?: return)
            installLogicToUI()
            displayAllUsers.adapter = displayUsersAdapter

            viewModel?.response?.observe(this, usersObservers)
            viewModel?.loadingStatus?.observe(this, (activity?.let { it as MenuActivity })?.progressBarLoad
                    ?: return)
            viewModel?.getAllUsers(filter, sortString)

            showPrompt("isFirstEnterAllUsersFragment", context.resources.getString(R.string.close_hint),
                    context.resources.getString(R.string.hint_all_travelers), context.resources.getString(R.string.hint_all_travelers_description), searchParametersText)
        } catch (e: Throwable) {
            Log.e("LOG_AUF", e.toString())
            FirebaseCrash.report(e)
        }
    }

    override fun onStart() {
        super.onStart()

        val now = Calendar.getInstance()

        dateDialog = CalendarDatePickerDialogFragment()
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setThemeCustom(R.style.BythewayDatePickerDialogTheme)
                .setPreselectedDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
    }

    override fun getViewFactoryClass() = viewModelFactory

    override fun getLayoutRes() = R.layout.fragment_display_all_users

    override fun getViewModelClass(): Class<DisplayUsersViewModel> = DisplayUsersViewModel::class.java

    fun nonSuchSetDate() {
        Snackbar.make(activity.findViewById(android.R.id.content), context.getString(R.string.dates_has_been_incorrect), Snackbar.LENGTH_LONG).show()
    }

    override fun onStop() {
        super.onStop()
        (activity as MenuActivity).toolbar.findViewById<EditText>(R.id.searchUsers).visibility = View.GONE
    }

    private fun installLogicToUI() {
        (activity as MenuActivity).toolbar.findViewById<EditText>(R.id.searchUsers).setOnEditorActionListener { textView, _, _ ->
            sortString = textView.text.toString()
            analytics.logEvent(TAG_ANALYTICS + "SEARCH_QUERY", null)
            updateViewsBeforeSearch()
            viewModel?.getAllUsers(filter, sortString)
            true
        }

        startBudget.filters = arrayOf(DecimalInputFilter())
        endBudget.filters = arrayOf(DecimalInputFilter())

        startAge.adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,
                viewModel?.yearsOldUsers)
        startAge.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (countInitialElements++ < SIZE_INITIAL_ELEMENTS)
                    startAge.setSelection(filter.startAge)
                else
                    filter.startAge = position
            }
        }

        endAge.adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,
                viewModel?.yearsOldUsers)
        endAge.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (countInitialElements++ < SIZE_INITIAL_ELEMENTS)
                    endAge.setSelection(filter.endAge)
                else
                    filter.endAge = position
            }
        }

        sexButtons.setOnCheckedChangeListener { _, id ->
            filter.sex = when (id) {
                sexM.id -> M_SEX
                sexW.id -> W_SEX
                else -> 0
            }
        }
        sexButtons.check(when (filter.sex) {
            M_SEX -> sexM.id
            W_SEX -> sexW.id
            else -> sexAny.id
        })
        updateChoseDateButtons()
        choseDateStart.setOnClickListener {
            openDateFromDialog(choseDateStart)
        }
        choseDateStart.setOnTouchListener(DateUtils.onDateTouch)

        choseDateEnd.setOnClickListener {
            openDateArrivedDialog(choseDateEnd)
        }
        choseDateEnd.setOnTouchListener(DateUtils.onDateTouch)
        view_contain_block_parameters.layoutTransition.setDuration(700L)
        saveParameters.setOnClickListener {
            analytics.logEvent(TAG_ANALYTICS + "CLICK_ON_SEARCH", null)

            try {
                filter.startBudget = if (startBudget.text.isNotEmpty()) Integer.parseInt(startBudget.text.toString()) else -1
                filter.endBudget = if (endBudget.text.isNotEmpty()) Integer.parseInt(endBudget.text.toString()) else -1
            } catch (e: Exception) {
                e.printStackTrace()
                filter.endBudget = -1
                filter.startBudget = 0

            }

            filter.startCity = startCity.text.toString()
            filter.endCity = endCity.text.toString()

            animationSlide()
            block_search_parameters.visibility = View.GONE
            setLogEventForSearch()
        }

        cancelParameters.setOnClickListener {
            analytics.logEvent(TAG_ANALYTICS + "CLICK_ON_CANCEL", null)

            filter.sex = 0
            filter.startAge = -1
            filter.endAge = viewModel?.yearsOldUsers?.size?.minus(1) ?: -1
            filter.startCity = ""
            filter.endCity = ""
            filter.startBudget = -1
            filter.endBudget = -1
            filter.startDate = 0L
            filter.endDate = 0L

            startAge.setSelection(filter.startAge)
            endAge.setSelection(filter.endAge)
            startBudget.setText("")
            endBudget.setText("")
            startCity.setText("")
            endCity.setText("")
            choseDateStart.setCompoundDrawables(null, null, null, null)
            choseDateEnd.setCompoundDrawables(null, null, null, null)
            updateChoseDateButtons()
            sexButtons.check(sexAny.id)
        }

        searchParametersText.setOnClickListener {
            if (block_search_parameters.visibility == View.GONE) {
                block_search_parameters.visibility = View.VISIBLE
            } else {
                block_search_parameters.visibility = View.GONE
            }
        }
    }

    private fun openDateArrivedDialog(view: TextView) {
        if (view.text.contains("  ")) {
            view.text = getString(R.string.filters_all_users_empty_date)
            filter.endDate = 0L
            view.setCompoundDrawables(null, null, null, null)
            return
        }
        val dateTo = Calendar.getInstance() //current time by default
        if (filter.endDate > 0L) dateTo.timeInMillis = filter.endDate
        dateDialog = CalendarDatePickerDialogFragment()
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setThemeCustom(R.style.BythewayDatePickerDialogTheme)
                .setPreselectedDate(dateTo.get(Calendar.YEAR), dateTo.get(Calendar.MONTH), dateTo.get(Calendar.DAY_OF_MONTH))

        dateDialog.setDateRange(MonthAdapter.CalendarDay(if (filter.startDate == 0L) System.currentTimeMillis() else filter.startDate), null)
        dateDialog.setOnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            view.text = StringBuilder(" ")
                    .append(dayOfMonth)
                    .append(" ")
                    .append(context.resources.getStringArray(R.array.months_array)[monthOfYear])
                    .toString()
            filter.endDate = DateUtils.getLongFromDate(year = year, month = monthOfYear, day = dayOfMonth)
            view.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_vector_black, 0)
        }
        dateDialog.show(activity.supportFragmentManager, "")
    }

    private fun openDateFromDialog(view: TextView) {
        if (view.text.contains("  ")) {
            view.text = getString(R.string.filters_all_users_empty_date)
            filter.startDate = 0L
            view.setCompoundDrawables(null, null, null, null)
            return
        }
        val dateFrom = Calendar.getInstance() //current time by default
        if (filter.startDate > 0L) dateFrom.timeInMillis = filter.startDate

        dateDialog = CalendarDatePickerDialogFragment()
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setThemeCustom(R.style.BythewayDatePickerDialogTheme)
                .setPreselectedDate(dateFrom.get(Calendar.YEAR), dateFrom.get(Calendar.MONTH), dateFrom.get(Calendar.DAY_OF_MONTH))

        dateDialog.setDateRange(MonthAdapter.CalendarDay(System.currentTimeMillis()), null)
        dateDialog.setOnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            view.text = StringBuilder(" ")
                    .append(dayOfMonth)
                    .append(" ")
                    .append(context.resources.getStringArray(R.array.months_array)[monthOfYear])
                    .toString()
            filter.startDate = DateUtils.getLongFromDate(year = year, month = monthOfYear, day = dayOfMonth)
            view.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_vector_black, 0)
        }
        dateDialog.show(activity.supportFragmentManager, "")
    }


    private fun setLogEventForSearch() {
        if (filter.endDate != 0L) analytics.logEvent(TAG_ANALYTICS + "END_DATE", null)
        if (filter.startDate != 0L) analytics.logEvent(TAG_ANALYTICS + "START_DATE", null)
        if (filter.endAge != -1) analytics.logEvent(TAG_ANALYTICS + "END_AGE", null)
        if (filter.startAge != -1) analytics.logEvent(TAG_ANALYTICS + "START_AGE", null)
        if (filter.endCity.isNotBlank()) analytics.logEvent(TAG_ANALYTICS + "END_CITY", null)
        if (filter.startCity.isNotBlank()) analytics.logEvent(TAG_ANALYTICS + "START_CITY", null)
        if (filter.endBudget != -1) analytics.logEvent(TAG_ANALYTICS + "START_BUDGET", null)
        if (filter.startBudget != -1) analytics.logEvent(TAG_ANALYTICS + "END_BUDGET", null)
        if (filter.sex != 0) analytics.logEvent(TAG_ANALYTICS + "SEX_ANY", null)
        if (filter.sex != W_SEX) analytics.logEvent(TAG_ANALYTICS + "SEX_FEMALE", null)
        if (filter.sex != M_SEX) analytics.logEvent(TAG_ANALYTICS + "SEX_MALE", null)
    }

    private fun animationSlide() {
        view_contain_block_parameters.layoutTransition.addTransitionListener(object : LayoutTransition.TransitionListener {
            override fun startTransition(p0: LayoutTransition?, p1: ViewGroup?, p2: View?, p3: Int) {}
            override fun endTransition(p0: LayoutTransition?, p1: ViewGroup?, view: View, p3: Int) {
                if ((view.id == block_search_parameters.id) && (block_search_parameters.visibility == View.GONE)) {
                    updateViewsBeforeSearch()
                    viewModel?.getAllUsers(filter, sortString)
                    view_contain_block_parameters.layoutTransition.removeTransitionListener(this)
                }
            }
        })
    }

    private fun updateViewsBeforeSearch() {
        displayAllUsers.visibility = View.GONE
        block_empty_users.visibility = View.GONE
        loadingWhereLoadUsers.visibility = View.VISIBLE
    }

    private fun updateViewsAfterSearch(isNotEmptyListUsers: Boolean) {
        loadingWhereLoadUsers.visibility = View.GONE
        if (isNotEmptyListUsers)
            displayAllUsers.visibility = View.VISIBLE
        else
            block_empty_users.visibility = View.VISIBLE
    }

    private fun updateChoseDateButtons() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = filter.startDate
        choseDateStart.text = if (filter.startDate > 0L)
            viewModel?.getTextFromDates(filter.startDate, context.resources.getStringArray(R.array.months_array)) else getString(R.string.filters_all_users_empty_date)
        if (filter.startDate > 0L)
            choseDateStart.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_vector_black, 0)
        calendar.timeInMillis = filter.endDate
        choseDateEnd.text = if (filter.endDate > 0L)
            viewModel?.getTextFromDates(filter.endDate, context.resources.getStringArray(R.array.months_array)) else getString(R.string.filters_all_users_empty_date)
        if (filter.endDate > 0L)
            choseDateEnd.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_vector_black, 0)
    }

    private fun showErrorLoading() {
    }
}