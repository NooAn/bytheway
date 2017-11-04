package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.ShowAllUsersAdapter
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.viewmodel.ShowUsersViewModel
import javax.inject.Inject


class AllUsersFragment : Fragment() {
    private lateinit var currentView: View
    private lateinit var viewModel: ShowUsersViewModel
    private lateinit var showUsersAdapter: ShowAllUsersAdapter
    private lateinit var recyclerView: RecyclerView
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_show_users, container, false)
        return currentView
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = currentView.findViewById(R.id.lazy_display_users)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.component.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ShowUsersViewModel::class.java)
        showUsersAdapter = ShowAllUsersAdapter(recyclerView, this.context)
        recyclerView.adapter = showUsersAdapter
        if (savedInstanceState != null) {
            // recyclerView.layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(LIST_DISPLAYING_USERS))
            // showUsersAdapter.users = savedInstanceState.getParcelableArrayList(LIST_USERS_IN_ADAPTER)
        }

        viewModel.getAllUsers().observe(this, object : Observer<List<User>> {
            override fun onChanged(list: List<User>?) {
                Log.e("LOG", "onChanged")
                if (list != null) {
                    showUsersAdapter.addItems(list)
                }
            }
        })

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        //outState?.putParcelable(LIST_DISPLAYING_USERS, recyclerView.layoutManager.onSaveInstanceState())
        //outState?.putParcelableArrayList(LIST_USERS_IN_ADAPTER, showUsersAdapter.users as ArrayList<out Parcelable>)
    }


    companion object {
        val LIST_DISPLAYING_USERS = "LIST_DISPLAYING_USERS"
        val LIST_USERS_IN_ADAPTER = "LIST_USERS_IN_ADAPTER"
        fun newInstance(): AllUsersFragment {
            val fragment = AllUsersFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }
}
