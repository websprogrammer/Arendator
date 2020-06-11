package com.kirille.lifepriority

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.json.JSONArray
import org.json.JSONException


class GalleryAdapter(mContext: Context, _images: JSONArray) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {
    private val images: JSONArray = _images
    private val context: Context = mContext
    private val adapterTag: String = "GalleryAdapter"
    private var galleryListener: GalleryListener? = null


    interface GalleryListener {
        fun onClick(position: Int)
    }


    class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.adPhoto)
    }


    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val photoView = LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)
        return GalleryViewHolder(photoView)

    }


    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val photoUrl: String
        try {
            photoUrl = images.getJSONObject(position).getString("small")
        } catch (e: JSONException) {
            Log.i(adapterTag, "JSONException $e")
            throw RuntimeException(e)
        }

        Glide
            .with(context)
            .load(photoUrl)
            .thumbnail(0.1f)
            .apply(RequestOptions().placeholder(ColorDrawable(Color.parseColor("#edeef0"))))
            .into(holder.thumbnail)
        
        holder.thumbnail.setOnClickListener {
            val node = galleryListener
            node?.onClick(holder.adapterPosition)
        }

    }


    override fun getItemCount(): Int = images.length()

    fun setGalleryListener(listener: GalleryListener) {
        galleryListener = listener
    }

}