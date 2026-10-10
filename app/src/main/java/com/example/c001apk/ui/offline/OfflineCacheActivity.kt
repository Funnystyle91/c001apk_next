package com.example.c001apk.ui.offline

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityOfflineCacheBinding
import com.example.c001apk.logic.model.OfflineFeedSummary
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.ui.feed.FeedActivity
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.makeToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

/**
 * 离线缓存列表：用户自己存下来的动态 / 图文，断网也能翻。
 *
 * 点条目进的是**同一个详情页**（[FeedActivity]），只是带上 `offline=true`，
 * 那边会跳过所有网络请求、只读本机存档（见 FeedViewModel.loadOffline）。
 */
@AndroidEntryPoint
class OfflineCacheActivity : BaseActivity<ActivityOfflineCacheBinding>() {

    private val viewModel by viewModels<OfflineCacheViewModel>()
    private lateinit var mAdapter: OfflineCacheAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBar()
        initView()
        initObserve()
    }

    private fun initBar() {
        binding.toolBar.apply {
            title = getString(R.string.offline_cache)
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener { finish() }
            inflateMenu(R.menu.offline_cache_menu)
            setOnMenuItemClickListener {
                if (it.itemId == R.id.clearAll) {
                    confirmClearAll()
                    true
                } else false
            }
        }
    }

    private fun initView() {
        mAdapter = OfflineCacheAdapter(
            onClick = { item ->
                IntentUtil.startActivity<FeedActivity>(this) {
                    putExtra("id", item.fid)
                    putExtra("offline", true)
                }
            },
            onDelete = { item -> confirmDelete(item) }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@OfflineCacheActivity)
            adapter = mAdapter
        }
    }

    private fun initObserve() {
        viewModel.list.observe(this) { list ->
            mAdapter.submitList(list)
            binding.emptyTip.isVisible = list.isEmpty()
            binding.recyclerView.isVisible = list.isNotEmpty()
            // 空列表时「清空全部」没意义，直接收起来
            binding.toolBar.menu.findItem(R.id.clearAll)?.isVisible = list.isNotEmpty()
        }
        viewModel.savingIds.observe(this) { ids ->
            binding.savingTip.isVisible = ids.isNotEmpty()
            if (ids.isNotEmpty())
                binding.savingTip.text = getString(R.string.offline_cache_saving_count, ids.size)
        }
    }

    private fun confirmDelete(item: OfflineFeedSummary) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.offline_cache_delete_title)
            .setMessage(R.string.offline_cache_delete_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ -> viewModel.delete(item.fid) }
            .show()
    }

    private fun confirmClearAll() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.offline_cache_clear)
            .setMessage(R.string.offline_cache_clear_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.clearAll()
                makeToast(getString(R.string.offline_cache_cleared))
            }
            .show()
    }
}
