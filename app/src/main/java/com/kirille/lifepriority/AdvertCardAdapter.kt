package com.kirille.lifepriority

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*
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
            val photosIcon2: ImageView = itemView.findViewById(R.id.photosIcon2)
            val advertName: TextView = itemView.findViewById(R.id.advertName)
            val advertText: TextView = itemView.findViewById(R.id.advertText)
            val advertDate: TextView = itemView.findViewById(R.id.advertDate)
//            val newAdvert: TextView = itemView.findViewById(R.id.newAdvert)
            val district: TextView = itemView.findViewById(R.id.district)
            val price: TextView = itemView.findViewById(R.id.price)
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


    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            // TODO Add handler for images in recyclerview - https://bumptech.github.io/glide/int/recyclerview.html
            holder.favImage

            val view = holder.cardView
            val context = view.context
            val item = items[holder.bindingAdapterPosition]

            holder.advertName.text = item.name
            holder.advertDate.text = getDateString(item.date, context.resources)

            if (item.description.isEmpty()) {
                holder.advertText.text = context.resources.getString(R.string.empty_description)
            } else {
                holder.advertText.text = item.description
            }

            if (item.district.isEmpty()){
                holder.district.visibility = Button.GONE
            } else {
                holder.district.text = item.district
                holder.district.visibility = Button.VISIBLE
            }

            if (item.price > 0) {

                val priceString = try {
                    NumberFormat.getInstance(Locale.FRANCE).format(item.price)
                } catch (e: IllegalArgumentException){
                    item.price.toString()
                }

                holder.price.text = context.resources.getString(
                        R.string.currency,
                    priceString
                )
                holder.price.visibility = View.VISIBLE
            } else {
                holder.price.visibility = View.GONE
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

                if (photos.length() > 1) {
                    val photoUrl2 = try {
                        (photos[1] as JSONObject).getString("small")
                    } catch (e: JSONException) {
                        throw RuntimeException(e)
                    }
                    Glide
                            .with(context)
                            .load(photoUrl2)
                            .thumbnail(0.1f)
                            .apply(RequestOptions().placeholder(ColorDrawable(Color.parseColor("#edeef0"))))
                            .into(holder.photosIcon2)

                    holder.photosIcon2.visibility = View.VISIBLE
                } else {
                    holder.photosIcon2.visibility = View.GONE
                }

            } else {
                holder.photosIcon.visibility = View.GONE
                holder.photosIcon2.visibility = View.GONE
            }


            val isFavorite = try {
                GetFavorite(context, item.postId).execute().get()
            } catch (e: InterruptedException) {
                false
            } catch (e: ExecutionException) {
                false
            }


            if (isFavorite) {
                holder.favImage.setImageResource(R.drawable.outline_favorite_black_36)
            } else {
                holder.favImage.setImageResource(R.drawable.outline_favorite_border_black_36)
            }

            holder.favImage.setOnClickListener {
                val newFavorite = !isFavorite
                if (newFavorite) {
                    AddFavoriteTask(context!!, item).execute()
                } else {
                    DeleteFavoriteTask(context!!, item.postId).execute()
                }

                item.isFavorite = !newFavorite
                this.notifyDataSetChanged()
            }

            item.isFavorite = isFavorite

            if (item.isSelected) {
                view.setCardBackgroundColor(Color.parseColor("#ABCAE8"))
            } else {
                view.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            }


            view.setOnClickListener {
                if (clickListener != null) {
                    clickListener?.onClick(item, holder.bindingAdapterPosition)
                }
            }

            view.setOnLongClickListener {
                if (longClickListener != null) {
                    longClickListener?.onLongClick(item, holder.bindingAdapterPosition, holder)
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