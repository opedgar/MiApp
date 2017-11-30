package com.example.edgar.miapp

import android.content.Intent
import android.media.Image
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.OptionalPendingResult
import com.google.android.gms.common.api.Result
import com.google.android.gms.common.api.ResultCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    lateinit var photoImageView:ImageView
    lateinit var nameTextView:TextView
    lateinit var emailTextView:TextView
    lateinit var idTextView:TextView

    lateinit var googleApiClient:GoogleApiClient

    lateinit var firebaseAuth:FirebaseAuth
    lateinit var firebaseAuthListener:FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        photoImageView = findViewById<ImageView>(R.id.photoImageView)
        nameTextView = findViewById<TextView>(R.id.nameTextView)
        emailTextView = findViewById<TextView>(R.id.emailTextView)
        idTextView = findViewById<TextView>(R.id.idTextView)

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            var user = firebaseAuth.currentUser
            if (user!=null){
                setUserData(user)
            }else{
                goLoginScreen()
            }
        }
    }

    private fun setUserData(user: FirebaseUser) {
        nameTextView.setText(user.displayName)
        emailTextView.setText(user.email)
        idTextView.setText(user.uid)
        Glide.with(this).load(user.photoUrl).into(photoImageView)
    }

    override fun onStart() {
        super.onStart()

        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    private fun goLoginScreen() {
        intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun logOut(view: View) {
        firebaseAuth.signOut()

        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback { status ->
            if (status.isSuccess){
                goLoginScreen()
            }else {
                Toast.makeText(applicationContext, getString(R.string.not_logout), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun revoke(view:View) {
        firebaseAuth.signOut()

        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback { status ->
            if (status.isSuccess){
                goLoginScreen()
            }else {
                Toast.makeText(applicationContext, "No se pudo revocar la sesión", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStop() {
        super.onStop()

        if (firebaseAuthListener!=null){
            firebaseAuth.removeAuthStateListener(firebaseAuthListener)
        }
    }
}
