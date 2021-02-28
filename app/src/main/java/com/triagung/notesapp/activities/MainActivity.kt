package com.triagung.notesapp.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        const val REQUEST_CODE_SELECT_IMAGE = 4
        const val REQUEST_CODE_STORAGE_PERMISSION = 5
    }

    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter

    private val notesList: ArrayList<Note> = ArrayList()
    private var noteClickedPosition: Int = -1

    private var dialogAddURL: AlertDialog? = null

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

        val inputSearch = findViewById<EditText>(R.id.inputSearch)
        inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notesAdapter.cancelTimer()
            }

            override fun afterTextChanged(s: Editable?) {
                if (notesList.size != 0) {
                    notesAdapter.searchNotes(s.toString())
                }
            }
        })

        findViewById<ImageView>(R.id.imageAddNote).setOnClickListener {
            startActivityForResult(
                Intent(applicationContext, CreateNoteActivity::class.java),
                REQUEST_CODE_ADD_NOTE
            )
        }

        findViewById<ImageView>(R.id.imageAddImage).setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
            } else {
                selectImage()
            }
        }

        findViewById<ImageView>(R.id.imageWebLink).setOnClickListener {
            showAddURLDialog()
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getPathFromUri(contentUri: Uri) : String {
        val filePath: String
        val cursor: Cursor? = contentResolver.query(
            contentUri, null, null, null, null
        )

        if (cursor == null) {
            filePath = contentUri.path.toString()
        } else {
            cursor.moveToFirst()
            val index: Int = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }

        return filePath
    }

    override fun onNoteClicked(note: Note, position: Int) {
        noteClickedPosition = position

        val intent = Intent(this, CreateNoteActivity::class.java)
        intent.putExtra("isViewOrUpdate", true)
        intent.putExtra("note", note)

        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE)
    }

    private fun getNotes(requestCode: Int, isNoteDeleted: Boolean) {
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
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false))
            }
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                val selectedImageUri = data.data
                if (selectedImageUri != null) {
                    try {
                        val selectedImagePath = getPathFromUri(selectedImageUri)

                        val intent = Intent(applicationContext, CreateNoteActivity::class.java)
                        intent.putExtra("isFromQuickActions", true)
                        intent.putExtra("quickActionType", "image")
                        intent.putExtra("imagePath", selectedImagePath)

                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
                    } catch (exception: Exception) {
                        Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showAddURLDialog() {
        if (dialogAddURL == null) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            val view: View = LayoutInflater
                .from(this)
                .inflate(R.layout.layout_add_url, findViewById(R.id.layoutAddUrlContainer))

            builder.setView(view)

            dialogAddURL = builder.create()
            if (dialogAddURL?.window != null) {
                dialogAddURL?.window!!.setBackgroundDrawable(ColorDrawable(0))
            }

            val inputURL: EditText = view.findViewById(R.id.inputURL)
            inputURL.requestFocus()

            view.findViewById<TextView>(R.id.textAdd).setOnClickListener {
                if (inputURL.text.toString().trim().isEmpty()) {
                    Toast.makeText(this, "Enter URL", Toast.LENGTH_SHORT).show()
                } else if (!Patterns.WEB_URL.matcher(inputURL.text.toString()).matches()) {
                    Toast.makeText(this, "Enter valid URL", Toast.LENGTH_SHORT).show()
                } else {
                    dialogAddURL?.dismiss()

                    val intent = Intent(applicationContext, CreateNoteActivity::class.java)
                    intent.putExtra("isFromQuickActions", true)
                    intent.putExtra("quickActionType", "URL")
                    intent.putExtra("URL", inputURL.text.toString())

                    startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
                }
            }

            view.findViewById<TextView>(R.id.textCancel).setOnClickListener {
                dialogAddURL?.dismiss()
            }
        }

        dialogAddURL?.show()
    }
}