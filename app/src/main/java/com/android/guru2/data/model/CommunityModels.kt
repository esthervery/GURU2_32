package com.android.guru2.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityPost(
    val id: Int? = null,
    @SerialName("user_id")
    val userId: String,
    val category: String, // "나란히" 또는 "마음으로"
    val title: String,
    val content: String,
    val hashtag: String? = null,
    @SerialName("image_urls")
    val imageUrls: List<String> = emptyList(),
    @SerialName("like_count")
    val likeCount: Int = 0,
    @SerialName("comment_count")
    val commentCount: Int = 0,
    @SerialName("author_name")
    val authorName: String,
    @SerialName("author_profile")
    val authorProfile: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class CommunityComment(
    val id: Int? = null,
    @SerialName("post_id")
    val postId: Int,
    @SerialName("user_id")
    val userId: String,
    val content: String,
    @SerialName("author_name")
    val authorName: String,
    @SerialName("author_profile")
    val authorProfile: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class PostLike(
    val id: Int? = null,
    @SerialName("post_id")
    val postId: Int,
    @SerialName("user_id")
    val userId: String,
    @SerialName("created_at")
    val createdAt: String? = null
)