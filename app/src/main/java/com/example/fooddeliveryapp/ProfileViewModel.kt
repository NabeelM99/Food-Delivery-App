package com.example.fooddeliveryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> get() = _userProfile

    fun loadProfile() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                db.collection("users").document(uid)
                    .addSnapshotListener { doc, _ ->
                        _userProfile.value = doc?.toObject(UserProfile::class.java)
                    }
            }
        }
    }

    fun updateProfile(name: String, mobile: String, address: String, dob: String) {
        viewModelScope.launch {
            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                db.collection("users").document(uid).update(
                    mapOf(
                        "name" to name,
                        "mobile" to mobile,
                        "address" to address,
                        "dob" to dob
                    )
                )
            }
        }
    }
}

data class UserProfile(
    val name: String = "",
    val mobile: String = "",
    val address: String = "",
    val dob: String = "",
    val profilePicture: String = ""
)