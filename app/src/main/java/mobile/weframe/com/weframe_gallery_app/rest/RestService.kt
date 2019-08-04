package mobile.weframe.com.weframe_gallery_app.rest

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.core.io.FileSystemResource
import org.springframework.http.*
import org.springframework.http.MediaType.MULTIPART_FORM_DATA
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
import org.springframework.http.client.SimpleClientHttpRequestFactory

class RestService private constructor() {

    companion object {
        const val SERVER_URL = "http://kotlin-resources-server.sa-east-1.elasticbeanstalk.com"
        val instance = RestService()
    }

    private val restTemplate: RestTemplate
    private var authHeader: String? = null
    private var progressTracker : ProgressTracker = ProgressTracker()

    init {
        val formHttpMessageConverter = FormHttpMessageConverter()
        formHttpMessageConverter.setCharset(Charset.forName("UTF8"))
        val httpRequestFactory = SimpleClientHttpRequestFactory()
        httpRequestFactory.setBufferRequestBody(false)
        val progressConverter = CallbackFormHttpMessageConverter(progressTracker)

        this.restTemplate = RestTemplate()
//        this.restTemplate.messageConverters.add(formHttpMessageConverter)
        this.restTemplate.messageConverters.add(0, progressConverter)
        this.restTemplate.messageConverters.add(MappingJackson2HttpMessageConverter())
        this.restTemplate.requestFactory = httpRequestFactory
    }

    fun processLogin(authToken: String) {
        this.authHeader =  "Bearer $authToken"
    }

    fun logout() {
        this.authHeader = ""
    }

    fun request(url: String, method: HttpMethod,
                entity: HttpEntity<out Any>,
                tracker: FileUploadProgressTracker = NullProgressTracker()) : ResponseEntity<JsonNode> {
        if(authHeader == null) {
            throw NotLoggedInException()
        }
        progressTracker.progressListener = tracker
        val headers = HttpHeaders()
        headers.putAll(entity.headers)
        headers["Authorization"] = listOf(authHeader)
        val requestEntity = HttpEntity(entity.body, headers)
        try {
            val response = this.restTemplate.exchange("$SERVER_URL$url", method, requestEntity, JsonNode::class.java)
            progressTracker.progressListener = null
            return response
        } catch(e: HttpClientErrorException) {
            this.authHeader = null
            throw NotLoggedInException(e)
        }
    }

}

class UserPictureService {

    fun upload(file: File, tracker: FileUploadProgressTracker = NullProgressTracker()): ResponseEntity<UserPicture> {
        val map = LinkedMultiValueMap<String, Any>()
        map.add("file", FileSystemResource(file.absoluteFile))
        map.add("name", UUID.randomUUID().toString())
        map.add("formatName", "jpg")
        val headers = HttpHeaders()
        headers.contentType = MULTIPART_FORM_DATA
        val imageEntity = HttpEntity<MultiValueMap<String, Any>>(map, headers)
        val pictureResponse = RestService.instance.request("/pictures", HttpMethod.POST, imageEntity, tracker)
        if(pictureResponse.statusCode == HttpStatus.OK || pictureResponse.statusCode == HttpStatus.CREATED) {
            val picture = jacksonObjectMapper().convertValue<Picture>(pictureResponse.body, object : TypeReference<Picture>(){})
            val userImageEntity = HttpEntity<Picture>(picture)
            val userPictureResponse = RestService.instance.request("/user-pictures", HttpMethod.POST, userImageEntity)
            val userPicture = jacksonObjectMapper().convertValue<UserPicture>(userPictureResponse.body, object : TypeReference<UserPicture>(){})
            return ResponseEntity(userPicture, userPictureResponse.statusCode)
        } else {
            throw OperationFailException("An unexpected error occurred while trying to upload the user picture. " +
                    "Status:${pictureResponse.statusCode}")
        }
    }

    fun get(page: Long = 0, size: Long = 10): ResponseEntity<UserPicturePagedResponse> {
        val headers = HttpHeaders()
        headers.add("page", page.toString())
        headers.add("size", size.toString())
        val entity = HttpEntity<Any>(null, headers)
        val response = RestService.instance.request("/user-pictures?page=$page&size=$size", HttpMethod.GET, entity)
        val responsePage = jacksonObjectMapper().convertValue<Page>(response.body.get("page"), object : TypeReference<Page>() {})
        return if(response.body.has("_embedded")) {
            val userPictures = jacksonObjectMapper().convertValue<List<UserPicture>>(
                response.body.get("_embedded").get("userPictures"), object : TypeReference<List<UserPicture>>() {})
            ResponseEntity(UserPicturePagedResponse(responsePage, userPictures), response.statusCode)
        } else {
            ResponseEntity(UserPicturePagedResponse(responsePage, emptyList()), response.statusCode)
        }
    }

    fun delete(id: Long) {
        val response = RestService.instance
            .request("/user-pictures/$id", HttpMethod.DELETE, HttpEntity.EMPTY)
        if(response.statusCode != HttpStatus.OK) {
            if(response.statusCode == HttpStatus.UNAUTHORIZED || response.statusCode == HttpStatus.FORBIDDEN) {
                throw NotLoggedInException()
            } else {
                throw OperationFailException("An unexpected error ocurred. Could not delete picture. " +
                        "Status: ${response.statusCode}")
            }
        }
    }
}

class NullProgressTracker: FileUploadProgressTracker {

    override fun trackProgress(progress: Long) {

    }
}

class OperationFailException(message: String?) : Exception(message)
class NotLoggedInException : Exception {
    constructor()
    constructor(cause: Throwable?) : super(cause)
}