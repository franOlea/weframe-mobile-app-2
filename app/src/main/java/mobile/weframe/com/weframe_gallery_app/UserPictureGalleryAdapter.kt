package mobile.weframe.com.weframe_gallery_app

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

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
            .load(userPicture.url)
//            .placeholder(R.drawable.placeholder)
//            .error(R.drawable.error)
            .fit()
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .networkPolicy(NetworkPolicy.NO_CACHE)
            .into(imageView)
    }

    override fun getItemCount(): Int {
        return userPictures.size
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