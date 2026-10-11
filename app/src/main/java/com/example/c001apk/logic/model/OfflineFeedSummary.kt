package com.example.c001apk.logic.model

/**
 * 离线缓存列表用的「轻量版」存档。
 *
 * 整条存档里 `feedJson` / `replyJson` 动辄上百 KB（正文 + 20 条评论的原样 JSON），
 * 铺列表没必要把它读进内存 —— 点进详情时详情页自己会按 fid 去库里取全量。
 */
data class OfflineFeedSummary(
    val fid: String,
    val uid: String?,
    val uname: String?,
    val avatar: String?,
    val title: String?,
    val message: String,
    val cover: String?,
    val device: String?,
    val dateline: Long,
    val savedReplyCount: Int,
    val savedAt: Long
)
