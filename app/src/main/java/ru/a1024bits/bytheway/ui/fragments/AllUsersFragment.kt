package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.ShowAllUsersAdapter
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.viewmodel.ShowUsersViewModel
import javax.inject.Inject
import android.support.v4.view.MenuItemCompat.getActionView
import android.content.Context.SEARCH_SERVICE
import android.app.SearchManager
import android.content.Context
import android.widget.Toast


class AllUsersFragment : Fragment() {
    private lateinit var currentView: View
    private lateinit var viewModel: ShowUsersViewModel
    private lateinit var showUsersAdapter: ShowAllUsersAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewContainFilters: View
    private var isDisplayFilters = true
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_display_all_users1, container, false)
        return currentView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = currentView.findViewById(R.id.display_all_users)
        viewContainFilters = LayoutInflater.from(context).inflate(R.layout.searching_parameters, null)
        //in onClick on batterKnife
        currentView.findViewById<View>(R.id.parameters_search_text).setOnClickListener {
            val search_parameters = currentView.findViewById<LinearLayout>(R.id.search_parameters)
            if (isDisplayFilters) search_parameters.addView(viewContainFilters)
            else search_parameters.removeView(viewContainFilters)

            isDisplayFilters = !isDisplayFilters
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.all_users_menu, menu)

        val searchView = menu.findItem(R.id.search_all_users_item).actionView as SearchView
        searchView.setSearchableInfo(
                (context.getSystemService(Context.SEARCH_SERVICE) as SearchManager).getSearchableInfo(activity.componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(context, query, Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
//                adapter.getFilter().filter(newText);
                Log.d("tag"," search:: " + newText)
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
        recyclerView.adapter = showUsersAdapter

        viewModel.userLiveData.observe(this, Observer<User> { list ->
            Log.e("LOG", "onChanged $list")
            if (list != null) {
                Log.e("LOG", "update $list")
                showUsersAdapter.addItem(list)
            }
        })
        viewModel.getAllUsers()
    }

    companion object {
        fun newInstance(): AllUsersFragment {
            val fragment = AllUsersFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }
}
