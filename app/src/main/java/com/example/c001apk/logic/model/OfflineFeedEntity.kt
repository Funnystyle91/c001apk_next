package com.example.c001apk.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 一条离线存档：帖子文稿 + 图片 + 置顶评论 + 前 20 条评论。
 *
 * 存的是**整份 JSON 快照**（[feedJson] / [replyJson]）而不是拆成一张张表：详情页渲染用的就是
 * 接口原样的 `HomeFeedResponse.Data`（图文还得再解 `messageRawOutput`），拆表等于把几十个字段
 * 和嵌套结构重搬一遍，服务端一加字段这里就漏。存原文的代价只是列表页要用的几个字段冗余一份。
 *
 * 落库前所有图片 URL 都会被换成本地文件路径（见 `OfflineCacheRepo`），所以断网、
 * 甚至原帖已经被删掉，也照样能翻出来看。
 */
@Entity
data class OfflineFeedEntity(
    /** 帖子 id，同时是主键：同一个帖子重复缓存直接覆盖 */
    @PrimaryKey
    val fid: String,
    val uid: String?,
    val uname: String?,
    /** 作者头像，本地路径 */
    val avatar: String?,
    /** 图文标题，动态没有这一项 */
    val title: String?,
    /** 正文纯文本，列表页拿它当摘要 */
    val message: String?,
    /** 列表页封面（正文第一张图 / 图文封面），本地路径 */
    val cover: String?,
    val device: String?,
    val dateline: Long,
    /** 原帖的回复总数；存档里只有置顶 + 前 20 条，通常比这个数小 */
    val replyCount: Int,
    /** 存档里实际存下来的评论条数 */
    val savedReplyCount: Int,
    /** 存下来的本机时间 */
    val savedAt: Long,
    /** 图片目录，删存档时整个目录一起删 */
    val imageDir: String,
    /** `HomeFeedResponse.Data` 的 JSON 原文（图片已换成本地路径） */
    val feedJson: String,
    /** `List<TotalReplyResponse.Data>` 的 JSON 原文（图片已换成本地路径） */
    val replyJson: String
)
