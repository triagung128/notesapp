package com.triagung.notesapp.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.triagung.notesapp.R
import com.triagung.notesapp.adapters.NotesAdapter
import com.triagung.notesapp.database.NotesDatabase
import com.triagung.notesapp.entities.Note
import com.triagung.notesapp.listeners.NotesListener
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), NotesListener {

    companion object {
        const val REQUEST_CODE_ADD_NOTE = 1
        const val REQUEST_CODE_UPDATE_NOTE = 2
        const val REQUEST_CODE_SHOW_NOTES = 3
    }

    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter

    private val notesList: ArrayList<Note> = ArrayList()
    private var noteClickedPosition: Int = -1

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

        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        notesRecyclerView.layoutManager = StaggeredGridLayoutManager(
            2, StaggeredGridLayoutManager.VERTICAL
        )

        notesAdapter = NotesAdapter(notesList, this)
        notesRecyclerView.adapter = notesAdapter

        getNotes(REQUEST_CODE_SHOW_NOTES, false)
    }

    override fun onNoteClicked(note: Note, position: Int) {
        noteClickedPosition = position
        val intent = Intent(this, CreateNoteActivity::class.java)
        intent.putExtra("isViewOrUpdate", true)
        intent.putExtra("note", note)
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE)
    }

    private fun getNotes(requestCode: Int, isNoteDeleted: Boolean) {
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
                when (requestCode) {
                    REQUEST_CODE_SHOW_NOTES -> {
                        notesList.addAll(notes)
                        notesAdapter.notifyDataSetChanged()
                    }
                    REQUEST_CODE_ADD_NOTE -> {
                        notesList.add(0, notes[0])
                        notesAdapter.notifyItemInserted(0)
                        notesRecyclerView.smoothScrollToPosition(0)
                    }
                    REQUEST_CODE_UPDATE_NOTE -> {
                        notesList.removeAt(noteClickedPosition)
                        if (isNoteDeleted) {
                            notesAdapter.notifyItemRemoved(noteClickedPosition)
                        } else {
                            notesList.add(noteClickedPosition, notes[noteClickedPosition])
                            notesAdapter.notifyItemChanged(noteClickedPosition)
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false)
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNotDeleted", false))
            }
        }
    }
}