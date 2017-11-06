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
import ru.a1024bits.bytheway.adapter.DisplaySimilarTravelsAdapter
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.viewmodel.ShowUsersViewModel
import java.util.*
import javax.inject.Inject

class SimilarTravelsFragment : Fragment() {
    private lateinit var currentView: View
    private lateinit var viewModel: ShowUsersViewModel
    private lateinit var showUsersAdapter: DisplaySimilarTravelsAdapter
    private lateinit var recyclerView: RecyclerView
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_display_similar_user_travels, container, false)
        return currentView
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = currentView.findViewById(R.id.display_similar_user_travels)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.component.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ShowUsersViewModel::class.java)
        showUsersAdapter = DisplaySimilarTravelsAdapter(this.context)
        recyclerView.adapter = showUsersAdapter

        val observer = Observer<List<User>> { list ->
            Log.e("LOG", "onChanged " + list + " - size")
            if (list != null) {
                val random = Random()
                for (user in list) {
                    user.percentsSimilarTravel = random.nextInt(100)
                    Log.e("tag", "11user percents: " + user.percentsSimilarTravel + " \n")
                }
                showUsersAdapter.addItems(list)
            }
        }
        viewModel.getSimilarUsersTravels(Filter(/* some data for filtering*/), observer).observe(this, observer)
    }

    companion object {
        fun newInstance(): SimilarTravelsFragment {
            val fragment = SimilarTravelsFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }
}