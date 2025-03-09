package com.example.fooddeliveryapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> get() = _userProfile

    fun loadProfile() {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                    db.collection("users").document(uid)
                        .addSnapshotListener { doc, error ->
                            if (error != null) {
                                Log.e("ProfileVM", "Listen error: ${error.message}")
                                return@addSnapshotListener
                            }
                            _userProfile.value = doc?.toObject(UserProfile::class.java).apply {
                                Log.d("ProfileVM", "Loaded profile: ${this?.name}")
                            }
                        }
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "Load error: ${e.message}")
            }
        }
    }

    fun refreshProfile() {
        loadProfile()
    }

    fun updateProfile(name: String, mobile: String, address: String, dob: String) {
        viewModelScope.launch {
            /*FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                val userRef = db.collection("users").document(uid)
                userRef.set(
                    mapOf(
                        "name" to name,
                        "mobile" to mobile,
                        "address" to address,
                        "dob" to dob
                    )
                ),
                com.google.firebase.firestore.SetOptions.merge()
                ), await()
            }*/
            try {
                FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                    db.collection("users").document(uid).set(
                        mapOf(
                            "name" to name,
                            "mobile" to mobile,
                            "address" to address,
                            "dob" to dob
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    ).await() // Wait for Firestore operation
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile: ${e.message}")
            }
        }
    }
}

data class UserProfile(
    val name: String = "",
    val mobile: String = "",
    val address: String = "",
    val dob: String = "",
    val profilePicture: String = "",
    val email: String = ""
)