package com.example.acronodroid.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

// Firestore / Room unified model
@IgnoreExtraProperties
@Entity(tableName = "acronyms")
data class Acronym(
    @PrimaryKey var id: String = "",                 // use UID or generated id
    var short: String = "",
    var full: String = "",
    var explanation: String = "",
    var example: String = "",
    var category: String = "",
    var authorUid: String? = null,  // if created by user
    var isLocalOnly: Boolean = false // used for the prepopulated set
)
