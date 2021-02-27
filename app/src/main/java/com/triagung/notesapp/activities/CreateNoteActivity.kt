package com.triagung.notesapp.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
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

    private lateinit var viewSubtitleIndicator: View

    private var selectedNoteColor: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        val imageBack = findViewById<ImageView>(R.id.imageBack)
        imageBack.setOnClickListener { onBackPressed() }

        inputNoteTitle = findViewById(R.id.inputNoteTitle)
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle)
        inputNoteText = findViewById(R.id.inputNote)
        textDateTime = findViewById(R.id.textDateTime)
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator)

        textDateTime.text = SimpleDateFormat(
            "EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()
        ).format(Date())

        val imageSave = findViewById<ImageView>(R.id.imageSave)
        imageSave.setOnClickListener { saveNote() }

        selectedNoteColor = "#333333"

        initMiscellaneous()
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
        note.color = selectedNoteColor

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

    private fun initMiscellaneous() {
        val layoutMiscellaneous: LinearLayout = findViewById(R.id.layoutMiscellaneous)
        val bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous)
        layoutMiscellaneous.findViewById<TextView>(R.id.textMiscellaneous).setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        val imageColor1: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor1)
        val imageColor2: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor2)
        val imageColor3: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor3)
        val imageColor4: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor4)
        val imageColor5: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor5)

        layoutMiscellaneous.findViewById<View>(R.id.viewColor1).setOnClickListener {
            selectedNoteColor = "#333333"
            imageColor1.setImageResource(R.drawable.ic_done)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.findViewById<View>(R.id.viewColor2).setOnClickListener {
            selectedNoteColor = "#FDBE3B"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(R.drawable.ic_done)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.findViewById<View>(R.id.viewColor3).setOnClickListener {
            selectedNoteColor = "#FF4842"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(R.drawable.ic_done)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.findViewById<View>(R.id.viewColor4).setOnClickListener {
            selectedNoteColor = "#3A52FC"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(R.drawable.ic_done)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.findViewById<View>(R.id.viewColor5).setOnClickListener {
            selectedNoteColor = "#000000"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(R.drawable.ic_done)
            setSubtitleIndicatorColor()
        }
    }

    private fun setSubtitleIndicatorColor() {
        val gradientDrawable = viewSubtitleIndicator.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor))
    }
}