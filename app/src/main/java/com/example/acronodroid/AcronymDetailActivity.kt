package com.example.acronodroid

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.acronodroid.db.AppDatabase
import com.example.acronodroid.models.Acronym
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.*

class AcronymDetailActivity : AppCompatActivity() {

    private lateinit var tvShort: TextView
    private lateinit var tvFull: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvExplanation: TextView
    private lateinit var tvExample: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button
    private lateinit var btnBack: ImageButton

    private val dbFirestore = FirebaseFirestore.getInstance()
    private val auth by lazy { FirebaseAuth.getInstance() }
    private lateinit var localDb: AppDatabase
    private lateinit var currentAcronym: Acronym

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acronym_detail)

        tvShort = findViewById(R.id.tvShort)
        tvFull = findViewById(R.id.tvFull)
        tvCategory = findViewById(R.id.tvCategory)
        tvExplanation = findViewById(R.id.tvExplanation)
        tvExample = findViewById(R.id.tvExample)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)
        btnBack = findViewById(R.id.btnBack)

        localDb = AppDatabase.getDatabase(this)

        btnBack.setOnClickListener {
            finish()
        }

        // Get data from intent
        val id = intent.getStringExtra("acronym_id") ?: UUID.randomUUID().toString()
        val short = intent.getStringExtra("acronym_short") ?: ""
        val full = intent.getStringExtra("acronym_full") ?: ""
        val expl = intent.getStringExtra("acronym_expl") ?: ""
        val example = intent.getStringExtra("acronym_example") ?: ""
        val cat = intent.getStringExtra("acronym_cat") ?: ""
        val author = intent.getStringExtra("acronym_author")

        currentAcronym = Acronym(
            id = id,
            short = short,
            full = full,
            explanation = expl,
            example = example,
            category = cat,
            authorUid = author
        )

        showData()

        btnEdit.setOnClickListener {
            val i = Intent(this, AddEditAcronymActivity::class.java)
            i.putExtra("is_edit", true)
            i.putExtra("id", currentAcronym.id)
            i.putExtra("short", currentAcronym.short)
            i.putExtra("full", currentAcronym.full)
            i.putExtra("expl", currentAcronym.explanation)
            i.putExtra("example", currentAcronym.example)
            i.putExtra("cat", currentAcronym.category)
            startActivity(i)
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Acronym?")
                .setMessage("Are you sure you want to delete this acronym? This action cannot be undone.")
                .setPositiveButton("Yes, Delete") { _, _ ->
                    deleteAcronym()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showData() {
        tvShort.text = currentAcronym.short
        tvFull.text = currentAcronym.full
        tvCategory.text = currentAcronym.category
        tvExplanation.text = currentAcronym.explanation
        tvExample.text = currentAcronym.example
    }

    private fun deleteAcronym() {
        lifecycleScope.launch {
            if (currentAcronym.isLocalOnly || currentAcronym.authorUid == null) {
                // Local only
                localDb.acronymDao().delete(currentAcronym)
                finish()
            } else {
                // Delete from Firestore
                dbFirestore.collection("acronyms").document(currentAcronym.id)
                    .delete()
                    .addOnSuccessListener {
                        lifecycleScope.launch {
                            localDb.acronymDao().delete(currentAcronym)
                        }
                        finish()
                    }
                    .addOnFailureListener {
                        finish()
                    }
            }
        }
    }
}