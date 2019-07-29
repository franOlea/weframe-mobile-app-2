package mobile.weframe.com.weframe_gallery_app.gallery

import mobile.weframe.com.weframe_gallery_app.rest.FileUploadProgressTracker
import mobile.weframe.com.weframe_gallery_app.rest.UserPicture
import java.io.File

interface UserPictureProvider {
    fun get(page: Long = 0, size: Long = 10) : List<UserPicture>
    fun upload(file: File, tracker: FileUploadProgressTracker): UserPicture
    fun delete(id: Long)
}