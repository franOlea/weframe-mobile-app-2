package mobile.weframe.com.weframe_gallery_app.gallery.provider

import mobile.weframe.com.weframe_gallery_app.gallery.UserPicture
import mobile.weframe.com.weframe_gallery_app.gallery.UserPictureProvider
import mobile.weframe.com.weframe_gallery_app.rest.UserPictureService
import android.os.AsyncTask



class RestUserPictureProvider : UserPictureProvider {

    private val userPictureService = UserPictureService()

    override fun get(page: Long, size: Long): List<UserPicture> {
        return userPictureService.get(page, size).body.userPictures
    }
}

data class PageRequest(val page: Long = 0, val size: Long = 10)

class DownloadFilesTask(private val provider: UserPictureProvider) : AsyncTask<PageRequest, Int, List<UserPicture>>() {

    override fun doInBackground(vararg request: PageRequest): List<UserPicture>? {
        return provider.get(request[0].page, request[0].size)
    }

}