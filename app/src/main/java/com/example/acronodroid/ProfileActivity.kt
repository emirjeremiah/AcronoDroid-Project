package com.example.acronodroid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var btnUpload: Button
    private lateinit var btnLogout: Button

    private val storageRef by lazy { FirebaseStorage.getInstance().reference }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { 
            uploadProfileImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        imgProfile = findViewById(R.id.imgProfile)
        btnUpload = findViewById(R.id.btnUpload)
        btnLogout = findViewById(R.id.btnLogout)

        // Load existing profile image if exists
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val ref = storageRef.child("profiles/$uid.jpg")
            ref.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this).load(uri).into(imgProfile)
            }
        }

        btnUpload.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun uploadProfileImage(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val ref = storageRef.child("profiles/$uid.jpg")
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    Glide.with(this).load(downloadUri).into(imgProfile)
                }
            }
    }
}
