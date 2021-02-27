package com.triagung.notesapp.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.triagung.notesapp.R
import com.triagung.notesapp.database.NotesDatabase
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_ADD_NOTE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageAddNoteMain = findViewById<ImageView>(R.id.imageAddNoteMain)
        imageAddNoteMain.setOnClickListener {
            startActivityForResult(
                Intent(applicationContext, CreateNoteActivity::class.java),
                REQUEST_CODE_ADD_NOTE
            )
        }

        getNotes()
    }

    private fun getNotes() {
//        @SuppressLint("StaticFieldLeak")
//        class GetNoteTask : AsyncTask<Void, Void, List<Note>>() {
//            override fun doInBackground(vararg p0: Void?): List<Note> {
//                return NotesDatabase.getDatabase(applicationContext).noteDao().getAllNotes()
//            }
//
//            override fun onPostExecute(notes: List<Note>?) {
//                super.onPostExecute(notes)
//                Log.d("MY_NOTES", notes.toString())
//            }
//        }
//
//        GetNoteTask().execute()
//
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            val notes = NotesDatabase.getDatabase(applicationContext).noteDao().getAllNotes()

            handler.post {
                Log.d("MY_NOTES", notes.toString())
            }
        }
    }
}