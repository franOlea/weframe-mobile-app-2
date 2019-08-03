package mobile.weframe.com.weframe_gallery_app.gallery.detail

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import mobile.weframe.com.weframe_gallery_app.R
import mobile.weframe.com.weframe_gallery_app.gallery.UserPictureGalleryActivity
import mobile.weframe.com.weframe_gallery_app.gallery.provider.RestUserPictureProvider
import mobile.weframe.com.weframe_gallery_app.rest.NotLoggedInException
import mobile.weframe.com.weframe_gallery_app.rest.UserPicture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.squareup.picasso.Target




class UserPictureActivity : AppCompatActivity() {

    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    companion object {
        const val EXTRA_USER_PICTURE = "UserPictureActivity.EXTRA_USER_PICTURE"
        const val DELETED_RESULT = "deleted"
        const val USER_PICTURE_EXTRA = "userPicture"
    }

    private lateinit var imageView: PhotoView
    private lateinit var userPicture: UserPicture
    private lateinit var deleteButton: FloatingActionButton
    private lateinit var progressBar: ProgressBar

    private val userPictureProvider = RestUserPictureProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_picture)
        userPicture = intent.getParcelableExtra(EXTRA_USER_PICTURE)
        imageView = findViewById(R.id.image)
        deleteButton = findViewById(R.id.delete_image_button)
        progressBar = findViewById(R.id.progress_bar)
    }

    override fun onStart() {
        super.onStart()

        Picasso.get()
            .load(userPicture.picture.url)
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.error)
            .into(target)

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

    private val target = object : Target {

        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            progressBar.visibility = View.GONE
            imageView.setImageBitmap(bitmap)
            val imageBitmap = (imageView.drawable as BitmapDrawable).bitmap
            onPalette(Palette.from(imageBitmap).generate())
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

        }

        override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {

        }
    }
}