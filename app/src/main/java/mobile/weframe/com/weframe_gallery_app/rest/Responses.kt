package mobile.weframe.com.weframe_gallery_app.rest

import com.fasterxml.jackson.annotation.JsonProperty
import mobile.weframe.com.weframe_gallery_app.gallery.UserPicture

class UserPicturePagedResponse(val page: Page, @JsonProperty("_embedded.userPictures") val userPictures: List<UserPicture>)

class Page(val pageNumber: Long, val totalPages: Long, val size: Long, val totalElements: Long)

class UserPicture(val id: Long, val picture: Picture, val user: String)

class Picture(val id: Long, val name: String, val key: String, val url: String)