package com.kirille.lifepriority

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.concurrent.ExecutionException

class AdvertCardAdapter(private var items: ArrayList<AdvertItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var clickListener: ClickListener? = null
    private var longClickListener: LongClickListener? = null

    private var viewTypeItem = 0
    private var viewTypeLoading = 1


    interface ClickListener {
        fun onClick(item: AdvertItem, position: Int)
    }

    interface LongClickListener {
        fun onLongClick(item: AdvertItem, position: Int, holder: RecyclerView.ViewHolder)
    }

    class ItemViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView) {
            val favImage: ImageView = itemView.findViewById(R.id.favImage)
            val photosIcon: ImageView = itemView.findViewById(R.id.photosIcon)
            val advertName: TextView = itemView.findViewById(R.id.advertName)
            val advertText: TextView = itemView.findViewById(R.id.advertText)
            val advertDate: TextView = itemView.findViewById(R.id.advertDate)
            val newAdvert: TextView = itemView.findViewById(R.id.newAdvert)
    }

    inner class ViewHolderLoading(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == viewTypeItem) {
            val cardView = LayoutInflater.from(parent.context).inflate(
                    R.layout.card_captioned_image,
                    parent,
                    false) as CardView
            ItemViewHolder(cardView)
        } else {
            val cardView = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_loading,
                    parent,
                    false)
            ViewHolderLoading(cardView)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            // TODO Add handler for images in recyclerview - https://bumptech.github.io/glide/int/recyclerview.html
            holder.favImage

            val view = holder.cardView
            val context = view.context
            val item = items[holder.adapterPosition]

            holder.advertName.text = item.name
            holder.advertDate.text = getDateString(item.date, context.resources)

            if (item.description.isEmpty()) {
                holder.advertText.text = context.resources.getString(R.string.empty_description)
            } else {
                holder.advertText.text = item.description
            }

            val photos = try {
                JSONArray(item.photos)
            } catch (e: JSONException) {
                JSONArray()
            }

            if (photos.length() > 0) {
                val photoUrl = try {
                    (photos[0] as JSONObject).getString("small")
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                Glide
                        .with(context)
                        .load(photoUrl)
                        .thumbnail(0.1f)
                        .apply(RequestOptions().placeholder(ColorDrawable(Color.parseColor("#edeef0"))))
                        .into(holder.photosIcon)

                holder.photosIcon.visibility = View.VISIBLE
            } else {
                holder.photosIcon.visibility = View.GONE
            }


            val isFavorite = try {
                GetFavorite(context, item.postId).execute().get()
            } catch (e: InterruptedException) {
                false
            } catch (e: ExecutionException) {
                false
            }

            if (isFavorite) {
                holder.favImage.visibility = View.VISIBLE
            } else {
                holder.favImage.visibility = View.GONE
            }

            item.isFavorite = isFavorite

            if (item.isSelected) {
                view.setCardBackgroundColor(Color.parseColor("#ABCAE8"))
            } else {
                view.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            }

            if (item.isNew) {
                holder.newAdvert.visibility = TextView.VISIBLE
            } else {
                holder.newAdvert.visibility = TextView.GONE
            }


            view.setOnClickListener {
                if (clickListener != null) {
                    clickListener?.onClick(item, holder.adapterPosition)
                }
            }

            view.setOnLongClickListener {
                if (longClickListener != null) {
                    longClickListener?.onLongClick(item, holder.adapterPosition, holder)
                }
                true
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].postId == -1) viewTypeLoading else viewTypeItem
    }

    override fun getItemCount(): Int = items.size

    fun setClickListener(_clickLister: ClickListener) {
        clickListener = _clickLister
    }

    fun setLongClickListener(_longClickListener: LongClickListener) {
        longClickListener = _longClickListener
    }

}