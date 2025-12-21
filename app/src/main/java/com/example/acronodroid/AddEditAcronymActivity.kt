package com.example.acronodroid

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.acronodroid.db.AppDatabase
import com.example.acronodroid.models.Acronym
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class AddEditAcronymActivity : AppCompatActivity() {

    private lateinit var inputShort: TextInputEditText
    private lateinit var inputFull: TextInputEditText
    private lateinit var inputCategory: TextInputEditText
    private lateinit var inputExplanation: TextInputEditText
    private lateinit var inputExample: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView

    private lateinit var dbLocal: AppDatabase
    private val dbFirestore = FirebaseFirestore.getInstance()
    private val auth by lazy { FirebaseAuth.getInstance() }

    private var isEdit = false
    private var editId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_acronym)

        inputShort = findViewById(R.id.inputShort)
        inputFull = findViewById(R.id.inputFull)
        inputCategory = findViewById(R.id.inputCategory)
        inputExplanation = findViewById(R.id.inputExplanation)
        inputExample = findViewById(R.id.inputExample)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)

        dbLocal = AppDatabase.getDatabase(this)

        btnBack.setOnClickListener {
            finish()
        }

        isEdit = intent.getBooleanExtra("is_edit", false)
        if (isEdit) {
            tvTitle.text = "Edit Acronym"
            btnSave.text = "Update Acronym"
            editId = intent.getStringExtra("id")
            inputShort.setText(intent.getStringExtra("short"))
            inputFull.setText(intent.getStringExtra("full"))
            inputCategory.setText(intent.getStringExtra("cat"))
            inputExplanation.setText(intent.getStringExtra("expl"))
            inputExample.setText(intent.getStringExtra("example"))
        } else {
            tvTitle.text = "Add Acronym"
        }

        btnSave.setOnClickListener {
            val short = inputShort.text.toString().trim()
            val full = inputFull.text.toString().trim()
            val category = inputCategory.text.toString().trim()
            val expl = inputExplanation.text.toString().trim()
            val example = inputExample.text.toString().trim()

            if (short.isEmpty() || full.isEmpty()) {
                Toast.makeText(this, "Please fill in at least Short and Full fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val id = editId ?: dbFirestore.collection("acronyms").document().id
                val userUid = auth.currentUser?.uid
                val acr = Acronym(
                    id = id,
                    short = short,
                    full = full,
                    explanation = expl,
                    example = example,
                    category = category,
                    authorUid = userUid,
                    isLocalOnly = false
                )

                // Save to Firestore
                dbFirestore.collection("acronyms").document(id)
                    .set(acr)
                    .addOnSuccessListener {
                        lifecycleScope.launch {
                            dbLocal.acronymDao().insert(acr)
                        }
                        Toast.makeText(this@AddEditAcronymActivity,
                            if (isEdit) "Acronym updated!" else "Acronym created!",
                            Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        // Fallback: store locally
                        lifecycleScope.launch {
                            val fallback = acr.copy(isLocalOnly = true)
                            dbLocal.acronymDao().insert(fallback)
                            Toast.makeText(this@AddEditAcronymActivity,
                                "Saved locally (offline mode)",
                                Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
            }
        }
    }
}