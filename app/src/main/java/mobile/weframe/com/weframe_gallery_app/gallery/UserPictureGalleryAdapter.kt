package mobile.weframe.com.weframe_gallery_app.gallery

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import mobile.weframe.com.weframe_gallery_app.R
import mobile.weframe.com.weframe_gallery_app.gallery.detail.UserPictureActivity
import mobile.weframe.com.weframe_gallery_app.rest.UserPicture

class UserPictureGalleryAdapter(val context: Context, val userPictures: List<UserPicture>)
    : RecyclerView.Adapter<UserPictureGalleryAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val photoView = inflater.inflate(R.layout.user_picture, parent, false)
        return MyViewHolder(photoView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val userPicture = userPictures[position]
        val imageView = holder.photoImageView

        Picasso.get()
            .load(userPicture.picture.url)
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.error)
            .fit().centerCrop()
//            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
//            .networkPolicy(NetworkPolicy.NO_CACHE)
            .into(imageView)
    }

    override fun getItemCount(): Int {
        return userPictures.size
    }

    inner class ThumbnailTransformation(val maxHeight: Int, val maxWidth: Int) : Transformation {

        override fun key(): String {
            return "thumbnailTransformation-$maxHeight-$maxWidth";
        }

        override fun transform(source: Bitmap?): Bitmap {
            val aspectRatio = source!!.height.toDouble() / source.width.toDouble()
            val targetHeight = (maxWidth * aspectRatio).toInt()
            val result = Bitmap.createScaledBitmap(source, maxWidth, targetHeight, false)
            if (result != source) {
                // Same bitmap is returned if sizes are the same
                source.recycle()
            }
            return result
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var photoImageView: ImageView = itemView.findViewById(R.id.iv_photo)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val userPicture = userPictures[position]
                val intent = Intent(context, UserPictureActivity::class.java).apply {
                    putExtra(UserPictureActivity.EXTRA_USER_PICTURE, userPicture)
                }
                context.startActivity(intent)
            }
        }
    }
}