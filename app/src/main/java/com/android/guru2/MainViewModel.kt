package com.android.guru2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.android.guru2.data.CharacterDbModel
import com.android.guru2.data.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _characterUrl = MutableLiveData<String?>()
    val characterUrl: LiveData<String?> = _characterUrl

    // 현재 모드 상태 저장 (HOME 또는 STAR)
    var isStarMode = MutableLiveData<Boolean>(false)

    fun setCharacterUrl(url: String) {
        _characterUrl.value = url
    }

    fun loadCharacter() {
        // 이미 데이터가 있으면 재호출 안 하도록
        if (_characterUrl.value != null) return

        viewModelScope.launch {
            try {
                val currentUser = SupabaseClientProvider.client.auth.currentUserOrNull()
                val userId = currentUser?.id ?: return@launch

                val characterData = SupabaseClientProvider.client.postgrest["characterInfo"]
                    .select { filter { eq("id", userId) } }
                    .decodeSingle<CharacterDbModel>()

                _characterUrl.postValue(characterData.character_url)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}