package com.example.ligasport.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /** Czy trwa logowanie/rejestracja */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /** Komunikat błędu (null = brak błędu) */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /** Czy użytkownik jest zalogowany */
    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    /** Czy jesteśmy w trybie rejestracji */
    private val _isRegistering = MutableStateFlow(false)
    val isRegistering: StateFlow<Boolean> = _isRegistering

    /**
     * Logowanie użytkownika.
     */
    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Wypełnij wszystkie pola"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // await() = czeka na wynik, rzuca wyjątek przy błędzie
                auth.signInWithEmailAndPassword(email, password).await()
                _isLoggedIn.value = true
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Nieprawidłowy email lub hasło"
            }
        }
    }

    /**
     * Rejestracja nowego użytkownika.
     */
    fun register(email: String, password: String, userName: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Wypełnij wszystkie pola"
            return
        }
        if (userName.isBlank()) {
            _errorMessage.value = "Podaj nazwę użytkownika"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // 1. Utwórz konto
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user!!
                val userId = user.uid

                // 2. Zapisz nazwę w Firestore
                val userData = hashMapOf(
                    "userName" to userName,
                    "email" to email
                )
                firestore.collection("users")
                    .document(userId)
                    .set(userData)
                    .await()

                // 3. Zapisz nazwę w Auth (displayName)
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(userName)
                    .build()
                user.updateProfile(profileUpdates).await()

                _isLoggedIn.value = true
                _isLoading.value = false
                onSuccess()

            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Błąd rejestracji"
            }
        }
    }

    /** Przełącz między logowaniem a rejestracją */
    fun toggleRegistering() {
        _isRegistering.value = !_isRegistering.value
        _errorMessage.value = null
    }

    /** Wylogowanie użytkownika */
    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
    }

    /** Sprawdza czy użytkownik jest zalogowany */
    fun checkLoginStatus(): Boolean {
        return auth.currentUser != null
    }

    /** Pobiera email zalogowanego użytkownika */
    fun getUserEmail(): String {
        return auth.currentUser?.email ?: ""
    }

    /** Czyści błąd */
    fun clearError() {
        _errorMessage.value = null
    }
}