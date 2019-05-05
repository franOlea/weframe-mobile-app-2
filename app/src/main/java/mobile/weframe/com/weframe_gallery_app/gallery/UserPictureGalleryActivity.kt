package mobile.weframe.com.weframe_gallery_app.gallery

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import mobile.weframe.com.weframe_gallery_app.LoginActivity
import mobile.weframe.com.weframe_gallery_app.R
import mobile.weframe.com.weframe_gallery_app.gallery.provider.DownloadFilesTask
import mobile.weframe.com.weframe_gallery_app.gallery.provider.InMemoryUserPictureProvider
import mobile.weframe.com.weframe_gallery_app.gallery.provider.PageRequest
import mobile.weframe.com.weframe_gallery_app.gallery.provider.RestUserPictureProvider
import mobile.weframe.com.weframe_gallery_app.rest.NotLoggedInException
import java.util.concurrent.TimeUnit

class UserPictureGalleryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var imageGalleryAdapter: UserPictureGalleryAdapter
    private val userPictureProvider =
        RestUserPictureProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_picture_gallery)

        val layoutManager = GridLayoutManager(this, 2)
        recyclerView = findViewById(R.id.rv_images)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        val userPictures = getUserPictures()
        imageGalleryAdapter = UserPictureGalleryAdapter(this, userPictures)
    }

    private fun getUserPictures(): List<UserPicture> {
        return try {
            userPictureProvider.get()
        } catch(e : NotLoggedInException) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            emptyList()
        }
    }

    override fun onStart() {
        super.onStart()
        recyclerView.adapter = imageGalleryAdapter
    }

}
