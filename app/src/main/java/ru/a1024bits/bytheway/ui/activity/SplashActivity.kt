package ru.a1024bits.bytheway.ui.activity


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.architecture.ext.getViewModel
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.viewmodel.RegistrationViewModel

class SplashActivity : AppCompatActivity() {

    private val preferences by lazy { getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE) }

    private var viewModel: RegistrationViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        viewModel = getViewModel()

        viewModel?.setTimestamp(FirebaseAuth.getInstance().currentUser?.uid ?: return)
        Log.e("LOG", "on ")
    }

    override fun onResume() {
        super.onResume()
        checkRegistrationAndForward()
    }

    private fun checkRegistrationAndForward() {
        //checks first enter
        if (preferences.getBoolean(Constants.FIRST_ENTER, true)) {
            startActivity(Intent(this, RegistrationActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
        } else {
            val menuActivityIntent = Intent(this, MenuActivity::class.java)
            if (intent.extras != null) {
                menuActivityIntent.putExtras(intent.extras)
            }
            menuActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            //if it isn't first start
            startActivity(menuActivityIntent)
        }
    }
}
