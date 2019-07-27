package mobile.weframe.com.weframe_gallery_app.gallery.detail

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import mobile.weframe.com.weframe_gallery_app.R
import mobile.weframe.com.weframe_gallery_app.rest.UserPicture

class UserPictureActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_PICTURE = "UserPictureActivity.EXTRA_USER_PICTURE"
    }

    private lateinit var imageView: ImageView
    private lateinit var userPicture: UserPicture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_picture)

        userPicture = intent.getParcelableExtra(EXTRA_USER_PICTURE)
        imageView = findViewById(R.id.image)
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
    }

    fun onPalette(palette: Palette?) {
        if (null != palette) {
            val parent = imageView.parent.parent as ViewGroup
            parent.setBackgroundColor(palette.getDarkVibrantColor(Color.GRAY))
        }
    }
}