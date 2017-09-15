package ru.a1024bits.bytheway

import android.app.Dialog
import android.arch.lifecycle.LifecycleActivity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_registration.*


class RegistrationActivity : LifecycleActivity(), GoogleApiClient.OnConnectionFailedListener {
    override fun onConnectionFailed(p0: ConnectionResult) {
    
    }
    
    private val RC_SIGN_IN = 9001
    private var mAuth: FirebaseAuth? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        mAuth = FirebaseAuth.getInstance();
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("51228349143-av2s208nris19uo3tm66i22e4narsrju.apps.googleusercontent.com")
                .requestId()
                .requestEmail()
                .build()
        
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* Activity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
        
        signInButton.setSize(SignInButton.SIZE_STANDARD)
        signInButton.setOnClickListener({
            // showPasswordDialog();
            signIn()
        })
        
    }
    
    private fun showPasswordDialog() {
        val dialog = Dialog(this)
        // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.sign_up)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialog.setTitle("Ваш пароль")
        dialog.show()
    }
    
    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    
    override fun onStart() {
        super.onStart()
        var currentUser: FirebaseUser? = mAuth?.currentUser;
        updateUI(currentUser)
    }
    
    private fun updateUI(currentUser: FirebaseUser?) {
        Log.d("LOG", currentUser.toString() + currentUser?.photoUrl + currentUser?.displayName)
        //impl in future for help
    }
    
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        
        
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result)
        }
    }
    
    private fun handleSignInResult(result: GoogleSignInResult) {
        Log.d("LOG", "handleSignInResult: ${result.isSuccess} ${result.status.statusMessage} ${result.status.statusCode} ${result.status}")
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.
            val acct = result.signInAccount
            // mStatusTextView.setText(getString(R.string.signed_in_fmt, acct!!.displayName))
            Toast.makeText(this, acct!!.displayName, Toast.LENGTH_SHORT).show();
            updateUI(true)
            firebaseAuthWithGoogle(acct)
        } else {
            // Signed out, show unauthenticated UI.
            if (result.status.statusCode != 200) {
                Log.d("LOG", "Problems with enternet")
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
                        val user = mAuth!!.getCurrentUser()
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("LOG", "signInWithCredential:failure", task.exception)
                        Toast.makeText(this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
    }
    
    private fun updateUI(signedIn: Boolean) {
        if (signedIn) {
            Toast.makeText(this, "It's Okay!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Not allowed Google", Toast.LENGTH_SHORT).show()
        }
    }
}
