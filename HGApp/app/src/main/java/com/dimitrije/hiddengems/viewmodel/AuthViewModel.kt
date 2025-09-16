package com.dimitrije.hiddengems.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import androidx.core.net.toUri
import androidx.compose.runtime.*

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun loginWithEmail(email: String, password: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    var registrationSuccess by mutableStateOf(false)
        private set

    fun registerWithFullData(
        name: String,
        surname: String,
        phone: String,
        email: String,
        password: String,
        photoUri: Uri?,
        context: Context,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val finalUri = photoUri ?: "android.resource://${context.packageName}/drawable/default_avatar".toUri()

                uploadToCloudinary(finalUri, context, uid,
                    onSuccess = { photoUrl ->
                        val userData = mapOf(
                            "name" to name,
                            "surname" to surname,
                            "phone" to phone,
                            "email" to email,
                            "photoUrl" to photoUrl,
                            "joinedAt" to Timestamp.now()
                        )

                        firestore.collection("users").document(uid)
                            .set(userData)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener(onError)
                    },
                    onError = onError
                )
            }
            .addOnFailureListener(onError)
    }

    private fun uploadToCloudinary(
        imageUri: Uri,
        context: Context,
        publicId: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val cloudName = "di4fagc5c"
        val uploadPreset = "hidden_gems_upload" //

        val inputStream = try {
            context.contentResolver.openInputStream(imageUri)
        } catch (e: Exception) {
            onError(e)
            return
        }

        if (inputStream == null) {
            onError(Exception("Invalid URI"))
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "profile.jpg",
                inputStream.readBytes().toRequestBody("image/*".toMediaTypeOrNull()))
            .addFormDataPart("upload_preset", uploadPreset)
            .addFormDataPart("public_id", "users/$publicId")
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onError(Exception("Cloudinary upload failed: ${response.code}"))
                    return
                }

                val json = JSONObject(response.body?.string() ?: "")
                val url = json.getString("secure_url")
                onSuccess(url)
            }
        })
    }
}