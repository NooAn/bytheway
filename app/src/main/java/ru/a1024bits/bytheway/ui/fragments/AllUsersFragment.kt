package ru.a1024bits.bytheway.ui.fragments

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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.fragment_display_all_users.*
import kotlinx.android.synthetic.main.searching_parameters_block.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.ExtensionsAllUsers
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.ShowAllUsersAdapter
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.viewmodel.ShowUsersViewModel
import java.util.*
import javax.inject.Inject


class AllUsersFragment : Fragment() {
    private val SIZE_INITIAL_ELEMENTS = 2
    private lateinit var filter: Filter
    private lateinit var viewModel: ShowUsersViewModel
    private lateinit var extension: ExtensionsAllUsers
    private lateinit var dateDialog: DatePickerDialog
    private lateinit var showUsersAdapter: ShowAllUsersAdapter
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

        saveParameters.setOnClickListener {
            if (startBudget.text.isNotEmpty())
                filter.startBudget = Integer.parseInt(startBudget.text.toString())
            if (endBudget.text.isNotEmpty())
                filter.endBudget = Integer.parseInt(endBudget.text.toString())
            filter.startCity = startCity.text.toString()
            filter.endCity = endCity.text.toString()

            loading_where_load_users.visibility = View.VISIBLE
            viewModel.getAllUsers(filter)
        }

        cancelParameters.setOnClickListener {
            filter.sex = 0
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
                showUsersAdapter.users.filterTo(result) {
                    it.cities.contains(query) || it.name.toLowerCase().contains(query) || it.email.toLowerCase().contains(query) ||
                            it.age.toString().contains(query) || it.budget.toString().contains(query) ||
                            it.city.toLowerCase().contains(query) || it.lastName.toLowerCase().contains(query) ||
                            it.phone.contains(query) || it.route.contains(query)
                }
                showUsersAdapter.setItems(result)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if ("" == newText) {
                    loading_where_load_users.visibility = View.VISIBLE
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
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ShowUsersViewModel::class.java)
        showUsersAdapter = ShowAllUsersAdapter(this.context)
        display_all_users.adapter = showUsersAdapter
//        FirebaseCrash.log(FirebaseAuth.getInstance().currentUser?.email + " purchased product ")

        viewModel.usersLiveData.observe(this, Observer<List<User>> { list ->
            Log.e("LOG", "onChanged $list")
            if (list != null) {
                Log.e("LOG", "update $list")
                showUsersAdapter.setItems(list)
                loading_where_load_users.visibility = View.GONE
            }
        })
        loading_where_load_users.visibility = View.VISIBLE
        viewModel.getAllUsers(filter)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (startBudget.text.isNotEmpty())
            filter.startBudget = Integer.parseInt(startBudget.text.toString())
        if (endBudget.text.isNotEmpty())
            filter.endBudget = Integer.parseInt(endBudget.text.toString())
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
        if (filter.startDate > 0L && filter.endDate > 0L) {
            val calendarStartDate = Calendar.getInstance()
            calendarStartDate.timeInMillis = filter.startDate
        val calendarEndDate = Calendar.getInstance()
            calendarEndDate.timeInMillis = filter.endDate
            //todo add years, if necessary
            choseDate.text = ("c: " + calendarStartDate.get(Calendar.DAY_OF_MONTH) + " " + context.resources.getStringArray(R.array.months_array)[calendarStartDate.get(Calendar.MONTH)]
                    + " по: " + calendarEndDate.get(Calendar.DAY_OF_MONTH) + " " + context.resources.getStringArray(R.array.months_array)[calendarEndDate.get(Calendar.MONTH)])
        } else
            choseDate.text = "выберите даты"
    }

    companion object {
        fun newInstance(): AllUsersFragment = AllUsersFragment()
    }
}