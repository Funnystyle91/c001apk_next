package com.example.c001apk.util

val String.http2https: String
    get() = if (this.getOrElse(4) { 's' } == 's') this
    else StringBuilder(this).insert(4, 's').toString()

/**
 * 离线存档里的图片前缀：`file://<绝对路径><原 URL 上的尺寸提示>`。
 *
 * 之所以把尺寸提示（`@1000x1000.jpg`）也一并搬到本地路径后面，是因为排版要读它
 * （见 `ImageUtil.getImageLp`）：只存一个干净路径的话，图文正文里每张图都会按 1:1 铺，
 * 宽图会被裁成方的。
 *
 * 注意 `getImageLp` 是拿 `@`、`x`、`.` 三个位置切字符串算宽高的，所以本地路径自身
 * 里不能出现 `x` —— 落盘目录叫 `offline_cache`、文件名是十六进制 hash，都天然没有。
 * 改目录名时留意这一点。
 */
const val LOCAL_IMAGE_SCHEME = "file://"

/** 是不是离线存档落下来的本机图 */
val String.isLocalImage: Boolean
    get() = startsWith(LOCAL_IMAGE_SCHEME)

/**
 * 本机图 → 能直接打开的文件路径；不是本机图返回 null。
 *
 * 摘掉尺寸提示才是真正的文件路径，顺带摘掉 `.s.jpg`：九宫格会给每张图挂缩略图后缀
 * （`NineGridImageView` 里那条 `${url}.s.jpg`），本机图没有衍生缩略图。
 */
val String.localImagePath: String?
    get() = if (!isLocalImage) null
    else removePrefix(LOCAL_IMAGE_SCHEME)
        .substringBefore('@')
        .substringBefore('?')
        .removeSuffix(".s.jpg")
        .ifBlank { null }

/**
 * 交给 Glide / Mojito 取图的实际地址。
 *
 * 本机图必须绕开 [http2https]：它按「第 4 个字符后面插一个 s」把 http 升成 https，
 * 喂 `file://` 进去会拼成 `file:s//`，取图那层就认不出来了。
 */
val String.imageUrl: String
    get() = localImagePath?.let { LOCAL_IMAGE_SCHEME + it } ?: http2https

/** 缩略图地址：本机图没有 `.s.jpg` 衍生图，给原图就行 */
val String.thumbImageUrl: String
    get() = if (isLocalImage) imageUrl else "$http2https.s.jpg"
