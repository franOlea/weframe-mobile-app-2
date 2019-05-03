package mobile.weframe.com.weframe_gallery_app

class InMemoryUserPictureProvider : UserPictureProvider {

    override fun get(): List<UserPicture> {
        return listOf(UserPicture("https://goo.gl/32YN2B"),
            UserPicture("https://goo.gl/Wqz4Ev"),
            UserPicture("https://goo.gl/U7XXdF"),
            UserPicture("https://goo.gl/ghVPFq"),
            UserPicture("https://goo.gl/qEaCWe"),
            UserPicture("https://goo.gl/vutGmM"))
    }
}