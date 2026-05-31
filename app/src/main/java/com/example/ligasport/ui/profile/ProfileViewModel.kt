package com.example.ligasport.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel dla zakładki Profil.
 *
 * Odpowiada za:
 * - Wyświetlanie i zmianę nazwy użytkownika
 * - Wyświetlanie emaila
 * - Wylogowanie
 */
class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /** Email zalogowanego użytkownika */
    val userEmail: String = auth.currentUser?.email ?: ""

    /** Nazwa użytkownika */
    private val _userName = MutableStateFlow("Użytkowniku")
    val userName: StateFlow<String> = _userName

    /** Czy trwa zapisywanie */
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    /** Czy udało się zapisać */
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    init {
        loadUserName()
    }

    /**
     * Pobiera nazwę użytkownika z Firestore.
     */
    private fun loadUserName() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val doc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                _userName.value = doc.getString("userName") ?: "Użytkowniku"
            } catch (e: Exception) {
                _userName.value = "Użytkowniku"
            }
        }
    }

    /**
     * Aktualizuje nazwę użytkownika w Firestore.
     *
     * @param newName - nowa nazwa
     * @param onSuccess - callback po udanym zapisie
     */
    fun updateUserName(newName: String, onSuccess: () -> Unit) {
        if (newName.isBlank()) return

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                // Zapisz w Firestore
                firestore.collection("users")
                    .document(userId)
                    .update("userName", newName)
                    .await()

                // Aktualizuj lokalny stan
                _userName.value = newName
                _saveSuccess.value = true
                onSuccess()
            } catch (e: Exception) {
                _saveSuccess.value = false
            } finally {
                _isSaving.value = false
            }
        }
    }

    /** Wylogowuje użytkownika */
    fun logout() {
        auth.signOut()
    }

    /** Pobiera pierwszą literę nazwy (do awatara) */
    fun getUserInitial(): String {
        return _userName.value.firstOrNull()?.uppercase() ?: "?"
    }
}