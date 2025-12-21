package com.example.acronodroid

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acronodroid.adapters.AcronymAdapter
import com.example.acronodroid.db.AppDatabase
import com.example.acronodroid.models.Acronym
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LibraryActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: AcronymAdapter
    private lateinit var rv: RecyclerView
    private val auth by lazy { FirebaseAuth.getInstance() }
    private var allAcronyms = listOf<Acronym>()
    private var currentCategory = "All Categories"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        db = AppDatabase.getDatabase(this)

        rv = findViewById(R.id.rvAcronyms)
        val searchInput = findViewById<EditText>(R.id.searchInput)
        val addBtn = findViewById<ImageButton>(R.id.btnAdd)
        val fabProfile = findViewById<FloatingActionButton>(R.id.fabProfile)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)

        adapter = AcronymAdapter(emptyList()) { acronym ->
            val i = Intent(this, AcronymDetailActivity::class.java)
            i.putExtra("acronym_id", acronym.id)
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

        // Setup category spinner
        val categories = mutableListOf("All Categories")
        lifecycleScope.launch {
            db.acronymDao().getAll().collectLatest { list ->
                allAcronyms = list

                // Extract unique categories
                val uniqueCategories = list.map { it.category }.distinct().sorted()
                categories.clear()
                categories.add("All Categories")
                categories.addAll(uniqueCategories)

                val spinnerAdapter = ArrayAdapter(
                    this@LibraryActivity,
                    android.R.layout.simple_spinner_item,
                    categories
                )
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = spinnerAdapter

                filterAcronyms()
            }
        }

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentCategory = categories[position]
                filterAcronyms()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Instant search
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterAcronyms(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        addBtn.setOnClickListener {
            startActivity(Intent(this, AddEditAcronymActivity::class.java))
        }

        fabProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun filterAcronyms(searchQuery: String = "") {
        var filtered = allAcronyms

        // Filter by category
        if (currentCategory != "All Categories") {
            filtered = filtered.filter { it.category == currentCategory }
        }

        // Filter by search query
        if (searchQuery.isNotEmpty()) {
            val query = searchQuery.lowercase()
            filtered = filtered.filter {
                it.short.lowercase().contains(query) ||
                        it.full.lowercase().contains(query) ||
                        it.explanation.lowercase().contains(query)
            }
        }

        adapter.update(filtered)
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this screen
        lifecycleScope.launch {
            db.acronymDao().getAll().collectLatest { list ->
                allAcronyms = list
                filterAcronyms()
            }
        }
    }
}