package com.example.edgar.miapp

import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    lateinit var googleApiClient:GoogleApiClient //Cliente necesario para usar los servicios de Google
    lateinit var signInButton:SignInButton
    val SIGN_IN_CODE = 777

    lateinit var firebaseAuth : FirebaseAuth
    lateinit var firebaseAuthListener : FirebaseAuth.AuthStateListener

    lateinit var progressBar:ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Parámetro de opciones
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)  //Autentificación básica
                .requestIdToken(getString(R.string.default_web_client_id))          //Solicitar token para iniciar sesión en Firebase con proveedor Google
                .requestEmail()                                                     //Pedir además de los datos básicos también el email
                .build()

        //Inicializamos el googleApiClient
        googleApiClient = GoogleApiClient.Builder(this) //El parámetro del Builder es el contexto, en nuestro caso LoginActivity
                .enableAutoManage(this, this)       //Permite gestionar el ciclo de vida del googleApiClient con el de la activity
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        signInButton = findViewById<SignInButton>(R.id.signInButton)            //asignar el botón que hemos creado en el design
        signInButton.setOnClickListener(View.OnClickListener { v ->
            val intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)  //Intent que abre el selector de inicio de sesión para una cuenta Google
            startActivityForResult(intent, SIGN_IN_CODE)                        //Le asignamos un código único
        })

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null){
                goMainScreen()
            }
        }

        progressBar = findViewById<ProgressBar>(R.id.progressBar)

    }

    override fun onStart() {
        super.onStart()

        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    //Método que nos falta para el segundo parámetro del enableAutomanage, que pide una reacción en caso de que algo salga mal
    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //Método usado para definir el comportamiento de startActivityForResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Comprobar el código único
        if (requestCode == SIGN_IN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result)
        }
    }

    //Manejar el resultado del SignIn
    private fun handleSignInResult(result: GoogleSignInResult) {
        if (result.isSuccess){
            firebaseAuthWithGoogle(result.signInAccount)
        }else Toast.makeText(this, getString(R.string.not_login), Toast.LENGTH_LONG)
    }

    //Creamos credencial y le otorgamos el token obtenido del account
    private fun firebaseAuthWithGoogle(signInAccount: GoogleSignInAccount?) {

        signInButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        var credential = GoogleAuthProvider.getCredential(signInAccount?.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, OnCompleteListener { task ->

            progressBar.visibility = View.GONE
            signInButton.visibility = View.VISIBLE
            if (!task.isSuccessful){
                Toast.makeText(applicationContext, "No se pudo autentificar con Firebase", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun goMainScreen() {
        var intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()

        if (firebaseAuthListener!=null){
            firebaseAuth.removeAuthStateListener { firebaseAuthListener }
        }
    }
}
