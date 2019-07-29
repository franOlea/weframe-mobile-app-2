package mobile.weframe.com.weframe_gallery_app.gallery.detail

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import mobile.weframe.com.weframe_gallery_app.R
import mobile.weframe.com.weframe_gallery_app.gallery.UserPictureGalleryActivity
import mobile.weframe.com.weframe_gallery_app.gallery.provider.RestUserPictureProvider
import mobile.weframe.com.weframe_gallery_app.rest.NotLoggedInException
import mobile.weframe.com.weframe_gallery_app.rest.UserPicture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.app.Activity



class UserPictureActivity : AppCompatActivity() {

    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    companion object {
        const val EXTRA_USER_PICTURE = "UserPictureActivity.EXTRA_USER_PICTURE"
        const val DELETED_RESULT = "deleted"
        const val USER_PICTURE_EXTRA = "userPicture"
    }

    private lateinit var imageView: ImageView
    private lateinit var userPicture: UserPicture
    private lateinit var deleteButton: FloatingActionButton

    private val userPictureProvider = RestUserPictureProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_picture)
        userPicture = intent.getParcelableExtra(EXTRA_USER_PICTURE)
        imageView = findViewById(R.id.image)
        deleteButton = findViewById(R.id.delete_image_button)
    }

    override fun onStart() {
        super.onStart()

        Picasso.get()
            .load(userPicture.picture.url)
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.error)
            .fit()
//            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
//            .networkPolicy(NetworkPolicy.NO_CACHE)
            .into(imageView, object : Callback {

                override fun onSuccess() {
                    val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                    onPalette(Palette.from(bitmap).generate())
                }

                override fun onError(e: Exception?) {
                }
            })

        deleteButton.setOnClickListener {
            executorService.submit {deleteUserPicture() }
        }
    }

    private fun deleteUserPicture() {
        try {
            userPictureProvider.delete(userPicture.id)
        } catch (e: NotLoggedInException) {
            loadActivity(UserPictureGalleryActivity::class.java)
        } catch (e: Exception) {
            loadGalleryActivity()
        }

        val returnIntent = Intent()
        returnIntent.putExtra(DELETED_RESULT, true)
        returnIntent.putExtra(USER_PICTURE_EXTRA, userPicture)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private fun loadGalleryActivity() {
        loadActivity(UserPictureGalleryActivity::class.java)
    }

    private fun loadActivity(clazz: Class<out Any>) {
        val intent = Intent(applicationContext, clazz)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    fun onPalette(palette: Palette?) {
        if (null != palette) {
            val parent = imageView.parent.parent as ViewGroup
            parent.setBackgroundColor(palette.getDarkVibrantColor(Color.GRAY))
        }
    }
}