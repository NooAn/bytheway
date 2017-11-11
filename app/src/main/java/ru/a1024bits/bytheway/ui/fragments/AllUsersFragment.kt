package ru.a1024bits.bytheway.ui.fragments

import android.app.DatePickerDialog
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
import kotlinx.android.synthetic.main.fragment_display_all_users.*
import kotlinx.android.synthetic.main.searching_parameters_block.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.ShowAllUsersAdapter
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.viewmodel.ShowUsersViewModel
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class AllUsersFragment : Fragment() {
    private lateinit var filter: Filter
    private var calendarStartDate: Calendar = Calendar.getInstance()
    private var calendarEndDate: Calendar = Calendar.getInstance()
    private lateinit var viewModel: ShowUsersViewModel
    private lateinit var showUsersAdapter: ShowAllUsersAdapter
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (savedInstanceState != null) {
            filter = savedInstanceState.getSerializable("filter") as Filter
            if (filter.startDate > 0) calendarStartDate.time = Date(filter.startDate)
            if (filter.endDate > 0) calendarEndDate.time = Date(filter.endDate)
        } else {
            filter = Filter()
            calendarEndDate.set(Calendar.MONTH, calendarEndDate.get(Calendar.MONTH) + 3)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_display_all_users, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        start_city_parameter.setText(filter.startCity)
        end_city_parameter.setText(filter.endCity)
        if (filter.endAge > 0)
            end_age_parameter.setText(filter.endAge.toString(10))
        if (filter.startAge > 0)
            start_age_parameter.setText(filter.startAge.toString(10))
        if (filter.startBudget > 0)
            start_budget_parameter.setText(filter.startBudget.toString(10))
        if (filter.endBudget > 0)
            end_budget_parameter.setText(filter.endBudget.toString(10))
        choose_sex_parameter.text = when {
            filter.sex == 1 -> context.getString(R.string.man)
            filter.sex == 2 -> context.getString(R.string.woman)
            else -> "choose sex"
        }

        start_date_parameter.setOnClickListener {
            DatePickerDialog(activity, DatePickerDialog.OnDateSetListener { dialog, year, month, day ->
                calendarStartDate.set(year, month, day)
                filter.startDate = calendarStartDate.timeInMillis
                dialog.updateDate(year, month, day)
            },
                    calendarStartDate.get(Calendar.YEAR),
                    calendarStartDate.get(Calendar.MONTH),
                    calendarStartDate.get(Calendar.DAY_OF_MONTH))
                    .show()
        }

        end_date_parameter.setOnClickListener {
            DatePickerDialog(activity, DatePickerDialog.OnDateSetListener { dialog, year, month, day ->
                calendarEndDate.set(year, month, day)
                filter.endDate = calendarEndDate.timeInMillis
                dialog.updateDate(year, month, day)
            },
                    calendarEndDate.get(Calendar.YEAR),
                    calendarEndDate.get(Calendar.MONTH),
                    calendarEndDate.get(Calendar.DAY_OF_MONTH))
                    .show()
        }

        save_parameters.setOnClickListener {
            if (start_age_parameter.text.toString().isNotEmpty())
                filter.startAge = Integer.parseInt(start_age_parameter.text.toString())
            else filter.startAge = 0
            if (end_age_parameter.text.toString().isNotEmpty())
                filter.endAge = Integer.parseInt(end_age_parameter.text.toString())
            else filter.endAge = 0
            if (start_budget_parameter.text.toString().isNotEmpty())
                filter.startBudget = Integer.parseInt(start_budget_parameter.text.toString())
            else filter.startBudget = 0
            if (end_budget_parameter.text.toString().isNotEmpty())
                filter.endBudget = Integer.parseInt(end_budget_parameter.text.toString())
            else filter.endBudget = 0

            filter.startCity = start_city_parameter.text.toString()
            filter.endCity = end_city_parameter.text.toString()

            loading_where_load_users.visibility = View.VISIBLE
            viewModel.getAllUsers(filter)
        }

        parameters_search_text.setOnClickListener {
            if (block_search_parameters.visibility == View.GONE) {
                block_search_parameters.visibility = View.VISIBLE
                choose_sex_parameter.setOnClickListener {
                    if (choose_sex_parameter.text == context.getString(R.string.man)) {
                        filter.sex = 2
                        choose_sex_parameter.text = context.getString(R.string.woman)
                    } else {
                        filter.sex = 1
                        choose_sex_parameter.text = context.getString(R.string.man)
                    }
                }
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
                if ("".equals(newText)) {
                    loading_where_load_users.visibility = View.VISIBLE
                    viewModel.getAllUsers(filter)
                }
                return false
            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_all_users_item -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
    }

    companion object {
        fun newInstance(): AllUsersFragment {
            val fragment = AllUsersFragment()
            return fragment
        }
    }
}
