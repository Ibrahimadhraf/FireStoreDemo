package com.ibrahim.firestoredemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_item.view.*

class ImageAdapter(
    val urls:List<String>
):RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.list_item ,parent ,false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
          val url=urls[position]
        Glide.with(holder.itemView).load(url).into(holder.itemView.ivImage)
    }

    override fun getItemCount(): Int {
      return urls.size
    }
    inner class ImageViewHolder( itemView: View):
        RecyclerView.ViewHolder(itemView) {}
}