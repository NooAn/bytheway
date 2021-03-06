package ru.a1024bits.bytheway.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextPaint
import android.util.Log
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.viewmodel.RegistrationViewModel
import javax.inject.Inject
import kotlin.math.roundToInt

class SplashActivity : AppCompatActivity() {

    private val preferences by lazy { getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE) }

    private var viewModel: RegistrationViewModel? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        App.component.inject(this)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RegistrationViewModel::class.java)
          viewModel?.setTimestamp(FirebaseAuth.getInstance().currentUser?.uid ?: return)

//        val text = "ROME1"
//        val text2 = "КРАСНОДАР"
//        Log.e("Log", text2.length.toString())
//        val bitmap = drawTextToBitmap(context = baseContext,
//                gResId = R.drawable.template_for_posting,
//                textSize = getTextSize(text, text2),
//                text1 = text,
//                text2 = text2)
//        findViewById<ImageView>(R.id.test).setImageBitmap(bitmap)
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
