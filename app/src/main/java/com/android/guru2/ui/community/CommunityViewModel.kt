package com.android.guru2.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.guru2.data.SupabaseClientProvider
import com.android.guru2.data.model.CommunityPost
import com.android.guru2.data.repository.CommunityRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CommunityViewModel : ViewModel() {

    private val repository = CommunityRepository()

    private val _posts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val posts: StateFlow<List<CommunityPost>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentCategory = MutableStateFlow("나란히")
    val currentCategory: StateFlow<String> = _currentCategory.asStateFlow()

    private fun currentUserIdOrNull(): String? {
        return SupabaseClientProvider.client.auth.currentUserOrNull()?.id
    }

    fun loadPosts(category: String) {
        _currentCategory.value = category
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = currentUserIdOrNull()
                if (userId == null) {
                    _posts.value = emptyList()
                    return@launch
                }
                _posts.value = repository.getPostsByCategory(category, userId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchPosts(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = currentUserIdOrNull()
                if (userId == null) {
                    _posts.value = emptyList()
                    return@launch
                }
                _posts.value = repository.searchPosts(query, _currentCategory.value, userId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPost(
        title: String,
        content: String,
        hashtag: String,
        imageUrl: String?,
        userName: String,
        userAge: String,
        profileImageUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = currentUserIdOrNull()
        if (userId == null) {
            onError("로그인이 필요합니다")
            return
        }

        val postId = UUID.randomUUID().toString()
        val date = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
        val tag = hashtag.trim().let { if (it.isEmpty()) "" else if (it.startsWith("#")) it else "#$it" }

        val post = CommunityPost(
            id = postId,
            userId = userId,
            userName = userName,
            userAge = userAge,
            hashtag = tag,
            profileImageUrl = profileImageUrl,
            imageUrl = imageUrl,
            title = title,
            content = content,
            likeCount = 0,
            date = date,
            category = _currentCategory.value
        )

        viewModelScope.launch {
            _isLoading.value = true
            val ok = repository.insertPost(post)
            _isLoading.value = false

            if (ok) {
                loadPosts(_currentCategory.value)
                onSuccess()
            } else {
                onError("등록 실패")
            }
        }
    }

    fun toggleLike(postId: String, onError: (String) -> Unit = {}) {
        val userId = currentUserIdOrNull()
        if (userId == null) {
            onError("로그인이 필요합니다")
            return
        }

        viewModelScope.launch {
            val ok = repository.toggleLike(postId, userId)
            if (!ok) onError("좋아요 반영 실패")
            loadPosts(_currentCategory.value)
        }
    }

    init {
        loadPosts("나란히")
    }
}