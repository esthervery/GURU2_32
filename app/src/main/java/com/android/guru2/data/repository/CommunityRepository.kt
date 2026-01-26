package com.android.guru2.data.repository

import com.android.guru2.data.SupabaseClient
import com.android.guru2.data.model.CommunityPost
import com.android.guru2.data.model.CommunityComment
import com.android.guru2.data.model.PostLike
import io.github.jan.supabase.postgrest.from

class CommunityRepository {
    private val client = SupabaseClient.client

    // 게시글 관련
    suspend fun insertPost(post: CommunityPost): Boolean {
        return try {
            client.from("community_posts").insert(post)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getPostsByCategory(category: String): List<CommunityPost> {
        return try {
            client.from("community_posts")
                .select {
                    filter {
                        eq("category", category)
                    }
                }.decodeList<CommunityPost>()
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getPostById(postId: Int): CommunityPost? {
        return try {
            client.from("community_posts")
                .select {
                    filter {
                        eq("id", postId)
                    }
                }.decodeSingleOrNull<CommunityPost>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun searchPosts(query: String, category: String): List<CommunityPost> {
        return try {
            client.from("community_posts")
                .select {
                    filter {
                        eq("category", category)
                    }
                }.decodeList<CommunityPost>()
                .filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.content.contains(query, ignoreCase = true) ||
                            it.hashtag?.contains(query, ignoreCase = true) == true
                }
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 좋아요 관련
    suspend fun toggleLike(postId: Int, userId: String): Boolean {
        return try {
            val existing = client.from("post_likes")
                .select {
                    filter {
                        eq("post_id", postId)
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<PostLike>()

            if (existing != null) {
                // 좋아요 취소
                client.from("post_likes")
                    .delete {
                        filter {
                            eq("id", existing.id!!)
                        }
                    }
            } else {
                // 좋아요 추가
                client.from("post_likes").insert(
                    PostLike(postId = postId, userId = userId)
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun isPostLiked(postId: Int, userId: String): Boolean {
        return try {
            val result = client.from("post_likes")
                .select {
                    filter {
                        eq("post_id", postId)
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<PostLike>()
            result != null
        } catch (e: Exception) {
            false
        }
    }

    // 댓글 관련
    suspend fun insertComment(comment: CommunityComment): Boolean {
        return try {
            client.from("community_comments").insert(comment)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getCommentsByPostId(postId: Int): List<CommunityComment> {
        return try {
            client.from("community_comments")
                .select {
                    filter {
                        eq("post_id", postId)
                    }
                }.decodeList<CommunityComment>()
                .sortedBy { it.createdAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}