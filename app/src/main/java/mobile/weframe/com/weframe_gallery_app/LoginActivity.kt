package mobile.weframe.com.weframe_gallery_app

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.ResponseType
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import kotlinx.android.synthetic.main.content_login.*
import mobile.weframe.com.weframe_gallery_app.gallery.UserPictureGalleryActivity
import mobile.weframe.com.weframe_gallery_app.rest.RestService

class LoginActivity : AppCompatActivity() {

    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        button = findViewById(R.id.login_button)
        val auth0 = createAuth0()
        val sharedPref = this.getSharedPreferences(
            getString(R.string.credentials_shared_preferences), Context.MODE_PRIVATE)
        button.setOnClickListener {
            runOnUiThread {
                button.visibility = View.GONE
            }
            WebAuthProvider.init(auth0)
                .withScheme("demo")
                .withAudience("http://localhost:8080")
                .start(this@LoginActivity, authCallback(sharedPref))
        }

    }

    private fun authCallback(sharedPref : SharedPreferences): AuthCallback {
        return object : AuthCallback {
            override fun onFailure(dialog: Dialog) {
                runOnUiThread {
                    dialog.show()
                    button.visibility = View.VISIBLE
                }
            }

            override fun onFailure(exception: AuthenticationException) {
                runOnUiThread {
                    Toast.makeText(
                        this@LoginActivity,
                        "Error: " + exception.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    button.visibility = View.VISIBLE
                }
            }

            override fun onSuccess(credentials: Credentials) {
                with(sharedPref.edit()) {
                    putString(getString(R.string.auth_token), credentials.accessToken)
                    putString(getString(R.string.auth_refresh_token), credentials.refreshToken)
                    putLong(getString(R.string.auth_token_expiration_at), credentials.expiresAt!!.time)
                    RestService.instance.processLogin(credentials.accessToken!!)
                    apply()
                }
                startActivity(createIntent(UserPictureGalleryActivity::class.java))
            }
        }
    }

    private fun createIntent(clazz : Class<out Any>) : Intent {
        var intent = Intent(applicationContext, clazz)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        return intent;
    }

    private fun createAuth0(): Auth0 {
        val auth0 = Auth0(this)
        auth0.isOIDCConformant = true
        return auth0
    }

}
