package com.example.ktor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import io.ktor.client.plugins.*
import io.ktor.utils.io.errors.*

data class PostUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val currentPage: Int = 1
)

class PostViewModel : ViewModel() {
    private val service = PostService()
    
    private val _uiState = MutableStateFlow(PostUiState())
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    private val limit = 10
    private var currentUserId: Int? = null

    init {
        loadPosts()
    }

    fun loadPosts(userId: Int? = currentUserId, reset: Boolean = false) {
        if (reset) {
            currentUserId = userId
            _uiState.update { it.copy(
                posts = emptyList(),
                currentPage = 1,
                endReached = false,
                error = null
            ) }
        }

        val currentState = _uiState.value
        if (currentState.isLoading || currentState.endReached) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val newPosts = service.getPosts(
                    page = _uiState.value.currentPage,
                    limit = limit,
                    userId = currentUserId
                )
                
                _uiState.update { state ->
                    state.copy(
                        posts = state.posts + newPosts,
                        currentPage = state.currentPage + 1,
                        endReached = newPosts.size < limit,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is IOException -> "Falha na conexão. Verifique sua internet."
                    is ClientRequestException -> "Erro na requisição (4xx): ${e.response.status.value}"
                    is ServerResponseException -> "Erro no servidor (5xx): ${e.response.status.value}"
                    else -> e.message ?: "Ocorreu um erro inesperado."
                }
                _uiState.update { it.copy(error = errorMessage, isLoading = false) }
            }
        }
    }
    
    fun retry() {
        loadPosts()
    }
}
