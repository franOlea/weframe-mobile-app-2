package mobile.weframe.com.weframe_gallery_app.rest

import org.springframework.core.io.FileSystemResource
import org.springframework.http.*
import org.springframework.http.MediaType.MULTIPART_FORM_DATA
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.io.File
import java.lang.Exception
import java.lang.IllegalStateException

import java.nio.charset.Charset
import java.util.*

class RestService private constructor() {

    companion object {
        val SERVER_URL = "http://weframers-franolea.rhcloud.com"
        val instance = RestService()
    }

    val restTemplate: RestTemplate
    var authHeader: HttpAuthentication? = null

    init {
        val formHttpMessageConverter = FormHttpMessageConverter()
        formHttpMessageConverter.setCharset(Charset.forName("UTF8"))

        this.restTemplate = RestTemplate()
        this.restTemplate.messageConverters.add(formHttpMessageConverter)
        this.restTemplate.messageConverters.add(MappingJackson2HttpMessageConverter())
        this.restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory()
    }

    fun processLogin(authToken: String) {
        this.authHeader =  object: HttpAuthentication() {
            override fun getHeaderValue(): String {
                return "Bearer $authToken"
            }
        }
    }

    fun <T> request(url: String, method: HttpMethod, entity: HttpEntity<out Any>, responseClass: Class<T>) : ResponseEntity<T> {
        if(authHeader == null) {
            throw IllegalStateException("The user is not logged in.")
        }
        entity.headers.setAuthorization(authHeader)
        try {
            return this.restTemplate.exchange("$SERVER_URL$url", method, entity, responseClass)
        } catch(e: HttpClientErrorException) {
            this.authHeader = null
            throw IllegalStateException("The user token is invalid or expired.")
        }
    }

//    fun <T> request(url: String, method: HttpMethod, responseClass: Class<T>) : ResponseEntity<T> {
//        if(authHeader == null) {
//            throw IllegalStateException("The user is not logged in.")
//        }
//        entity.headers.setAuthorization(authHeader)
//        try {
//            this.restTemplate.exchange("$SERVER_URL$url", method, entity, responseClass)
//        } catch(e: HttpClientErrorException) {
//            this.authHeader = null
//            throw IllegalStateException("The user token is invalid or expired.")
//        }
//    }

}

class UserPictureService {

    fun upload(file: File): ResponseEntity<UserPicture> {
        val map = LinkedMultiValueMap<String, Any>()
        map.add("file", FileSystemResource(file.absoluteFile))
        map.add("name", UUID.randomUUID().toString())
        map.add("formatName", "jpg")
        val headers = HttpHeaders()
        headers.contentType = MULTIPART_FORM_DATA
        val imageEntity = HttpEntity<MultiValueMap<String, Any>>(map, headers)
        val picture = RestService.instance
            .request("/pictures", HttpMethod.POST, imageEntity, Picture::class.java)
        if(picture.statusCode == HttpStatus.OK || picture.statusCode == HttpStatus.CREATED) {
            val userImageEntity = HttpEntity<Picture>(picture.body)
            return RestService.instance
                .request("/user-pictures", HttpMethod.POST, userImageEntity, UserPicture::class.java)
        } else {
            throw UploadFailException("An unexpected error occurred while trying to upload the user picture. Status:${picture.statusCode}")
        }
    }

    fun get(page: Long = 0, size: Long = 10): ResponseEntity<UserPicturePagedResponse> {
        val headers = HttpHeaders()
        headers.add("page", page.toString())
        headers.add("size", size.toString())
        val entity = HttpEntity<Any>(null, headers)
        return RestService.instance
            .request("/user-pictures", HttpMethod.GET, entity, UserPicturePagedResponse::class.java)
    }

}

class UploadFailException(message: String?) : Exception(message)