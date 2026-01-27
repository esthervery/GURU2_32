package com.android.guru2.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CommunityPost(
    val id: String,


    @SerialName("user_id")
    val userId: String = "",


    @SerialName("user_name")
    val userName: String = "",


    @SerialName("user_age")
    val userAge: String = "",


    val hashtag: String = "",


    @SerialName("profile_image_url")
    val profileImageUrl: String = "",


    @SerialName("image_url")
    val imageUrl: String? = null,


    val title: String = "",
    val content: String = "",


    @SerialName("like_count")
    val likeCount: Int = 0,


    val date: String = "",
    val category: String = "",


    @SerialName("created_at")
    val createdAt: String? = null
)


@Serializable
data class PostLike(
    val id: Long? = null,


    @SerialName("post_id")
    val postId: String,


    @SerialName("user_id")
    val userId: String,


    @SerialName("created_at")
    val createdAt: String? = null
)