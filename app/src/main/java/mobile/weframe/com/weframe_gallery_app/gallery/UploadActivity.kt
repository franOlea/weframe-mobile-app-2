package mobile.weframe.com.weframe_gallery_app.gallery

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import mobile.weframe.com.weframe_gallery_app.LoginActivity
import mobile.weframe.com.weframe_gallery_app.R
import mobile.weframe.com.weframe_gallery_app.gallery.provider.RestUserPictureProvider
import mobile.weframe.com.weframe_gallery_app.rest.FileUploadProgressTracker
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.log

class UploadActivity : AppCompatActivity() {
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private val userPictureProvider = RestUserPictureProvider()
    private lateinit var progressCounter: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        progressCounter = findViewById(R.id.progress_counter)
        val filePath = intent.getSerializableExtra(getString(R.string.upload_file_path)) as String
        postPicture(File(filePath))
    }

    private fun postPicture(file: File) {
        executorService.submit {
            try {
                userPictureProvider.upload(file, progressTracker)
                startActivity(UserPictureGalleryActivity::class.java)
            } catch(e : Exception) {
                startActivity(LoginActivity::class.java)
            }
        }
    }

    private fun startActivity(clazz: Class<out Any>) {
        val intent = Intent(applicationContext, clazz)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    private val progressTracker = object : FileUploadProgressTracker {

        @SuppressLint("SetTextI18n")
        override fun trackProgress(progress: Long) {
            runOnUiThread {
                progressCounter.text = "Progress: $progress"
            }
        }
    }


}
