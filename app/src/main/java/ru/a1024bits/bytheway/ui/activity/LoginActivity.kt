package ru.a1024bits.bytheway.ui.activity

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.crashlytics.android.Crashlytics
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
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.*
import io.fabric.sdk.android.Fabric
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.exception.InvalidCredentialsException
import ru.a1024bits.bytheway.ui.dialogs.newRegistrationByPhone
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.viewmodel.RegistrationViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val GOOGLE_SIGN_IN_ACTIVITY_CODE = 9001
private const val GOOGLE_SIGN_IN_CODE = "GOOGLE"
private const val VK_SIGN_IN_CODE = "VK"
private const val ANALYTICS_TAG = "RegS"

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: RegistrationViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val authManager = FirebaseAuth.getInstance()
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var analyticsManager: FirebaseAnalytics
    var mVerificationId: String = ""
    private val preferences: SharedPreferences by lazy { getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        App.component.inject(this)
        authManager.useAppLanguage()
        analyticsManager = FirebaseAnalytics.getInstance(applicationContext)

        googleApiClient = createGoogleApiClient()

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RegistrationViewModel::class.java)

        logStartRegistration()

        subscribeOnRegistration()

        numberLogIn.setOnClickListener { startRegistrationByNumber() }

        googleLogIn.setOnClickListener { startRegistrationIfPossible(GOOGLE_SIGN_IN_CODE) }

        vkLogIn.setOnClickListener { startRegistrationIfPossible(VK_SIGN_IN_CODE) }
    }

    fun validatePhoneNumber(phoneNumber: String): Boolean =
            viewModel.validatePhoneNumber(phoneNumber)

    fun authPhone(phone: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(viewModel.prepareNumber(phone), 30, TimeUnit.SECONDS, this,
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(authCred: PhoneAuthCredential?) {
                        if (authCred is AuthCredential)
                            signInGoogle(authCred)
                    }

                    override fun onVerificationFailed(e: FirebaseException?) {
                        if (e is FirebaseAuthInvalidCredentialsException) {
                            Log.w("LOG", "Invalid Credential", e)
                            logEvent("invalid_sms")

                        } else if (e is FirebaseTooManyRequestsException) {
                            Log.w("LOG", "many request", e)
                            logEvent("error_lot_req")

                        }
                        Toast.makeText(this@LoginActivity, R.string.just_error, Toast.LENGTH_SHORT).show()
                    }

                    override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken) {
                        super.onCodeSent(verificationId, token)
                        mVerificationId = if (verificationId == null || verificationId.isEmpty()) "" else verificationId
                    }
                })
    }

    fun registerUserByNumber(number: String): Completable =
            Completable.create { emitter ->
                if (mVerificationId.isNotEmpty()) {
                    signInGoogle(PhoneAuthProvider.getCredential(mVerificationId, number))
                    viewModel.load.observe(this, object : Observer<Boolean> {
                        override fun onChanged(result: Boolean?) {
                            result?.let { if (it) emitter.onComplete() else emitter.onError(InvalidCredentialsException()) }
                            viewModel.load.removeObserver(this)
                        }
                    })
                } else
                    emitter.onError(Exception("Not valid Verification Id"))
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (isResultOfVkLogIn(requestCode, resultCode, data))
        else {
            super.onActivityResult(requestCode, resultCode, data)
            when (requestCode) {
                GOOGLE_SIGN_IN_ACTIVITY_CODE ->
                    data?.let { handleSignInGoogleResult(Auth.GoogleSignInApi.getSignInResultFromIntent(data)) }
                            ?: let { createValidUser(false) }
                else -> createValidUser(false)
            }
        }
    }

    private fun isResultOfVkLogIn(requestCode: Int, resultCode: Int, data: Intent?) =
            VKSdk.onActivityResult(requestCode, resultCode, data, onVkRegistrationCallback())

    private fun onVkRegistrationCallback(): VKCallback<VKAccessToken> =
            object : VKCallback<VKAccessToken> {
                override fun onResult(vkToken: VKAccessToken) {
                    VKApi.users().get(VKParameters.from(VKApiConst.ACCESS_TOKEN, vkToken.accessToken,
                            VKApiConst.FIELDS, "first_name_{mom}", VKApiConst.FIELDS, "last_name_{mom}",
                            VKApiConst.FIELDS, "photo_max"))
                            .executeWithListener(object : VKRequest.VKRequestListener() {
                                override fun onComplete(response: VKResponse) {
                                    super.onComplete(response)
                                    viewModel.registrationUserWithVk(vkToken.userId)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                with(response.json.getJSONArray("response").getJSONObject(0)) {
                                                    viewModel.initUserWithVkSignIn(getString("photo_max"),
                                                            getString("first_name"), getString("last_name"))
                                                    createValidUser(true)
                                                }
                                            }, { createValidUser(false) })
                                }
                            })
                }

                override fun onError(error: VKError?) {
                    showMessageIntoToast("vk login not completed")
                }
            }

    fun signInGoogle(credential: AuthCredential) {
        authManager.signInWithCredential(credential)
                .addOnSuccessListener(this) {
                    viewModel.initUserWithGoogleOrPhoneSignIn()
                    createValidUser(true)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    logEvent("RegistrationScreen_Error_Login")
                    logException(it)
                    createValidUser(false)
                }
    }


    private fun startRegistrationIfPossible(logInCode: String) {
        if (isNetworkAvailable())
            startRegistration(logInCode)
        else
            showDialogInternet(logInCode)
    }

    private fun startRegistration(logInCode: String) {
        when (logInCode) {
            GOOGLE_SIGN_IN_CODE -> signInGoogle()
            VK_SIGN_IN_CODE -> signInVk()
        }
    }

    private fun signInGoogle() {
        startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(googleApiClient), GOOGLE_SIGN_IN_ACTIVITY_CODE)
    }

    private fun signInVk() {
        VKSdk.login(this@LoginActivity)
    }

    private fun subscribeOnRegistration() {
        viewModel.load.observe(this, Observer<Boolean> { upload ->
            upload?.let { if (upload) onSuchRegistration() else onFailRegistration() }
        })
    }

    private fun logStartRegistration() {
        analyticsManager.setUserId(authManager?.currentUser.toString())
        analyticsManager.setCurrentScreen(this, "Registration", this.javaClass.simpleName)
        logEvent("Enter")
    }

    private fun onFailRegistration() {
        logEvent("Error_Checking")
        createValidUser(false)
        Toast.makeText(this@LoginActivity, "Error for registration", Toast.LENGTH_SHORT).show()
    }


    private fun onSuchRegistration() {
        logEvent("Success")
        openMainActivity()
    }

    private fun openMainActivity() {
        startActivity(Intent(this@LoginActivity, MenuActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
        preferences.edit()
                .putBoolean(Constants.FIRST_ENTER, false).apply()
    }

    private fun createGoogleApiClient(): GoogleApiClient =
            GoogleApiClient.Builder(this.applicationContext)
                    .enableAutoManage(this,
                            { result -> onFailGoogleConnection(result, "Google_Connect_Fail") })
                    .addApi(Auth.GOOGLE_SIGN_IN_API, createGoogleSignInOptions())
                    .build()

    private fun onFailGoogleConnection(result: ConnectionResult, event: String) {
        logEvent(event)
        logException(Exception("${result.errorMessage} ${result.errorCode}"))
    }

    private fun createGoogleSignInOptions(): GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(resources.getString(R.string.default_web_client_id))
                    .requestId()
                    .requestEmail()
                    .build()

    private fun showDialogInternet(logInCode: String) {
        with(AlertDialog.Builder(this).create()) {
            setTitle("Info")
            setMessage(resources.getString(R.string.no_internet))
            setButton(Dialog.BUTTON_POSITIVE, "OK") { _, _ ->
                startRegistration(logInCode)
                dismiss()
            }
            show()
        }
    }

    private fun isNetworkAvailable(): Boolean =
            with((getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo) { isConnected }

    private fun handleSignInGoogleResult(result: GoogleSignInResult) {
        if (result.isSuccess) {
            result.signInAccount?.let { account -> signInGoogle(getCredential(account)) }
        } else {
            if (result.status.statusCode == 200) {
                logEvent("Error_NotKnow")
            } else {
                logEvent("Error_Not200")
                logEvent("status_${result.status.statusCode}")
            }
            createValidUser(false)
        }
    }

    private fun getCredential(googleAccount: GoogleSignInAccount) =
            GoogleAuthProvider.getCredential(googleAccount.idToken, null)

    private fun createValidUser(signedIn: Boolean) {
        if (signedIn) {
            logEvent("Success_Login")
            viewModel.ifUserNotExistThenSave()
        } else {
            showMessageIntoToast(R.string.problem_with_this_way_authorization)
        }
    }

    private fun showMessageIntoToast(stringId: Int) {
        showMessageIntoToast(applicationContext.getString(stringId))
    }

    private fun showMessageIntoToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun startRegistrationByNumber() {
        newRegistrationByPhone().show(supportFragmentManager, "dialogRegistrationOnNumber")
    }

    private fun logEvent(event: String) {
        analyticsManager.logEvent("${ANALYTICS_TAG}_$event", null)
        Log.e("LOG", "event: $event")
    }

    private fun logException(exception: Exception) {
        Crashlytics.logException(exception)
    }
}