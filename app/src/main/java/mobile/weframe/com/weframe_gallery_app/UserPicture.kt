package mobile.weframe.com.weframe_gallery_app

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserPicture(val url: String) : Parcelable