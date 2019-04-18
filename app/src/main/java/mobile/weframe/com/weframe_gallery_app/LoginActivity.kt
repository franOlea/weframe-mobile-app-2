package mobile.weframe.com.weframe_gallery_app

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import kotlinx.android.synthetic.main.content_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val auth0 = createAuth0()


        val sharedPref = this.getSharedPreferences(
            getString(R.string.credentials_shared_preferences), Context.MODE_PRIVATE)
        button.setOnClickListener {

            WebAuthProvider.init(auth0)
                .withScheme("demo")
                .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
                .start(this@LoginActivity, authCallback(sharedPref))
        }

    }

    private fun authCallback(sharedPref : SharedPreferences): AuthCallback {
        return object : AuthCallback {
            override fun onFailure(dialog: Dialog) {
                runOnUiThread { dialog.show() }
            }

            override fun onFailure(exception: AuthenticationException) {
                runOnUiThread {
                    Toast.makeText(
                        this@LoginActivity,
                        "Error: " + exception.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onSuccess(credentials: Credentials) {
                sharedPref.edit().putString(getString(R.string.auth_token), credentials.accessToken)
                sharedPref.edit().putString(getString(R.string.auth_refresh_token), credentials.refreshToken)
                sharedPref.edit().putLong(getString(R.string.auth_token_expiration_at), credentials.expiresAt!!.time)
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
