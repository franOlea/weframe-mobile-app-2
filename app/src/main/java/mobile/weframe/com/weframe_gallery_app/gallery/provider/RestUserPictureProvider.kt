package mobile.weframe.com.weframe_gallery_app.gallery.provider

import mobile.weframe.com.weframe_gallery_app.gallery.UserPictureProvider
import mobile.weframe.com.weframe_gallery_app.rest.UserPictureService
import mobile.weframe.com.weframe_gallery_app.rest.UserPicture
import java.io.File


class RestUserPictureProvider : UserPictureProvider {

    private val userPictureService = UserPictureService()

    override fun get(page: Long, size: Long): List<UserPicture> {
        return userPictureService.get(page, size).body.userPictures
    }

    override fun upload(file: File) : UserPicture {
        return userPictureService.upload(file).body
    }
}

data class PageRequest(val page: Long = 0, val size: Long = 10)