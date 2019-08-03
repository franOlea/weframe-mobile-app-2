package mobile.weframe.com.weframe_gallery_app.gallery

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mobile.weframe.com.weframe_gallery_app.LoginActivity
import mobile.weframe.com.weframe_gallery_app.R
import mobile.weframe.com.weframe_gallery_app.gallery.detail.UserPictureActivity.Companion.DELETED_RESULT
import mobile.weframe.com.weframe_gallery_app.gallery.detail.UserPictureActivity.Companion.USER_PICTURE_EXTRA
import mobile.weframe.com.weframe_gallery_app.gallery.provider.PageRequest
import mobile.weframe.com.weframe_gallery_app.gallery.provider.RestUserPictureProvider
import mobile.weframe.com.weframe_gallery_app.rest.UserPicture
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class UserPictureGalleryActivity : AppCompatActivity() {


    @Suppress("PrivatePropertyName")
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var imageGalleryAdapter: UserPictureGalleryAdapter
    private lateinit var addButton: FloatingActionButton
    private val userPictureProvider = RestUserPictureProvider()
    private val userPictures: MutableList<UserPicture> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_picture_gallery)

        val layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 2)
        recyclerView = findViewById(R.id.rv_images)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        addButton = findViewById(R.id.add_image_button)
        addButton.setOnClickListener {pickFromGallery()}
        imageGalleryAdapter = UserPictureGalleryAdapter(this, userPictures)
        getUserPictures()
    }

    private fun getUserPictures(pageRequest: PageRequest = PageRequest()) {
        executorService.submit {
            try {
                val retrievedPictures = userPictureProvider.get(pageRequest.page, pageRequest.size)
                this.userPictures.removeAll { true }
                this.userPictures.addAll(retrievedPictures)
                runOnUiThread {this.imageGalleryAdapter.notifyDataSetChanged()}

            } catch(e : Exception) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        recyclerView.adapter = imageGalleryAdapter
    }

    private fun pickFromGallery() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                println("Show explaination")
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.

                //Create an Intent with action as ACTION_PICK
                val intent = Intent(Intent.ACTION_PICK)
                // Sets the type as image/*. This ensures only components of type image are selected
                intent.type = "image/*"
                //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
                val mimeTypes = arrayOf("image/jpeg", "image/png")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                // Launching the Intent
                startActivityForResult(intent, GALLERY_REQUEST_CODE)
            }
        } else {
            // Permission has already been granted
            //Create an Intent with action as ACTION_PICK
            val intent = Intent(Intent.ACTION_PICK)
            // Sets the type as image/*. This ensures only components of type image are selected
            intent.type = "image/*"
            //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            // Launching the Intent
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    //data.getData return the content URI for the selected Image
                    val selectedImage = data!!.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    // Get the cursor
                    val cursor = contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
                    // Move to first row
                    cursor!!.moveToFirst()
                    //Get the column index of MediaStore.Images.Media.DATA
                    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                    //Gets the String value in the column
                    val imgDecodableString = cursor.getString(columnIndex)
                    cursor.close()
                    // Set the Image in ImageView after decoding the String
                    //imageView.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString))
                    val intent = Intent(applicationContext, UploadActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    intent.putExtra(getString(R.string.upload_file_path), imgDecodableString)
                    startActivity(intent)
                }
                USER_PICTURE_REQUEST_CODE -> {
                    if(resultCode == Activity.RESULT_OK) {
                        if(data!!.getBooleanExtra(DELETED_RESULT, false)) {
                            val element = data.getParcelableExtra<UserPicture>(USER_PICTURE_EXTRA)
                            userPictures.remove(element)
                            runOnUiThread {
                                this.imageGalleryAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
    }

    companion object {
        private const val GALLERY_REQUEST_CODE: Int = 100
        const val USER_PICTURE_REQUEST_CODE: Int = 200
    }

}
