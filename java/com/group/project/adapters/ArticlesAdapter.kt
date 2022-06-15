package com.group.project.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.group.project.databinding.ArticlesItemBinding
import com.group.project.models.DataRespond

class ArticlesAdapter: RecyclerView.Adapter<ArticlesAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(val binding: ArticlesItemBinding) : RecyclerView.ViewHolder(binding.root)

    /* https://youtu.be/t6Sql3WMAnk?t=1325 */
    private val diffCallback = object : DiffUtil.ItemCallback<DataRespond>() {
        override fun areItemsTheSame(oldItem: DataRespond, newItem: DataRespond): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DataRespond, newItem: DataRespond): Boolean {
            return oldItem == newItem
        }
    }
    //asynchronous and updating to happen behind the scene
    private val differ = AsyncListDiffer(this,diffCallback)
    var articles: List<DataRespond>
        get() = differ.currentList
        set(value) {differ.submitList(value)}

    override fun getItemCount(): Int {
        return articles.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(ArticlesItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ))
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.binding.apply {
            val article = articles[position]
            tvTitle.text = article.title
            cbDone.isChecked = article.approved
        }
    }

}