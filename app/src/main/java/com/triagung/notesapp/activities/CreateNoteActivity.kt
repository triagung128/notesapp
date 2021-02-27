package com.triagung.notesapp.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.triagung.notesapp.R
import com.triagung.notesapp.database.NotesDatabase
import com.triagung.notesapp.entities.Note
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class CreateNoteActivity : AppCompatActivity() {

    private lateinit var inputNoteTitle: EditText
    private lateinit var inputNoteSubtitle: EditText
    private lateinit var inputNoteText: EditText
    private lateinit var textDateTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        val imageBack = findViewById<ImageView>(R.id.imageBack)
        imageBack.setOnClickListener { onBackPressed() }

        inputNoteTitle = findViewById(R.id.inputNoteTitle)
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle)
        inputNoteText = findViewById(R.id.inputNote)
        textDateTime = findViewById(R.id.textDateTime)

        textDateTime.text = SimpleDateFormat(
            "EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()
        ).format(Date())

        val imageSave = findViewById<ImageView>(R.id.imageSave)
        imageSave.setOnClickListener {
            saveNote()
        }
    }

    private fun saveNote() {
        if (inputNoteTitle.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Note title can't be empty", Toast.LENGTH_SHORT).show()
            return
        } else if (inputNoteSubtitle.text.toString().trim().isEmpty()
                && inputNoteText.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Note can't be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val note = Note()
        note.title = inputNoteTitle.text.toString()
        note.subtitle = inputNoteSubtitle.text.toString()
        note.noteText = inputNoteText.text.toString()
        note.dateTime = textDateTime.text.toString()

//        @SuppressLint("StaticFieldLeak")
//        class SaveNoteTask : AsyncTask<Void, Void, Void>() {
//            override fun doInBackground(vararg p0: Void?): Void? {
//                NotesDatabase.getDatabase(applicationContext).noteDao().insertNote(note)
//                return null
//            }
//
//            override fun onPostExecute(result: Void?) {
//                super.onPostExecute(result)
//                val intent = Intent()
//                setResult(RESULT_OK, intent)
//                finish()
//            }
//        }
//
//        SaveNoteTask().execute()

        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            NotesDatabase.getDatabase(applicationContext).noteDao().insertNote(note)

            handler.post {
                val intent = Intent()
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }
}