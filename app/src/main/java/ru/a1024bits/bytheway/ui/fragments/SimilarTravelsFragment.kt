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
import kotlinx.android.synthetic.main.fragment_display_similar_user_travels.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.DisplaySimilarTravelsAdapter
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.viewmodel.DisplayUsersViewModel
import java.util.*
import javax.inject.Inject

class SimilarTravelsFragment : Fragment() {
    private lateinit var currentView: View
    private lateinit var filter: Filter
    private lateinit var viewModel: DisplayUsersViewModel
    private lateinit var showUsersAdapter: DisplaySimilarTravelsAdapter
    private lateinit var recyclerView: RecyclerView
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    //private var countInitialElements = 0
    private var tempStartAge: Int = -1
    private var tempEndAge: Int = -1
    private var tempStartDate: Long = 0L
    private var tempEndDate: Long = 0L
    private var tempSex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filter = if (savedInstanceState != null) {
            tempStartAge = savedInstanceState.getInt("tempStartAge")
            tempSex = savedInstanceState.getInt("tempSex")
            tempEndAge = savedInstanceState.getInt("tempEndAge")
            tempStartDate = savedInstanceState.getLong("tempStartDate")
            tempEndDate = savedInstanceState.getLong("tempEndDate")
            savedInstanceState.getSerializable("filter") as Filter
        } else Filter()
    }

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
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DisplayUsersViewModel::class.java)
        showUsersAdapter = DisplaySimilarTravelsAdapter(this.context)
        recyclerView.adapter = showUsersAdapter

        viewModel.similarUsersLiveData.observe(this, Observer<List<User>> { list ->
            Log.e("LOG", "onChanged " + list + " - size")
            if (list != null) {
                val random = Random()
                for (user in list) {
                    user.percentsSimilarTravel = random.nextInt(100)
                    Log.e("tag", "11user percents: " + user.percentsSimilarTravel + " \n")
                }
                showUsersAdapter.addItems(list)
                loading_where_load_users.visibility = View.GONE
            }
        })
        viewModel.getUsersWithSimilarTravel(filter)
        loading_where_load_users.visibility = View.VISIBLE
    }

    companion object {
        fun newInstance(): SimilarTravelsFragment {
            val fragment = SimilarTravelsFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }
}