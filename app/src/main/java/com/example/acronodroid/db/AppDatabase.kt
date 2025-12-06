package com.example.acronodroid.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.acronodroid.models.Acronym
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Acronym::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun acronymDao(): AcronymDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "acronodroid_db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate with curated acronyms on a background thread
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                prepopulate(database.acronymDao())
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        // Prepopulate with sample acronyms
        private suspend fun prepopulate(dao: AcronymDao) {
            val samples = SampleAcronyms.get()
            for (a in samples) dao.insert(a)
        }
    }
}
