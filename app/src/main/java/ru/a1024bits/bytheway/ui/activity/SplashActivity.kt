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
//        val s = "НИЖEГОРОДСКАЯ ОБЛАСТЬ"
//        val s1 = "САНКТ-ПЕТЕРБУРГ"
//        val s2 = "МОСКВА"
//        val s3 = "СОЧИ"
//        val s4 = "MOSCOW"
//        val s5 = "AMSTERDAM"
//        val textSize = getTextSize(s4, s5)
//        val bitmap = drawTextToBitmap(context = baseContext,
//                gResId = R.drawable.template_for_posting,
//                textSize = textSize,
//                text1 = s4,
//                text2 = s5)
      //  findViewById<ImageView>(R.id.test).setImageBitmap(bitmap)
    }

    private fun getTextSize(text1: String, text2: String) =
            if (text1.length >= 21 || text2.length >= 20) 90 else 125


    private fun drawTextToBitmap(context: Context, gResId: Int, textSize: Int = 78, text1: String, text2: String): Bitmap {
        val resources = context.resources
        val scale = resources.displayMetrics.density
        var bitmap = BitmapFactory.decodeResource(resources, gResId)

        var bitmapConfig = bitmap.config;
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true)

        val canvas = Canvas(bitmap)
        // new antialised Paint
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // text color - #3D3D3D
        paint.color = Color.rgb(93, 101, 67)
        // text size in pixels
        paint.textSize = (textSize * scale).roundToInt().toFloat()
        val fontFace = ResourcesCompat.getFont(context, R.font.acrobat)
        paint.typeface = Typeface.create(fontFace, Typeface.NORMAL)
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE)

        // draw text to the Canvas center
        val bounds = Rect()
        paint.getTextBounds(text1, 0, text1.length, bounds)
        var x = (bitmap.width - bounds.width()) / 2f - 470
        var y = (bitmap.height + bounds.height()) / 2f - 140
        canvas.drawText(text1, x, y, paint)

        paint.getTextBounds(text2, 0, text2.length, bounds)
        x = (bitmap.width - bounds.width()) / 2f - 470
        y = (bitmap.height + bounds.height()) / 2f + 235
        canvas.drawText(text2, x, y, paint)

        return bitmap
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
