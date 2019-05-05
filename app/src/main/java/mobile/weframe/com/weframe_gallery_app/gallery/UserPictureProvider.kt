package mobile.weframe.com.weframe_gallery_app.gallery

interface UserPictureProvider {
    fun get(page: Long = 0, size: Long = 10) : List<UserPicture>
}