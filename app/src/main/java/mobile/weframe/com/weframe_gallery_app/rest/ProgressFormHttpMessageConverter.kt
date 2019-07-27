package mobile.weframe.com.weframe_gallery_app.rest

import org.springframework.core.io.Resource
import org.springframework.http.HttpOutputMessage
import org.springframework.http.converter.*
import java.io.IOException
import java.util.*

class ProgressFormHttpMessageConverter(private val progressTracker: ProgressTracker) : FormHttpMessageConverter() {

    init {
        val partConverters = ArrayList<HttpMessageConverter<*>>()
        partConverters.add(ByteArrayHttpMessageConverter())
        val stringHttpMessageConverter = StringHttpMessageConverter()
        stringHttpMessageConverter.setWriteAcceptCharset(false)
//        partConverters.add(stringHttpMessageConverter)
        partConverters.add(ProgressResourceHttpMessageConverter())
        setPartConverters(partConverters)
    }

    internal inner class ProgressResourceHttpMessageConverter : ResourceHttpMessageConverter() {

        @Throws(IOException::class, HttpMessageNotWritableException::class)
        override fun writeInternal(resource: Resource, outputMessage: HttpOutputMessage) {
            val inputStream = resource.inputStream
            val outputStream = outputMessage.body

            val buffer = ByteArray(4096)
            val contentLength = resource.contentLength()
            var byteCount = 0
            var bytesRead: Int
            while(true) {
                bytesRead = inputStream.read(buffer)
                if(bytesRead == -1) {
                    break
                }
                outputStream.write(buffer, 0, bytesRead)
                byteCount += bytesRead
                val percentage = ((byteCount.toDouble() - contentLength) / contentLength * 100 + 100).toLong()
                progressTracker.trackProgress(percentage)
            }
            outputStream.flush()
        }
    }
}