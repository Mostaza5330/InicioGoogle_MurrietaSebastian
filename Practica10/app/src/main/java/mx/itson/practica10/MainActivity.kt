package mx.itson.practica10

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)

        // Verificar si el usuario ya está logueado
        if (isUserLoggedIn()) {
            goToWelcomeActivity()
            return
        }

        setContentView(R.layout.activity_main)
        setupGoogleSignIn()
        setupClickListeners()
    }

    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        findViewById<MaterialCardView>(R.id.cvGoogleSignIn).setOnClickListener {
            signInWithGoogle()
        }

        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            loginWithEmailPassword()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 100)
    }

    private fun loginWithEmailPassword() {
        val email = findViewById<EditText>(R.id.etEmail).text.toString()
        val password = findViewById<EditText>(R.id.etPassword).text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            saveUserSession(email, "Email")
            goToWelcomeActivity(email, "Email")
        } else {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {
            handleGoogleSignInResult(data)
        }
    }

    private fun handleGoogleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            val email = account.email ?: "No email"
            val provider = "Google"

            // Guardar sesión
            saveUserSession(email, provider)

            // Ir a la pantalla principal
            goToWelcomeActivity(email, provider)

        } catch (e: ApiException) {
            Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserSession(email: String, provider: String) {
        with(sharedPreferences.edit()) {
            putBoolean("is_logged_in", true)
            putString("user_email", email)
            putString("user_provider", provider)
            putLong("login_timestamp", System.currentTimeMillis())
            apply()
        }
    }

    private fun goToWelcomeActivity(email: String? = null, provider: String? = null) {
        val userEmail = email ?: sharedPreferences.getString("user_email", "")
        val userProvider = provider ?: sharedPreferences.getString("user_provider", "")

        val intent = Intent(this, WelcomeActivity::class.java).apply {
            putExtra("mail", userEmail)
            putExtra("provider", userProvider)
        }
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        // Verificar cuenta de Google existente
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null && !isUserLoggedIn()) {
            saveUserSession(account.email ?: "", "Google")
            goToWelcomeActivity()
        }
    }
}
