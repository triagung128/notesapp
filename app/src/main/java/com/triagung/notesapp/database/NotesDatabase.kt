package com.triagung.notesapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.triagung.notesapp.dao.NoteDao
import com.triagung.notesapp.entities.Note

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var INSTANCE: NotesDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context) : NotesDatabase {
            if (INSTANCE == null) {
                synchronized(NotesDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                            NotesDatabase::class.java, "notes_db"
                        ).build()
                    }
                }
            }

            return INSTANCE as NotesDatabase
        }
    }

    abstract fun noteDao() : NoteDao
}