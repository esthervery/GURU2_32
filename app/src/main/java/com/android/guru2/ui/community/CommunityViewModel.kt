package com.android.guru2.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.guru2.data.model.CommunityPost
import com.android.guru2.data.repository.CommunityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {
    private val repository = CommunityRepository()

    private val _posts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val posts: StateFlow<List<CommunityPost>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentCategory = MutableStateFlow("나란히")
    val currentCategory: StateFlow<String> = _currentCategory.asStateFlow()

    fun loadPosts(category: String) {
        _currentCategory.value = category
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _posts.value = repository.getPostsByCategory(category)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchPosts(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _posts.value = repository.searchPosts(query, _currentCategory.value)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPost(
        title: String,
        content: String,
        hashtag: String?,
        imageUrls: List<String>,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val userId = "test_user" // TODO: 실제 사용자 ID
        val authorName = "사용자" // TODO: 실제 사용자 이름

        val post = CommunityPost(
            userId = userId,
            category = _currentCategory.value,
            title = title,
            content = content,
            hashtag = hashtag,
            imageUrls = imageUrls,
            authorName = authorName
        )

        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.insertPost(post)
            _isLoading.value = false

            if (success) {
                loadPosts(_currentCategory.value)
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun toggleLike(postId: Int) {
        val userId = "test_user" // TODO: 실제 사용자 ID
        viewModelScope.launch {
            repository.toggleLike(postId, userId)
            loadPosts(_currentCategory.value)
        }
    }

    init {
        loadPosts("나란히")
    }
}