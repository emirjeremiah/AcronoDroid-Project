package com.example.acronodroid

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.acronodroid.db.AppDatabase
import com.example.acronodroid.models.Acronym
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.*

class AddEditAcronymActivity : AppCompatActivity() {

    private lateinit var inputShort: EditText
    private lateinit var inputFull: EditText
    private lateinit var inputCategory: EditText
    private lateinit var inputExplanation: EditText
    private lateinit var inputExample: EditText
    private lateinit var btnSave: Button

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

        dbLocal = AppDatabase.getDatabase(this)

        isEdit = intent.getBooleanExtra("is_edit", false)
        if (isEdit) {
            editId = intent.getStringExtra("id")
            inputShort.setText(intent.getStringExtra("short"))
            inputFull.setText(intent.getStringExtra("full"))
            inputCategory.setText(intent.getStringExtra("cat"))
            inputExplanation.setText(intent.getStringExtra("expl"))
            inputExample.setText(intent.getStringExtra("example"))
        }

        btnSave.setOnClickListener {
            val short = inputShort.text.toString().trim()
            val full = inputFull.text.toString().trim()
            val category = inputCategory.text.toString().trim()
            val expl = inputExplanation.text.toString().trim()
            val example = inputExample.text.toString().trim()

            if (short.isEmpty() || full.isEmpty()) {
                // quick validation
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val id = editId ?: dbFirestore.collection("acronyms").document().id
                val userUid = auth.currentUser?.uid
                val acr = Acronym(id = id, short = short, full = full, explanation = expl, example = example, category = category, authorUid = userUid, isLocalOnly = false)

                // Save to Firestore (user-created)
                dbFirestore.collection("acronyms").document(id)
                    .set(acr)
                    .addOnSuccessListener {
                        // Also store locally to show instantly
                        lifecycleScope.launch {
                            dbLocal.acronymDao().insert(acr)
                        }
                        finish()
                    }
                    .addOnFailureListener {
                        // fallback: store only locally and mark as local
                        lifecycleScope.launch {
                            val fallback = acr.copy(isLocalOnly = true)
                            dbLocal.acronymDao().insert(fallback)
                            finish()
                        }
                    }
            }
        }
    }
}
