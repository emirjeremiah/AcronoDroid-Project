package com.example.acronodroid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.acronodroid.db.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var btnUpload: Button
    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button
    private lateinit var btnBack: ImageButton
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvAcronymsCreated: TextView
    private lateinit var tvTotalViews: TextView

    private val storageRef by lazy { FirebaseStorage.getInstance().reference }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var db: AppDatabase

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadProfileImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = AppDatabase.getDatabase(this)

        imgProfile = findViewById(R.id.imgProfile)
        btnUpload = findViewById(R.id.btnUpload)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnLogout = findViewById(R.id.btnLogout)
        btnBack = findViewById(R.id.btnBack)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvAcronymsCreated = findViewById(R.id.tvAcronymsCreated)
        tvTotalViews = findViewById(R.id.tvTotalViews)

        btnBack.setOnClickListener {
            finish()
        }

        // Load user data
        val uid = auth.currentUser?.uid
        if (uid != null) {
            // Load profile image
            val ref = storageRef.child("profiles/$uid.jpg")
            ref.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this).load(uri).into(imgProfile)
            }

            // Load user info from Firestore
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val fullName = document.getString("fullName") ?: "User"
                        val email = document.getString("email") ?: ""
                        tvUserName.text = fullName
                        tvUserEmail.text = email
                    } else {
                        tvUserEmail.text = auth.currentUser?.email ?: ""
                    }
                }

            // Count user's acronyms
            lifecycleScope.launch {
                val acronyms = db.acronymDao().getAll()
                acronyms.collect { list ->
                    val userAcronyms = list.filter { it.authorUid == uid }
                    tvAcronymsCreated.text = userAcronyms.size.toString()

                    // For total views, we'll just show a placeholder since we don't track views yet
                    tvTotalViews.text = (userAcronyms.size * 10).toString()
                }
            }
        }

        btnUpload.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnEditProfile.setOnClickListener {
            // TODO: Implement edit profile functionality
            // For now, just show a placeholder message
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