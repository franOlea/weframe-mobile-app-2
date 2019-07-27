package mobile.weframe.com.weframe_gallery_app.rest

import org.springframework.core.io.Resource
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.http.HttpOutputMessage
import org.springframework.http.converter.ResourceHttpMessageConverter
import java.io.IOException
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.FormHttpMessageConverter

class ProgressTracker: FileUploadProgressTracker {

    var progressListener: FileUploadProgressTracker? = null

    override fun trackProgress(progress: Long) {
        progressListener?.trackProgress(progress)
    }
}

interface FileUploadProgressTracker {

    fun trackProgress(progress: Long)
}

class CallbackFormHttpMessageConverter(callback: FileUploadProgressTracker) : FormHttpMessageConverter() {

    init {

        val partConverters = ArrayList<HttpMessageConverter<out Any>>()
        partConverters.add(CallbackResourceHttpMessageConverter(callback))

        // Continue to add in default for FormHttpMessageConverter.
        partConverters.add(ByteArrayHttpMessageConverter())
        val stringHttpMessageConverter = StringHttpMessageConverter()
        stringHttpMessageConverter.setWriteAcceptCharset(false)
        partConverters.add(stringHttpMessageConverter)
        partConverters.add(ResourceHttpMessageConverter())
        setPartConverters(partConverters)
    }

}

class CallbackResourceHttpMessageConverter(private val callback: FileUploadProgressTracker) :
    ResourceHttpMessageConverter() {

    @Throws(IOException::class, HttpMessageNotWritableException::class)
    override fun writeInternal(resource: Resource, outputMessage: HttpOutputMessage) {

        val `in` = resource.inputStream
        try {
            val fileSize = resource.contentLength()
            val buffer = ByteArray(4096)

            var count: Long = 0
            var n: Int
            while(true) {
                n = `in`.read(buffer)
                if(n == -1) {
                    break
                }
                outputMessage.body.write(buffer, 0, n)
                count += n.toLong()
                val percentage = ((count.toDouble() - fileSize) / fileSize * 100 + 100).toLong()
                callback.trackProgress(percentage)
            }
        } finally {
            try {
                `in`.close()
            } catch (ex: IOException) {
            }

        }
        outputMessage.body.flush()
    }

}
