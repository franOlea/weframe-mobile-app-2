package mobile.weframe.com.weframe_gallery_app.rest

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserPicturePagedResponse(val page: Page, val userPictures: List<UserPicture>) : Parcelable

@Parcelize
class Page(val number: Long, val totalPages: Long, val size: Long, val totalElements: Long) : Parcelable

@Parcelize
class UserPicture(val id: Long, val picture: Picture, val user: String) : Parcelable

@Parcelize
class Picture(val id: Long, val name: String, val key: String, val url: String?) : Parcelable