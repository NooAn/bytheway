package ru.a1024bits.bytheway.ui.fragments

import android.animation.LayoutTransition
import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.fragment_display_all_users.*
import kotlinx.android.synthetic.main.searching_parameters_block.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.ExtensionsAllUsers
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.DisplayAllUsersAdapter
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.util.DecimalInputFilter
import ru.a1024bits.bytheway.viewmodel.DisplayUsersViewModel
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class AllUsersFragment : Fragment() {
    private val SIZE_INITIAL_ELEMENTS = 2
    private lateinit var filter: Filter
    private lateinit var viewModel: DisplayUsersViewModel
    private lateinit var extension: ExtensionsAllUsers
    private lateinit var dateDialog: DatePickerDialog
    private lateinit var displayUsersAdapter: DisplayAllUsersAdapter
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private var countInitialElements = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        extension = ExtensionsAllUsers(context)

        filter = if (savedInstanceState != null) {
            savedInstanceState.getSerializable("filter") as Filter

        } else {
            val result = Filter()
            result.endAge = extension.yearsOldUsers.size - 1
            result
        }
        updateDateDialog()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_display_all_users, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startCity.setText(filter.startCity)
        endCity.setText(filter.endCity)

        startBudget.filters = arrayOf(DecimalInputFilter())
        endBudget.filters = arrayOf(DecimalInputFilter())

        startAge.adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,
                extension.yearsOldUsers)
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
                extension.yearsOldUsers)
        endAge.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (countInitialElements++ < SIZE_INITIAL_ELEMENTS)
                    endAge.setSelection(filter.endAge)
                else
                    filter.endAge = position
            }
        }
        choseDate.setOnClickListener {
            dateDialog.show(activity.fragmentManager, "")
        }
        sexButtons.setOnCheckedChangeListener { _, id ->
            filter.sex = when (id) {
                sex_M.id -> 1
                sex_W.id -> 2
                else -> 0
            }
        }
        sexButtons.check(when (filter.sex) {
            1 -> sex_M.id
            2 -> sex_W.id
            else -> sex_Any.id
        })
        updateChoseDateButtons()

        view_contain_block_parameters.layoutTransition.setDuration(700L)
        saveParameters.setOnClickListener {
            filter.startBudget = if (startBudget.text.isNotEmpty()) Integer.parseInt(startBudget.text.toString()) else -1
            filter.endBudget = if (endBudget.text.isNotEmpty()) Integer.parseInt(endBudget.text.toString()) else -1
            filter.startCity = startCity.text.toString()
            filter.endCity = endCity.text.toString()

            view_contain_block_parameters.layoutTransition.addTransitionListener(object : LayoutTransition.TransitionListener {
                override fun startTransition(p0: LayoutTransition?, p1: ViewGroup?, p2: View?, p3: Int) {}
                override fun endTransition(p0: LayoutTransition?, p1: ViewGroup?, view: View, p3: Int) {
                    if ((view.id == block_search_parameters.id) && (block_search_parameters.visibility == View.GONE)) {
                        view_contain_block_parameters.layoutTransition.setDuration(0L)
                        display_all_users.visibility = View.GONE
                        loadingWhereLoadUsers.visibility = View.VISIBLE
                        view_contain_block_parameters.layoutTransition.setDuration(700L)
                        view_contain_block_parameters.layoutTransition.removeTransitionListener(this)
                    }
                }
            })
            block_search_parameters.visibility = View.GONE
            viewModel.getAllUsers(filter)
        }

        cancelParameters.setOnClickListener {
            filter.sex = 0
            filter.startAge = -1
            filter.endAge = - 1
            filter.startAge = 0
            filter.endAge = extension.yearsOldUsers.size - 1
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

            updateDateDialog()
            updateChoseDateButtons()

            sexButtons.check(when (filter.sex) {
                1 -> sex_M.id
                2 -> sex_W.id
                else -> sex_Any.id
            })
        }

        view_contain_block_parameters.layoutTransition.setDuration(700L)
        searchParametersText.setOnClickListener {
            if (block_search_parameters.visibility == View.GONE) {
                block_search_parameters.visibility = View.VISIBLE
            } else {
                block_search_parameters.visibility = View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.all_users_menu, menu)
        



        val searchView = menu.findItem(R.id.search_all_users_item).actionView as SearchView
        searchView.setSearchableInfo(
                (context.getSystemService(Context.SEARCH_SERVICE) as SearchManager).getSearchableInfo(activity.componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(quer: String): Boolean {
                val query = quer.toLowerCase()
                val result = ArrayList<User>()
                displayUsersAdapter.users.filterTo(result) {
                    it.cities.contains(query) || it.name.toLowerCase().contains(query) || it.email.toLowerCase().contains(query) ||
                            it.age.toString().contains(query) || it.budget.toString().contains(query) ||
                            it.city.toLowerCase().contains(query) || it.lastName.toLowerCase().contains(query) ||
                            it.phone.contains(query) || it.route.contains(query)
                }
                displayUsersAdapter.setItems(result)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if ("" == newText) {
                    display_all_users.visibility = View.GONE
                    loadingWhereLoadUsers.visibility = View.VISIBLE
                    viewModel.getAllUsers(filter)
                }
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


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.component.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DisplayUsersViewModel::class.java)
        displayUsersAdapter = DisplayAllUsersAdapter(this.context, extension)
        display_all_users.adapter = displayUsersAdapter

        viewModel.usersLiveData.observe(this, Observer<List<User>> { list ->
            Log.e("LOG", "onChanged $list")
            if (list != null) {
                Log.e("LOG", "update $list")
                display_all_users.adapter = displayUsersAdapter
                loadingWhereLoadUsers.visibility = View.GONE
                displayUsersAdapter.setItems(list)
                display_all_users.visibility = View.VISIBLE
            }
        })
        loadingWhereLoadUsers.visibility = View.VISIBLE
        viewModel.getAllUsers(filter)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        filter.startBudget = if (startBudget.text.isNotEmpty()) Integer.parseInt(startBudget.text.toString()) else -1
        filter.endBudget = if (endBudget.text.isNotEmpty()) Integer.parseInt(endBudget.text.toString()) else -1
        outState.putSerializable("filter", filter)
    }

    private fun updateDateDialog() {
        val currentStartDate = Calendar.getInstance()
        if (filter.startDate > 0L) currentStartDate.timeInMillis = filter.startDate
        val currentEndDate = Calendar.getInstance()
        currentEndDate.timeInMillis = currentEndDate.timeInMillis + 1000L * 60 * 60 * 24
        if (filter.endDate > 0L) currentEndDate.timeInMillis = filter.endDate

        dateDialog = DatePickerDialog.newInstance(
                { _, year, monthOfYear, dayOfMonth, yearEnd, monthOfYearEnd, dayOfMonthEnd ->
                    val calendarStartDate = Calendar.getInstance()
                    calendarStartDate.set(Calendar.YEAR, year)
                    calendarStartDate.set(Calendar.MONTH, monthOfYear)
                    calendarStartDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val calendarEndDate = Calendar.getInstance()
                    calendarEndDate.set(Calendar.YEAR, yearEnd)
                    calendarEndDate.set(Calendar.MONTH, monthOfYearEnd)
                    calendarEndDate.set(Calendar.DAY_OF_MONTH, dayOfMonthEnd)

                    if (calendarStartDate.timeInMillis >= calendarEndDate.timeInMillis) {
                        Snackbar.make(activity.findViewById(android.R.id.content), "даты оказались некорректными, попробуйте ввести их снова", Snackbar.LENGTH_LONG).show()
                        return@newInstance
                    }
                    filter.startDate = calendarStartDate.timeInMillis
                    filter.endDate = calendarEndDate.timeInMillis
                    updateChoseDateButtons()
                },
                currentStartDate.get(Calendar.YEAR),
                currentStartDate.get(Calendar.MONTH),
                currentStartDate.get(Calendar.DAY_OF_MONTH),
                currentEndDate.get(Calendar.YEAR),
                currentEndDate.get(Calendar.MONTH),
                currentEndDate.get(Calendar.DAY_OF_MONTH))
        dateDialog.setStartTitle("НАЧАЛО")
        dateDialog.setEndTitle("КОНЕЦ")
    }

    private fun updateChoseDateButtons() {
            choseDate.text = if (filter.startDate > 0L && filter.endDate > 0L)
                extension.getTextFromDates(filter.startDate, filter.endDate, 0)
        else context.getString(R.string.filters_all_users_empty_date)
    }

    companion object {
        fun newInstance(): AllUsersFragment = AllUsersFragment()
    }
}