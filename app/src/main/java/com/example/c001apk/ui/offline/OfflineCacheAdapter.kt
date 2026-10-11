package com.example.c001apk.ui.offline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.databinding.ItemOfflineCacheBinding
import com.example.c001apk.logic.model.OfflineFeedSummary
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageUtil

/** 离线缓存条目：封面 + 标题 / 摘要 + 作者 / 时间 / 存档信息 + 删除 */
class OfflineCacheAdapter(
    private val onClick: (OfflineFeedSummary) -> Unit,
    private val onDelete: (OfflineFeedSummary) -> Unit,
) : ListAdapter<OfflineFeedSummary, OfflineCacheAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemOfflineCacheBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemOfflineCacheBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val ctx = holder.binding.root.context
        with(holder.binding) {
            // 动态没有标题，正文直接顶到第一行，别留一行空白
            val hasTitle = !item.title.isNullOrBlank()
            title.text = if (hasTitle) item.title else item.message
            desc.text = item.message
            desc.isVisible = hasTitle && item.message.isNotBlank()
            info.text = buildString {
                item.uname?.takeIf { it.isNotBlank() }?.let { append(it) }
                item.device?.takeIf { it.isNotBlank() }?.let {
                    if (isNotEmpty()) append(" · ")
                    append(it)
                }
                // dateline 与服务端一致是秒；savedAt 是本机毫秒，喂给 fromToday 前要换算
                if (item.dateline > 0) {
                    if (isNotEmpty()) append(" · ")
                    append(DateUtils.fromToday(item.dateline))
                }
                if (item.savedReplyCount > 0) {
                    if (isNotEmpty()) append(" · ")
                    append(ctx.getString(R.string.offline_cache_reply_count, item.savedReplyCount))
                }
                if (isNotEmpty()) append(" · ")
                append(
                    ctx.getString(
                        R.string.offline_cache_saved_at,
                        DateUtils.fromToday(item.savedAt / 1000)
                    )
                )
            }
            if (item.cover.isNullOrEmpty()) {
                // 用 INVISIBLE 而不是 GONE：卡片高度统一，没有封面的条目不会塌矮一截
                cover.isInvisible = true
            } else {
                cover.isInvisible = false
                ImageUtil.showIMG(cover, item.cover, true)
            }
            root.setOnClickListener { onClick(item) }
            delete.setOnClickListener { onDelete(item) }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<OfflineFeedSummary>() {
            override fun areItemsTheSame(oldItem: OfflineFeedSummary, newItem: OfflineFeedSummary) =
                oldItem.fid == newItem.fid

            override fun areContentsTheSame(oldItem: OfflineFeedSummary, newItem: OfflineFeedSummary) =
                oldItem == newItem
        }
    }
}
