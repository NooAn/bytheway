package ru.a1024bits.bytheway.ui.fragments

import android.animation.LayoutTransition
import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crash.FirebaseCrash
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
import ru.a1024bits.bytheway.util.DecimalInputFilter
import ru.a1024bits.bytheway.viewmodel.DisplayUsersViewModel
import ru.a1024bits.bytheway.viewmodel.OnUpdateDialog
import javax.inject.Inject


class AllUsersFragment : BaseFragment<DisplayUsersViewModel>(), OnUpdateDialog {
    private val SIZE_INITIAL_ELEMENTS = 2
    private val TAG_ANALYTICS = "AllUsersFragment_"
    private lateinit var filter: Filter
    private var dateDialog: DatePickerDialog? = null
    private lateinit var displayUsersAdapter: DisplayAllUsersAdapter
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private var countInitialElements = 0
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        analytics = FirebaseAnalytics.getInstance(this.context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_display_all_users, container, false)

    private val usersObservers: Observer<Response<List<User>>> = Observer<Response<List<User>>> { response ->
        when (response?.status) {
            Status.SUCCESS -> if (response.data == null) showErrorLoading() else {
                if (response.data.isNotEmpty())
                    displayUsersAdapter.setItems(response.data)
                updateViewsAfterSearch(response.data.isNotEmpty())
            }
            Status.ERROR -> {
                Log.e("LOG", "log e:" + response.error)
                showErrorLoading()
            }
        }

    }

    private fun showErrorLoading() {

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        App.component.inject(this)
        super.onActivityCreated(savedInstanceState)
        analytics.setCurrentScreen(this.activity, "AllUsersFragment", this.javaClass.simpleName)

        try {
            filter = viewModel?.filter ?: Filter()

            installLogicToUI()

            displayUsersAdapter = DisplayAllUsersAdapter(this.context, viewModel!!)
            displayAllUsers.adapter = displayUsersAdapter

            viewModel?.response?.observe(this, usersObservers)
            viewModel?.loadingStatus?.observe(this, (activity as MenuActivity).progressBarLoad)
            loadingWhereLoadUsers.visibility = View.VISIBLE
            viewModel?.getAllUsers()

            showPrompt("isFirstEnterAllUsersFragment", context.resources.getString(R.string.close_hint),
                    context.resources.getString(R.string.hint_all_travelers), context.resources.getString(R.string.hint_all_travelers_description))
        } catch (e: Throwable) {
            Log.e("LOG_AUF", e.toString())
            FirebaseCrash.report(e)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.all_users_menu, menu)

        val searchView = menu.findItem(R.id.search_all_users_item).actionView as SearchView
        searchView.setSearchableInfo(
                (context.getSystemService(Context.SEARCH_SERVICE) as SearchManager).getSearchableInfo(activity.componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            var isNotStartSearch = false
            override fun onQueryTextSubmit(query: String): Boolean {
                displayUsersAdapter.setItems(viewModel?.filterUsersByString(query.toLowerCase(), query, displayUsersAdapter.users))
                analytics.logEvent(TAG_ANALYTICS + "SEARCH_QUERY", null)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty() && isNotStartSearch && this@AllUsersFragment.view != null) {
                    updateViewsBeforeSearch()
                    viewModel?.getAllUsers()
                }
                isNotStartSearch = true
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.search_all_users_item -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun getViewFactoryClass() = viewModelFactory

    override fun getLayoutRes() = R.layout.fragment_display_all_users

    override fun getViewModelClass(): Class<DisplayUsersViewModel> = DisplayUsersViewModel::class.java

    override fun notSuchSetDate() {
        Snackbar.make(activity.findViewById(android.R.id.content), context.getString(R.string.dates_has_been_incorrect), Snackbar.LENGTH_LONG).show()
    }

    override fun onSuchSetDate() {
        updateChoseDateButtons()
    }

    private fun installLogicToUI() {
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
        dateDialog = viewModel?.updateDateDialog(this)
        choseDate.setOnClickListener {
            dateDialog?.show(activity.fragmentManager, "")
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

        view_contain_block_parameters.layoutTransition.setDuration(700L)
        saveParameters.setOnClickListener {
            analytics.logEvent(TAG_ANALYTICS + "CLICK_ON_SEARCH", null)

            filter.startBudget = if (startBudget.text.isNotEmpty()) Integer.parseInt(startBudget.text.toString()) else -1
            filter.endBudget = if (endBudget.text.isNotEmpty()) Integer.parseInt(endBudget.text.toString()) else -1
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

            dateDialog = viewModel?.updateDateDialog(this)
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
                    viewModel?.getAllUsers()
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
        choseDate.text = if (filter.startDate > 0L && filter.endDate > 0L)
            viewModel?.getTextFromDates(filter.startDate, filter.endDate, 0)
        else context.getString(R.string.filters_all_users_empty_date)
    }

    companion object {
        fun newInstance(): AllUsersFragment = AllUsersFragment()
    }
}