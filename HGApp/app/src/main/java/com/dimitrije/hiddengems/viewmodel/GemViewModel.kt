package com.dimitrije.hiddengems.viewmodel

import androidx.lifecycle.ViewModel
import com.dimitrije.hiddengems.model.Gem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
class GemViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userGems = MutableStateFlow<List<Gem>>(emptyList())
    val userGems: StateFlow<List<Gem>> = _userGems

    fun saveGem(title: String, description: String, lat: Double, lng: Double, imageUrl: String? = null) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("gems").document()
        val gem = Gem(
            id = docRef.id,
            title = title,
            description = description,
            lat = lat,
            lng = lng,
            imageUrl = imageUrl,
            createdBy = uid
        )
        docRef.set(gem).addOnSuccessListener {
            db.collection("users").document(uid)
                .update("score", com.google.firebase.firestore.FieldValue.increment(2))
        }

    }
}