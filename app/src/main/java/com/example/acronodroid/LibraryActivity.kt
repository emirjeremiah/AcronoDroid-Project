package com.example.acronodroid

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acronodroid.adapters.AcronymAdapter
import com.example.acronodroid.db.AppDatabase
import com.example.acronodroid.models.Acronym
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.RecyclerView

class LibraryActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: AcronymAdapter
    private lateinit var rv: RecyclerView
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        db = AppDatabase.getDatabase(this)

        rv = findViewById(R.id.rvAcronyms)
        val searchInput = findViewById<EditText>(R.id.searchInput)
        val addBtn = findViewById<ImageButton>(R.id.btnAdd)
        val profileBtn = findViewById<Button>(R.id.btnProfile)

        adapter = AcronymAdapter(emptyList()) { acronym ->
            val i = Intent(this, AcronymDetailActivity::class.java)
            i.putExtra("acronym_id", acronym.id)
            // pass source flag to indicate local vs remote
            i.putExtra("acronym_short", acronym.short)
            i.putExtra("acronym_full", acronym.full)
            i.putExtra("acronym_expl", acronym.explanation)
            i.putExtra("acronym_example", acronym.example)
            i.putExtra("acronym_cat", acronym.category)
            i.putExtra("acronym_author", acronym.authorUid)
            startActivity(i)
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // Observes all acronyms initially
        lifecycleScope.launch {
            db.acronymDao().getAll().collectLatest { list ->
                adapter.update(list)
            }
        }

        // Instant search (local DB only)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = "%${s.toString().trim()}%"
                lifecycleScope.launch {
                    db.acronymDao().search(q).collectLatest { list ->
                        adapter.update(list)
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        addBtn.setOnClickListener {
            startActivity(Intent(this, AddEditAcronymActivity::class.java))
        }

        profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            // redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
