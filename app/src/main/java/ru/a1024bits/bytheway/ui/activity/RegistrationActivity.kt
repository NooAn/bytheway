package ru.a1024bits.bytheway.ui.activity

import android.app.Activity
import android.arch.lifecycle.LifecycleActivity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_registration.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.viewmodel.MyProfileViewModel
import ru.a1024bits.bytheway.viewmodel.RegistrationViewModel
import javax.inject.Inject


class RegistrationActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    private var viewModel: RegistrationViewModel? = null
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private val RC_SIGN_IN = 9001
    private val mAuth = FirebaseAuth.getInstance()
    private var mGoogleApiClient: GoogleApiClient? = null
    lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        App.component.inject(this)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(resources.getString(R.string.default_web_client_id))
                .requestId()
                .requestEmail()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* Activity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        Log.e("LOG", mAuth?.currentUser.toString())
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RegistrationViewModel::class.java)
        mFirebaseAnalytics.setUserId(mAuth?.currentUser.toString())
        mFirebaseAnalytics.setCurrentScreen(this, "Registration", this.javaClass.getSimpleName())
        mFirebaseAnalytics.logEvent("RegistrationScreen_Enter", null)

        viewModel?.load?.observe(this, object : Observer<Boolean> {
            override fun onChanged(upload: Boolean?) {
                if (upload == true) {
                    mFirebaseAnalytics.logEvent("RegistrationScreen_Success", null)
                    startActivity(Intent(this@RegistrationActivity, MenuActivity::class.java))
                } else {
                    mFirebaseAnalytics.logEvent("RegistrationScreen_Error_Checkin", null)
                    Toast.makeText(this@RegistrationActivity, "Error for registration", Toast.LENGTH_SHORT).show()
                }
            }
        })
        signIn()
        if (isNetworkAvailable() == false) showSnack()
    }

    var snackbar: Snackbar? = null

    private fun showSnack() {
        snackbar = Snackbar.make(this.findViewById(android.R.id.content), R.string.no_internet, Snackbar.LENGTH_LONG)
        snackbar?.show()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.getActiveNetworkInfo()
        return activeNetworkInfo != null && activeNetworkInfo.isConnected()
    }

    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onStart() {
        super.onStart()
    }

    //fixme Status{statusCode=NETWORK_ERROR, resolution=null}
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("LOG", "result activity registration")
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result != null)
                handleSignInResult(result)
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.
            val acct = result.signInAccount
            acct?.let { account -> firebaseAuthWithGoogle(account) } //maybe not this variant
        } else {
            // Signed out, show unauthenticated UI.
            if (result.status.statusCode != 200) {
                mFirebaseAnalytics.logEvent("RegistrationScreen_Error_Not200", null)
                Log.d("LOG", "Problems with enternet ${result.status.statusCode} and ${result.status.toString()}")
            } else {
                mFirebaseAnalytics.logEvent("RegistrationScreen_Error_NotKnow", null)
            }
            updateUI(false)
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("LOG", "firebaseAuthWithGoogle: ${acct.id}  ${acct.idToken}")

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)

        mAuth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("LOG", "signInWithCredential:success")
                        updateUI(true)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("LOG", "signInWithCredential:failure", task.exception)
                        Toast.makeText(this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        mFirebaseAnalytics.logEvent("RegistrationScreen_Error_Login", null)
                    }
                }
    }

    private fun updateUI(signedIn: Boolean) {
        if (signedIn) {
            mFirebaseAnalytics.logEvent("RegistrationScreen_Success_Login", null)
            viewModel?.ifUserNotExistThenSave(mAuth.currentUser)
        } else {
            Toast.makeText(this, "Not allowed Google", Toast.LENGTH_SHORT).show()
        }
    }
}
