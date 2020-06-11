package com.kirille.lifepriority

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.TextView


class KeyWordsAdapter(private var layoutId: Int, private var keyWords: List<String>) : RecyclerView.Adapter<KeyWordsAdapter.ViewHolder>() {
    private var longClickListener: LongClickListener? = null

    interface LongClickListener {
        fun onLongClick(item: String, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = keyWords.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var item: TextView = itemView.findViewById(R.id.key_word_item)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = holder.item

        item.setOnLongClickListener {
            longClickListener?.onLongClick(keyWords[position], holder.adapterPosition)
            true
        }
        item.text = keyWords[position]
    }

    fun setLongClickListener(_longClickListener: LongClickListener) {
        longClickListener = _longClickListener
    }

}