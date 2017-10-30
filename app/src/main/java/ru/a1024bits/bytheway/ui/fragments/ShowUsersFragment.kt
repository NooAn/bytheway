package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.ShowAllUsersAdapter
import ru.a1024bits.bytheway.viewmodel.MockUsersViewModel


class ShowUsersFragment : Fragment(){
    private lateinit var currentView: View
    private lateinit var viewModel: MockUsersViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_show_users, container, false)
        return currentView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MockUsersViewModel::class.java)
        val recyclerView: RecyclerView = currentView.findViewById(R.id.lazy_shower_users)
        recyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = ShowAllUsersAdapter(recyclerView, this.context, viewModel.userRepo)
    }


    companion object {
        fun newInstance(): ShowUsersFragment {
            val fragment = ShowUsersFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }
}