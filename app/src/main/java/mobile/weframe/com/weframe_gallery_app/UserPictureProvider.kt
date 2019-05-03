package mobile.weframe.com.weframe_gallery_app

interface UserPictureProvider {
    fun get() : List<UserPicture>
}