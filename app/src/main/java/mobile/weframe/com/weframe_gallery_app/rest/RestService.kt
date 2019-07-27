package mobile.weframe.com.weframe_gallery_app.rest

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
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
import java.nio.charset.Charset
import java.util.*

import com.fasterxml.jackson.module.kotlin.*

class RestService private constructor() {

    companion object {
        val SERVER_URL = "http://kotlin-resources-server.sa-east-1.elasticbeanstalk.com"
        val instance = RestService()
    }

    val restTemplate: RestTemplate
    var authHeader: String? = null

    init {
        val formHttpMessageConverter = FormHttpMessageConverter()
        formHttpMessageConverter.setCharset(Charset.forName("UTF8"))

        this.restTemplate = RestTemplate()
        this.restTemplate.messageConverters.add(formHttpMessageConverter)
        this.restTemplate.messageConverters.add(MappingJackson2HttpMessageConverter())
        this.restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory()
    }

    fun processLogin(authToken: String) {
        this.authHeader =  "Bearer $authToken"
    }

    fun request(url: String, method: HttpMethod, entity: HttpEntity<out Any>) : ResponseEntity<JsonNode> {
        if(authHeader == null) {
            throw NotLoggedInException()
        }
        val headers = HttpHeaders()
        headers.putAll(entity.headers)
        headers["Authorization"] = listOf(authHeader)
        val requestEntity = HttpEntity(entity.body, headers)
        try {
            return this.restTemplate.exchange("$SERVER_URL$url", method, requestEntity, JsonNode::class.java)
        } catch(e: HttpClientErrorException) {
            this.authHeader = null
            throw NotLoggedInException(e)
        }
    }

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
        val pictureResponse = RestService.instance.request("/pictures", HttpMethod.POST, imageEntity)
        if(pictureResponse.statusCode == HttpStatus.OK || pictureResponse.statusCode == HttpStatus.CREATED) {
            val picture = jacksonObjectMapper().convertValue<Picture>(pictureResponse.body, object : TypeReference<Picture>(){})
            val userImageEntity = HttpEntity<Picture>(picture)
            val userPictureResponse = RestService.instance.request("/user-pictures", HttpMethod.POST, userImageEntity)
            val userPicture = jacksonObjectMapper().convertValue<UserPicture>(userPictureResponse.body, object : TypeReference<UserPicture>(){})
            return ResponseEntity(userPicture, userPictureResponse.statusCode)
        } else {
            throw UploadFailException("An unexpected error occurred while trying to upload the user picture. Status:${pictureResponse.statusCode}")
        }
    }

    fun get(page: Long = 0, size: Long = 10): ResponseEntity<UserPicturePagedResponse> {
        val headers = HttpHeaders()
        headers.add("page", page.toString())
        headers.add("size", size.toString())
        val entity = HttpEntity<Any>(null, headers)
        val response = RestService.instance.request("/user-pictures?page=$page&size=$size", HttpMethod.GET, entity)
        val page = jacksonObjectMapper().convertValue<Page>(response.body.get("page"), object : TypeReference<Page>() {})
        return if(response.body.has("_embedded")) {
            val userPictures = jacksonObjectMapper().convertValue<List<UserPicture>>(response.body.get("_embedded").get("userPictures"), object : TypeReference<List<UserPicture>>() {})
            ResponseEntity(UserPicturePagedResponse(page, userPictures), response.statusCode)
        } else {
            ResponseEntity(UserPicturePagedResponse(page, emptyList()), response.statusCode)
        }
    }
}

class UploadFailException(message: String?) : Exception(message)
class NotLoggedInException : Exception {
    constructor()
    constructor(cause: Throwable?) : super(cause)
}