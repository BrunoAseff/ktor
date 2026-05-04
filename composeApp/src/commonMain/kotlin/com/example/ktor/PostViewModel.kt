package com.example.ktor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {
    private val service = PostService()
    
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentPage = 1
    private val limit = 10
    private var canLoadMore = true
    private var currentUserId: Int? = null

    init {
        loadPosts()
    }

    fun loadPosts(userId: Int? = currentUserId, reset: Boolean = false) {
        if (reset) {
            currentPage = 1
            _posts.value = emptyList()
            canLoadMore = true
            currentUserId = userId
        }

        if (_isLoading.value || !canLoadMore) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newPosts = service.getPosts(currentPage, limit, userId)
                if (newPosts.isEmpty()) {
                    canLoadMore = false
                } else {
                    _posts.value = _posts.value + newPosts
                    currentPage++
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
