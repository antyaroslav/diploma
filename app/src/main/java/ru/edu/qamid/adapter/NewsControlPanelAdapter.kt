package ru.edu.qamid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.edu.qamid.R
import ru.edu.qamid.databinding.ItemNewsControlPanelBinding
import ru.edu.qamid.dto.News
import ru.edu.qamid.dto.NewsWithCategory
import ru.edu.qamid.extensions.getType
import ru.edu.qamid.utils.Utils
import ru.edu.qamid.utils.Utils.generateShortUserName

interface NewsOnInteractionListener {
    fun onCard(newsItem: News)
    fun onEdit(newItemWithCategory: NewsWithCategory)
    fun onRemove(newItemWithCategory: NewsWithCategory)
}

class NewsControlPanelListAdapter(
    private val onInteractionListener: NewsOnInteractionListener
) : ListAdapter<NewsWithCategory, NewsControlPanelListAdapter.NewsControlPanelViewHolder>(
    NewsControlPanelDiffCallBack
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsControlPanelViewHolder {
        val binding = ItemNewsControlPanelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return NewsControlPanelViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: NewsControlPanelViewHolder, position: Int) {
        val newsWithCategory = getItem(position)
        holder.bind(newsWithCategory)
    }

    class NewsControlPanelViewHolder(
        private val binding: ItemNewsControlPanelBinding,
        private val onInteractionListener: NewsOnInteractionListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(newsItem: NewsWithCategory) {
            with(binding) {
                newsItemTitleTextView.text = newsItem.newsItem.title
                newsItemDescriptionTextView.text = newsItem.newsItem.description
                newsItemPublicationDateTextView.text =
                    Utils.formatDate(newsItem.newsItem.publishDate)
                newsItemCreateDateTextView.text =
                    Utils.formatDate(newsItem.newsItem.createDate)
                newsItemAuthorNameTextView.text =
                    generateShortUserName(newsItem.newsItem.creatorName)

                setCategoryIcon(newsItem)

                if (newsItem.newsItem.isOpen) {
                    newsItemDescriptionTextView.visibility = View.VISIBLE
                    newsItemExpandImageView.setImageResource(R.drawable.expand_less_24)
                } else {
                    newsItemDescriptionTextView.visibility = View.GONE
                    newsItemExpandImageView.setImageResource(R.drawable.expand_more_24)
                }

                newsItemMaterialCardView.setOnClickListener {
                    onInteractionListener.onCard(newsItem.newsItem)
                }

                if (newsItem.newsItem.publishEnabled) {
                    newsItemPublishedTextView.text =
                        itemView.context.getString(R.string.news_control_panel_active)
                    newsItemPublishedIconImageView.setImageResource(R.drawable.ic_baseline_check_24)
                } else {
                    newsItemPublishedTextView.text =
                        itemView.context.getString(R.string.news_control_panel_not_active)
                    newsItemPublishedIconImageView.setImageResource(R.drawable.ic_baseline_clear_24)
                }

                newsItemEditImageView.setOnClickListener {
                    onInteractionListener.onEdit(newsItem)
                }

                newsItemDeleteImageView.setOnClickListener {
                    onInteractionListener.onRemove(newsItem)
                }
            }
        }

        private fun setCategoryIcon(newsItem: NewsWithCategory) {
            val iconResId = when (newsItem.category.getType()) {
                News.Category.Type.Advertisement -> R.raw.icon_advertisement
                News.Category.Type.Salary -> R.raw.icon_salary
                News.Category.Type.Union -> R.raw.icon_union
                News.Category.Type.Birthday -> R.raw.icon_birthday
                News.Category.Type.Holiday -> R.raw.icon_holiday
                News.Category.Type.Massage -> R.raw.icon_massage
                News.Category.Type.Gratitude -> R.raw.icon_gratitude
                News.Category.Type.Help -> R.raw.icon_help
                News.Category.Type.Unknown -> return
            }
            binding.categoryIconImageView.setImageResource(iconResId)
        }
    }
}

private object NewsControlPanelDiffCallBack : DiffUtil.ItemCallback<NewsWithCategory>() {
    override fun areItemsTheSame(oldItem: NewsWithCategory, newItem: NewsWithCategory): Boolean {
        return oldItem.newsItem.id == newItem.newsItem.id
    }

    override fun areContentsTheSame(oldItem: NewsWithCategory, newItem: NewsWithCategory): Boolean {
        return oldItem == newItem
    }
}
