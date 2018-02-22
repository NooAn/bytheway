package ru.a1024bits.bytheway.ui.activity

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
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.*
import com.google.firebase.crash.FirebaseCrash
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.ui.dialogs.ErrorStandartRegistrationDialog
import ru.a1024bits.bytheway.viewmodel.RegistrationViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class RegistrationActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    override fun onConnectionFailed(p0: ConnectionResult) {
        mFirebaseAnalytics.logEvent("RegistrationScreen_Connect_Fail", null)
        FirebaseCrash.report(Exception(p0.errorMessage + " " + p0.errorCode))
    }

    private var viewModel: RegistrationViewModel? = null
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val RC_SIGN_IN = 9001
    private val mAuth = FirebaseAuth.getInstance()
    private var mGoogleApiClient: GoogleApiClient? = null
    lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        App.component.inject(this)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        mAuth.useAppLanguage()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(resources.getString(R.string.default_web_client_id))
                .requestId()
                .requestEmail()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* Activity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RegistrationViewModel::class.java)
        mFirebaseAnalytics.setUserId(mAuth?.currentUser.toString())
        mFirebaseAnalytics.setCurrentScreen(this, "Registration", this.javaClass.getSimpleName())
        mFirebaseAnalytics.logEvent("RegistrationScreen_Enter", null)

        viewModel?.load?.observe(this, object : Observer<Boolean> {
            override fun onChanged(upload: Boolean?) {
                if (upload == true) {
                    mFirebaseAnalytics.logEvent("RegistrationScreen_Success", null)
                    startActivity(Intent(this@RegistrationActivity, MenuActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
                } else {
                    mFirebaseAnalytics.logEvent("RegistrationScreen_Error_Checkin", null)
                    updateUI(false)
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

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("LOG", "result activity registration")
        try {
            if (requestCode == RC_SIGN_IN) {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                result?.let {
                    handleSignInResult(it)
                }
            } else {
                updateUI(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateUI(false)
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

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        signInGoogle(credential)
    }

    fun signInGoogle(credential: AuthCredential, errorDialog: ErrorStandartRegistrationDialog? = null) {
        try {
            mAuth?.signInWithCredential(credential)
                    ?.addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            errorDialog?.dismissAllowingStateLoss()
                            Log.d("LOG", "signInWithCredential:success")
                            updateUI(true)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("LOG", "signInWithCredential:failure", task.exception)
                            Toast.makeText(this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                            mFirebaseAnalytics.logEvent("RegistrationScreen_Error_Login", null)
                            updateUI(false)
                        }
                    }
                    ?.addOnFailureListener {
                        Log.w("LOG", "signInWithCredential:failure")
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        mFirebaseAnalytics.logEvent("RegistrationScreen_Error_Login", null)
                        FirebaseCrash.report(it)
                        showErrorText()
                    }
        } catch (e: Exception) {
            FirebaseCrash.report(e)
            showErrorText()
        }
    }

    private fun updateUI(signedIn: Boolean) {
        if (signedIn) {
            mFirebaseAnalytics.logEvent("RegistrationScreen_Success_Login", null)
            viewModel?.ifUserNotExistThenSave(mAuth.currentUser)
        } else {
            showErrorText()
            Toast.makeText(this, "Проблемы с гугл аккаунтом", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showErrorText() {
        ErrorStandartRegistrationDialog.newInstance(this).show(supportFragmentManager, "dialogRegistrationOnNumber")
    }

    fun validatePhoneNumber(phone: EditText): Boolean {
        val phoneNumber = phone.text.toString()
        if (phoneNumber.isBlank() || !phoneNumber.matches(Regex("^\\+?\\d{10,12}$"))) {
            phone.error = "Invalid phone number."//falseui
            return false
        }
        return true
    }


    var mVerificationId: String? = null

    fun authPhone(phone: EditText) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone.text.toString(),        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(authCred: PhoneAuthCredential?) {
                        // This callback will be invoked in two situations:
                        // 1 - Instant verification. In some cases the phone number can be instantly
                        //     verified without needing to send or enter a verification code.
                        // 2 - Auto-retrieval. On some devices Google Play services can automatically
                        //     detect the incoming verification SMS and perform verification without
                        //     user action.
                        if (authCred is AuthCredential)
                            signInGoogle(authCred)
                    }

                    override fun onVerificationFailed(e: FirebaseException?) {
                        // This callback is invoked in an invalid request for verification is made,
                        // for instance if the the phone number format is not valid.
                        if (e is FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            Log.w("LOG", "Invalid Credintial");
                            mFirebaseAnalytics.logEvent("RegistrationScreen_invalid_sms", null)

                        } else if (e is FirebaseTooManyRequestsException) {
                            // The SMS quota for the project has been exceeded
                            Log.w("LOG", "many request", e);
                            mFirebaseAnalytics.logEvent("RegistrationScreen_error_lot_req", null)

                        }
                        Toast.makeText(this@RegistrationActivity, R.string.just_error, Toast.LENGTH_SHORT).show()

                    }

                    override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?) {
                        super.onCodeSent(verificationId, token)
                        // The SMS verification code has been sent to the provided phone number, we
                        // now need to ask the user to enter the code and then construct a credential
                        // by combining the code with a verification ID.
                        mVerificationId = verificationId;


                        // Save verification ID and resending token so we can use them later

                    }
                });
    }
}
