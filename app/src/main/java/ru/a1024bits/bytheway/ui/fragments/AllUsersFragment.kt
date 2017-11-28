package ru.a1024bits.bytheway.ui.fragments

import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_display_all_users.*
import kotlinx.android.synthetic.main.searching_parameters_block.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.ExtensionsAllUsers
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.ShowAllUsersAdapter
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.viewmodel.ShowUsersViewModel
import javax.inject.Inject
import kotlin.collections.ArrayList


class AllUsersFragment : Fragment() {
    private val SIZE_INITIAL_ELEMENTS = 4
    private lateinit var filter: Filter
    private lateinit var viewModel: ShowUsersViewModel
    private lateinit var extension: ExtensionsAllUsers
    private lateinit var showUsersAdapter: ShowAllUsersAdapter
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private var countInitialElements = 0
    private var tempStartAge: Int = -1
    private var tempEndAge: Int = -1
    private var tempStartDate: Long = 0L
    private var tempEndDate: Long = 0L
    private var tempSex: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        extension = ExtensionsAllUsers(context)
        filter = if (savedInstanceState != null) {
            tempStartAge = savedInstanceState.getInt("tempStartAge")
            tempSex = savedInstanceState.getInt("tempSex")
            tempEndAge = savedInstanceState.getInt("tempEndAge")
            tempStartDate = savedInstanceState.getLong("tempStartDate")
            tempEndDate = savedInstanceState.getLong("tempEndDate")
            savedInstanceState.getSerializable("filter") as Filter
        } else Filter()


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
                if (countInitialElements++ < SIZE_INITIAL_ELEMENTS && tempStartAge >= 0) {
                    startAge.setSelection(tempStartAge)
                    return
                }
                tempStartAge = position
            }
        }

        endAge.adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,
                extension.yearsOldUsers)
        endAge.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (countInitialElements++ < SIZE_INITIAL_ELEMENTS) {
                    val settingAge = if (tempEndAge >= 0) tempEndAge else extension.yearsOldUsers.size - 1
                    endAge.setSelection(settingAge)
                    tempEndAge = settingAge
                    return
                }
                tempEndAge = position
            }
        }

        startDate.adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,
                extension.getDaysInEndDataStr())
        startDate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, p3: Long) {
                if (countInitialElements++ < SIZE_INITIAL_ELEMENTS) {
                    val settingDate = extension.getPositionFromDate(tempStartDate, false)
                    tempStartDate = extension.daysInMonths[settingDate]
                    startDate.setSelection(settingDate)
                    return
                }
                tempStartDate = extension.daysInMonths[position]
            }
        }
        endDate.adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,
                extension.getDaysInEndDataStr())
        endDate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (countInitialElements++ < SIZE_INITIAL_ELEMENTS) {
                    val settingDate = extension.getPositionFromDate(tempEndDate, true)
                    tempEndDate = extension.daysInMonths[settingDate]
                    endDate.setSelection(settingDate)
                    return
                }
                tempEndDate = extension.daysInMonths[position]
            }
        }

        if (filter.startBudget > 0)
            startBudget.setText(filter.startBudget.toString())
        if (filter.endBudget > 0)
            endBudget.setText(filter.endBudget.toString())

        sexButtons.setOnCheckedChangeListener { _, id ->
            tempSex = when (id) {
                sex_M.id -> 1
                sex_W.id -> 2
                else -> 0
            }
        }
        sexButtons.check(when (tempSex) {
            1 -> sex_M.id
            2 -> sex_W.id
            else -> sex_Any.id
        })

        saveParameters.setOnClickListener {
            filter.startAge = tempStartAge
            filter.endAge = tempEndAge
            filter.startDate = tempStartDate
            filter.endDate = tempEndDate
            if (startBudget.text.toString().isNotEmpty())
                filter.startBudget = Integer.parseInt(startBudget.text.toString())
            else filter.startBudget = 0
            if (endBudget.text.toString().isNotEmpty())
                filter.endBudget = Integer.parseInt(endBudget.text.toString())
            else filter.endBudget = 0
            filter.sex = tempSex
            filter.startCity = startCity.text.toString()
            filter.endCity = endCity.text.toString()

            loading_where_load_users.visibility = View.VISIBLE
            viewModel.getAllUsers(filter)
        }

        cancelParameters.setOnClickListener {
            sexButtons.check(when (tempSex) {
                1 -> sex_M.id
                2 -> sex_W.id
                else -> sex_Any.id
            })
            startAge.setSelection(filter.startAge)
            endAge.setSelection(filter.endAge)
            startDate.setSelection(extension.getPositionFromDate(filter.startDate, false))
            endDate.setSelection(extension.getPositionFromDate(filter.endDate, true))

            if (filter.startBudget > 0)
                startBudget.setText(filter.startBudget.toString())
            if (filter.endBudget > 0)
                endBudget.setText(filter.endBudget.toString())

        }

        searchParametersText.setOnClickListener {

            val transOn= AnimationUtils.loadAnimation(context, R.anim.transition_on)

            val transOut= AnimationUtils.loadAnimation(context, R.anim.transition_out)
            if (block_search_parameters.visibility == View.GONE) {
                block_search_parameters.startAnimation(transOn)
                block_search_parameters.visibility = View.VISIBLE

            } else {
                block_search_parameters.startAnimation(transOut)
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
                if ("".equals(newText)) {
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
        outState.putSerializable("filter", filter)
        outState.putInt("tempStartAge", tempStartAge)
        outState.putInt("tempEndAge", tempEndAge)
        outState.putLong("tempStartDate", tempStartDate)
        outState.putLong("tempEndDate", tempEndDate)
        outState.putInt("tempSex", tempSex)
    }

    companion object {
        fun newInstance(): AllUsersFragment = AllUsersFragment()
    }
}