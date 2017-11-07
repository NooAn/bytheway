package ru.a1024bits.bytheway.ui.activity

import android.arch.lifecycle.LifecycleActivity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_registration.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.viewmodel.MyProfileViewModel
import javax.inject.Inject


class RegistrationActivity : LifecycleActivity(), GoogleApiClient.OnConnectionFailedListener {
    override fun onConnectionFailed(p0: ConnectionResult) {
    
    }
    
    
    private var viewModel: MyProfileViewModel? = null
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    
    private val RC_SIGN_IN = 9001
    private val mAuth = FirebaseAuth.getInstance();
    private var mGoogleApiClient: GoogleApiClient? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        App.component.inject(this)
        
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
        
        signInButton.setSize(SignInButton.SIZE_STANDARD)
        signInButton.setOnClickListener({
            signIn()
        })
        
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyProfileViewModel::class.java)
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
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result)
        }
    }
    
    private fun handleSignInResult(result: GoogleSignInResult) {
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.
            val acct = result.signInAccount
            firebaseAuthWithGoogle(acct!!)
        } else {
            // Signed out, show unauthenticated UI.
            if (result.status.statusCode != 200) {
                Log.d("LOG", "Problems with enternet ${result.status.statusCode} and ${result.status.toString()}")
            }
            updateUI(false)
        }
    }
    
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("LOG", "firebaseAuthWithGoogle: ${acct.id}  ${acct.idToken}")
        
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("LOG", "signInWithCredential:success")
                        updateUI(true)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("LOG", "signInWithCredential:failure", task.exception)
                        Toast.makeText(this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }
    
    private fun updateUI(signedIn: Boolean) {
        if (signedIn) {
            Log.e("LOG2", mAuth?.currentUser?.photoUrl.toString())
            viewModel?.load?.observe(this, object : Observer<Boolean> {
                override fun onChanged(upload: Boolean?) {
                    if (upload?.equals(true)!!) {
                        startActivity(Intent(this@RegistrationActivity, MenuActivity::class.java))
                    } else {
                        Toast.makeText(this@RegistrationActivity, "Error for registration", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            viewModel?.ifUserNotExistThenSave(mAuth.currentUser)
        } else {
            Toast.makeText(this, "Not allowed Google", Toast.LENGTH_SHORT).show()
        }
    }
    
    
}
