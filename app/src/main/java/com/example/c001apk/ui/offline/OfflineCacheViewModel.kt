package com.example.c001apk.ui.offline

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.model.OfflineFeedSummary
import com.example.c001apk.logic.repository.OfflineCacheRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 离线缓存列表。
 *
 * 列表本身是 Room 的 `LiveData`（见 [OfflineCacheRepo.loadAllLive]），存 / 删都由数据库自己
 * 推新数据，这里不需要手动 refresh，也没有分页 —— 缓存条数就是用户自己存过的那几条。
 */
@HiltViewModel
class OfflineCacheViewModel @Inject constructor(
    private val offlineCacheRepo: OfflineCacheRepo,
) : ViewModel() {

    /** 只读展示列的轻量存档，正文 / 评论的 JSON 留在库里（见 OfflineFeedSummary） */
    val list: LiveData<List<OfflineFeedSummary>> = offlineCacheRepo.loadAllLive()

    /** 正在缓存的帖子 id：列表页拿它显示顶部提示条 */
    val savingIds: LiveData<Set<String>> = offlineCacheRepo.savingIds

    fun delete(fid: String) {
        viewModelScope.launch { offlineCacheRepo.delete(fid) }
    }

    fun clearAll() {
        viewModelScope.launch { offlineCacheRepo.deleteAll() }
    }
}
