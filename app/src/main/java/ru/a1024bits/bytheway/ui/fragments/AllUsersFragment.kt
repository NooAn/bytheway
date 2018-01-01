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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.fragment_display_all_users.*
import kotlinx.android.synthetic.main.searching_parameters_block.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.DisplayAllUsersAdapter
import ru.a1024bits.bytheway.extensions.ExtensionsAllUsers
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.repository.M_SEX
import ru.a1024bits.bytheway.repository.W_SEX
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.util.DecimalInputFilter
import ru.a1024bits.bytheway.viewmodel.DisplayUsersViewModel
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import javax.inject.Inject


class AllUsersFragment : Fragment() {
    private val SIZE_INITIAL_ELEMENTS = 2
    private lateinit var filter: Filter
    private lateinit var extension: ExtensionsAllUsers
    private lateinit var viewModel: DisplayUsersViewModel
    private lateinit var dateDialog: DatePickerDialog
    private lateinit var displayUsersAdapter: DisplayAllUsersAdapter
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private var countInitialElements = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
//        System.gc()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_display_all_users, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.component.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DisplayUsersViewModel::class.java)
        filter = viewModel.filter
        extension = viewModel.extension

        installLogicToUI()

        displayUsersAdapter = DisplayAllUsersAdapter(this.context, extension)
        displayAllUsers.adapter = displayUsersAdapter

        viewModel.usersLiveData.observe(this, Observer<List<User>> { list ->
            Log.e("LOG", "onChanged $list")
            if (list != null) {
                if (list.isNotEmpty())
                    displayUsersAdapter.setItems(list)
                updateViewsAfterSearch(list.isNotEmpty())
            }
        })
        loadingWhereLoadUsers.visibility = View.VISIBLE
        viewModel.getAllUsers(filter)
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
                displayUsersAdapter.setItems(extension.filterUsersInAdapterByString(query.toLowerCase(), query, displayUsersAdapter.users))
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty() && isNotStartSearch && this@AllUsersFragment.view != null) {
                    updateViewsBeforeSearch()
                    viewModel.getAllUsers(filter)
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

    fun nonSuchSetDate() {
        Snackbar.make(activity.findViewById(android.R.id.content), context.getString(R.string.dates_has_been_incorrect), Snackbar.LENGTH_LONG).show()
    }

    fun suchSetDate() {
        updateChoseDateButtons()
    }

    private fun installLogicToUI() {
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
        dateDialog = viewModel.updateDateDialog(this)
//        updateDateDialog()
        choseDate.setOnClickListener {
            dateDialog.show(activity.fragmentManager, "")
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
            filter.startBudget = if (startBudget.text.isNotEmpty()) Integer.parseInt(startBudget.text.toString()) else -1
            filter.endBudget = if (endBudget.text.isNotEmpty()) Integer.parseInt(endBudget.text.toString()) else -1
            filter.startCity = startCity.text.toString()
            filter.endCity = endCity.text.toString()

            animationSlide()
            block_search_parameters.visibility = View.GONE
            viewModel.getAllUsers(filter)
        }

        cancelParameters.setOnClickListener {
            filter.sex = 0
            filter.startAge = -1
            filter.endAge = -1
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

            dateDialog = viewModel.updateDateDialog(this)
            updateChoseDateButtons()

            sexButtons.check(sexAny.id)
        }

        view_contain_block_parameters.layoutTransition.setDuration(700L)
        searchParametersText.setOnClickListener {
            if (block_search_parameters.visibility == View.GONE) {
                block_search_parameters.visibility = View.VISIBLE
            } else {
                block_search_parameters.visibility = View.GONE
            }
        }

        if ((activity as MenuActivity).preferences.getBoolean("isFirstEnterAllUsersFragment", true))
            MaterialShowcaseView.Builder(activity)
                    .setTarget(searchParametersText)
                    .renderOverNavigationBar()
                    .setDismissText(context.resources.getString(R.string.close_hint))
                    .setTitleText(context.resources.getString(R.string.hint_all_travelers))
                    .setContentText(context.resources.getString(R.string.hint_all_travelers_description))
                    .withCircleShape()
                    .setListener(object : IShowcaseListener {
                        override fun onShowcaseDisplayed(p0: MaterialShowcaseView?) {
                        }

                        override fun onShowcaseDismissed(p0: MaterialShowcaseView?) {
                            if (activity != null && !activity.isDestroyed)
                                (activity as MenuActivity).preferences.edit().putBoolean("isFirstEnterAllUsersFragment", false).apply()
                        }
                    })
                    .show()
    }

    private fun animationSlide() {
        view_contain_block_parameters.layoutTransition.addTransitionListener(object : LayoutTransition.TransitionListener {
            override fun startTransition(p0: LayoutTransition?, p1: ViewGroup?, p2: View?, p3: Int) {}
            override fun endTransition(p0: LayoutTransition?, p1: ViewGroup?, view: View, p3: Int) {
                if ((view.id == block_search_parameters.id) && (block_search_parameters.visibility == View.GONE)) {
                    updateViewsBeforeSearch()
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
            extension.getTextFromDates(filter.startDate, filter.endDate, 0)
        else context.getString(R.string.filters_all_users_empty_date)
    }

    companion object {
        fun newInstance(): AllUsersFragment = AllUsersFragment()
    }
}