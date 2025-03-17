package com.example.fooddeliveryapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> get() = _userProfile
    private var snapshotListener: ListenerRegistration? = null
    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address


    fun loadProfile() {
        snapshotListener?.remove()
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            snapshotListener = db.collection("users").document(uid)
                .addSnapshotListener { doc, error ->
                    if (error != null) {
                        Log.e("ProfileVM", "Listen error: ${error.message}")
                        return@addSnapshotListener
                    }
                    // Update full user profile
                    val profile = doc?.toObject(UserProfile::class.java)
                    _userProfile.value = profile

                    // Sync address state with profile
                    profile?.address?.let { _address.value = it }
                }
        }
    }


    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }

    fun refreshProfile() {
        _userProfile.value = null
        loadProfile()
    }

    fun updateLocalAddress(newAddress: String) {
        _userProfile.value = _userProfile.value?.copy(address = newAddress)
    }

    fun updateAddress(newAddress: String) {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                    db.collection("users").document(uid).update("address", newAddress).await()
                    updateLocalAddress(newAddress)
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "Address update failed: ${e.message}")
            }
        }
    }


    fun updateProfile(name: String, mobile: String, address: String, dob: String) {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                    db.collection("users").document(uid).set(
                        mapOf(
                            "name" to name,
                            "mobile" to "+973$mobile",
                            "address" to address, // Use parameter directly
                            "dob" to dob
                        ),
                        SetOptions.merge()
                    ).await()
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "Update error: ${e.message}")
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