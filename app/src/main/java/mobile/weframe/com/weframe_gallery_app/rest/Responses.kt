package mobile.weframe.com.weframe_gallery_app.rest

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserPicturePagedResponse(val page: Page, val userPictures: List<UserPicture>) : Parcelable

@Parcelize
class Page(val number: Long, val totalPages: Long, val size: Long, val totalElements: Long) : Parcelable

@Parcelize
class UserPicture(val id: Long, val picture: Picture, val user: String) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserPicture

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

@Parcelize
class Picture(val id: Long, val name: String, val key: String, val url: String?) : Parcelable