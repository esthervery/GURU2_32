package com.android.guru2.data.repository

import com.android.guru2.data.SupabaseClient
import com.android.guru2.data.model.CommunityPost
import com.android.guru2.data.model.PostLike
import io.github.jan.supabase.postgrest.from

class CommunityRepository {

    private val client = SupabaseClient.client

    // 글 등록
    suspend fun insertPost(post: CommunityPost): Boolean {
        return try {
            client.from("community_posts").insert(post)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 카테고리 + 사용자별 목록
    suspend fun getPostsByCategory(category: String, userId: String): List<CommunityPost> {
        return try {
            client.from("community_posts")
                .select {
                    filter {
                        eq("category", category)
                        eq("user_id", userId)
                    }
                }
                .decodeList<CommunityPost>()
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 검색(간단)
    suspend fun searchPosts(query: String, category: String, userId: String): List<CommunityPost> {
        return try {
            client.from("community_posts")
                .select {
                    filter {
                        eq("category", category)
                        eq("user_id", userId)
                    }
                }
                .decodeList<CommunityPost>()
                .filter {
                    it.title.contains(query, true) ||
                            it.content.contains(query, true) ||
                            it.hashtag.contains(query, true)
                }
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 좋아요 토글 + like_count 동기화
    suspend fun toggleLike(postId: String, userId: String): Boolean {
        return try {
            val existing = client.from("post_likes")
                .select {
                    filter {
                        eq("post_id", postId)
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<PostLike>()

            val post = client.from("community_posts")
                .select { filter { eq("id", postId) } }
                .decodeSingleOrNull<CommunityPost>()

            val currentCount = post?.likeCount ?: 0

            if (existing != null) {
                // 취소
                client.from("post_likes")
                    .delete { filter { eq("id", existing.id!!) } }

                val newCount = (currentCount - 1).coerceAtLeast(0)
                client.from("community_posts")
                    .update({ set("like_count", newCount) }) { filter { eq("id", postId) } }
            } else {
                // 추가
                client.from("post_likes").insert(PostLike(postId = postId, userId = userId))

                val newCount = currentCount + 1
                client.from("community_posts")
                    .update({ set("like_count", newCount) }) { filter { eq("id", postId) } }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}