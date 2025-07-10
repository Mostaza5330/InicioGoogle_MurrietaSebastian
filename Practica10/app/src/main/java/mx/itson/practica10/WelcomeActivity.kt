package mx.itson.practica10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import android.content.SharedPreferences
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class WelcomeActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)

        // Configurar Google Sign-In Client (ESTO FALTABA)
        setupGoogleSignInClient()

        // Configurar UI
        setupUI()
    }

    private fun setupGoogleSignInClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupUI() {
        val evCorreo: TextView = findViewById(R.id.tvUserEmail)
        val evProveedor: TextView = findViewById(R.id.tvAuthProvider)

        // Obtener datos del Intent o de SharedPreferences
        val mail = intent.getStringExtra("mail") ?: sharedPreferences.getString("user_email", "No disponible")
        val provider = intent.getStringExtra("provider") ?: sharedPreferences.getString("user_provider", "No disponible")

        evCorreo.text = "Correo: $mail"
        evProveedor.text = "Proveedor: $provider"

        // Configurar botón de logout
        val btnSalir: Button = findViewById(R.id.btnLogout)
        btnSalir.setOnClickListener {
            logout()
        }
    }

    private fun logout() {

        // Obtener el proveedor para saber cómo hacer logout
        val provider = sharedPreferences.getString("user_provider", "")

        when (provider) {
            "Google" -> {
                // Logout de Google
                googleSignInClient.signOut().addOnCompleteListener { task ->
                    googleSignInClient.revokeAccess().addOnCompleteListener {
                        clearUserSession()
                        goToMainActivity()
                    }
                }
            }
        }
    }

    private fun clearUserSession() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }

    private fun goToMainActivity() {
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

}
