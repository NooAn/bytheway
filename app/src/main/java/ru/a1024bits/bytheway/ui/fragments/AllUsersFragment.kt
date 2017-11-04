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
        showUsersAdapter = ShowAllUsersAdapter(this.context)
        recyclerView.adapter = showUsersAdapter

        viewModel.getAllUsers(showUsersAdapter).observe(this, Observer<List<User>> { list ->
            Log.e("LOG", "onChanged")
            if (list != null) {
                showUsersAdapter.addItems(list)
            }
        })

    }

    companion object {
        fun newInstance(): AllUsersFragment {
            val fragment = AllUsersFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }
}
