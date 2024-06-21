package com.example.homework3.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.homework3.R
import com.example.homework3.data.NewsItem
import com.example.homework3.databinding.FirstMaterialNewsItemViewBinding
import com.example.homework3.databinding.MaterialNewsItemViewBinding

class NewsItemListAdapter(
    var items: List<NewsItem>,
    private val context: Context
) : RecyclerView.Adapter<NewsItemListAdapter.ItemViewHolder>() {

    private val LOG_TAG = "NewsItemListAdapter"
    var itemClickListener: ((NewsItem) -> Unit)? = null

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        Log.e(LOG_TAG, "ON CREATE VIEWHOLDER")

        // Inflate different layouts based on view type
        val binding = when (viewType) {
            R.layout.material_news_item_view -> {
                MaterialNewsItemViewBinding.inflate(
                    LayoutInflater.from(context), parent, false
                )
            }
            R.layout.first_material_news_item_view -> {
                FirstMaterialNewsItemViewBinding.inflate(
                    LayoutInflater.from(context), parent, false
                )
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
        return ItemViewHolder(binding)
    }

    // Determine the view type based on position
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            R.layout.first_material_news_item_view
        } else {
            R.layout.material_news_item_view
        }
    }

    // Replace the contents of a view
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        Log.e(LOG_TAG, "ON BIND VIEWHOLDER $position")
        val magicCard = items[position]
        holder.bind(magicCard)
        holder.itemView.setOnClickListener { itemClickListener?.invoke(magicCard) }
    }

    // Return the size of the dataset
    override fun getItemCount(): Int = items.size

    // Check if show images preference is enabled
    private fun isShowImagesEnabled(): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean("showImages", false)
    }

    // Provide a reference to the views for each data item
    inner class ItemViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        // Bind data to the views
        fun bind(newsItem: NewsItem) {
            when (binding) {
                is MaterialNewsItemViewBinding -> {
                    binding.tvCardTitle.text = newsItem.title
                    binding.tvCardAuthor.text = newsItem.author
                    binding.tvCardDate.text = newsItem.publicationDate.toString()

                    val imageUrl = newsItem.imageUrl
                    if (isShowImagesEnabled()) {
                        Glide.with(binding.root)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(binding.ivCard)
                    } else {
                        binding.ivCard.visibility = View.GONE
                    }
                }
                is FirstMaterialNewsItemViewBinding -> {
                    binding.tvCardTitle.text = newsItem.title
                    binding.tvAuthor.text = newsItem.author
                    binding.tvPublicationDate.text = newsItem.publicationDate.toString()

                    val imageUrl = newsItem.imageUrl
                    if (isShowImagesEnabled()) {
                        Glide.with(binding.root)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(binding.ivCard)
                    } else {
                        binding.ivCard.visibility = View.GONE
                    }
                }
            }
        }
    }
}
