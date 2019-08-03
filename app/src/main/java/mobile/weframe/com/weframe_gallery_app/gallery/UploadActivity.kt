package mobile.weframe.com.weframe_gallery_app.gallery

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import mobile.weframe.com.weframe_gallery_app.LoginActivity
import mobile.weframe.com.weframe_gallery_app.R
import mobile.weframe.com.weframe_gallery_app.gallery.provider.RestUserPictureProvider
import mobile.weframe.com.weframe_gallery_app.rest.FileUploadProgressTracker
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class UploadActivity : AppCompatActivity() {
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private val userPictureProvider = RestUserPictureProvider()
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        progressBar = findViewById(R.id.progress_bar)
        val filePath = intent.getSerializableExtra(getString(R.string.upload_file_path)) as String
        postPicture(File(filePath))
    }

    private fun startActivity(clazz: Class<out Any>) {
        val intent = Intent(applicationContext, clazz)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    private val progressTracker = object : FileUploadProgressTracker {

        override fun trackProgress(progress: Long) {
            if(progress in 1..99) {
                runOnUiThread {
                    progressBar.progress = progress.toInt()
                }
            }
        }
    }

    private fun postPicture(file: File) {
        executorService.submit {
            try {
                val rotatedFile = rotatePictureFileToCorrectOrientation(file)
                userPictureProvider.upload(rotatedFile, progressTracker)
                startActivity(UserPictureGalleryActivity::class.java)
            } catch(e : Exception) {
                startActivity(LoginActivity::class.java)
            }
        }
    }

    private fun rotatePictureFileToCorrectOrientation(file: File): File {
        val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> {
            }
        }
        val resultingBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
        originalBitmap.recycle()
        val resultingFile = saveBitmapToTempFile(resultingBitmap)
        resultingBitmap.recycle()
        return resultingFile
    }

    private fun saveBitmapToTempFile(imageBitmap: Bitmap): File {
        val file = File(this.cacheDir, "uploadTmp")
        file.delete()
        file.createNewFile()

        //Convert bitmap to byte array
        val byteArrayOutputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        //write the bytes in file
        val fileOutputStream = FileOutputStream(file)
        byteArrayOutputStream.writeTo(fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        byteArrayOutputStream.flush()
        byteArrayOutputStream.close()
        return file
    }


}
