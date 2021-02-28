package com.triagung.notesapp.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.triagung.notesapp.R
import com.triagung.notesapp.database.NotesDatabase
import com.triagung.notesapp.entities.Note
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class CreateNoteActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_STORAGE_PERMISSION = 1
        const val REQUEST_CODE_SELECT_IMAGE = 2
    }

    private lateinit var inputNoteTitle: EditText
    private lateinit var inputNoteSubtitle: EditText
    private lateinit var inputNoteText: EditText

    private lateinit var textDateTime: TextView
    private lateinit var textWebURL: TextView

    private lateinit var imageNote: ImageView

    private lateinit var layoutWebURL: LinearLayout
    private lateinit var viewSubtitleIndicator: View

    private var selectedNoteColor: String = "#333333" //Default color note
    private var selectedImagePath: String = ""

    private var dialogAddURL: AlertDialog? = null
    private var dialogDeleteNote: AlertDialog? = null

    private var alreadyAvailableNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        val imageBack = findViewById<ImageView>(R.id.imageBack)
        imageBack.setOnClickListener { onBackPressed() }

        inputNoteTitle = findViewById(R.id.inputNoteTitle)
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle)
        inputNoteText = findViewById(R.id.inputNote)

        textDateTime = findViewById(R.id.textDateTime)
        textWebURL = findViewById(R.id.textWebURL)

        imageNote = findViewById(R.id.imageNote)

        layoutWebURL = findViewById(R.id.layoutWebURL)

        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator)

        textDateTime.text = SimpleDateFormat(
            "EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()
        ).format(Date())

        val imageSave = findViewById<ImageView>(R.id.imageSave)
        imageSave.setOnClickListener { saveNote() }

        if (intent.getBooleanExtra("isViewOrUpdate", false)) {
            alreadyAvailableNote = intent.getSerializableExtra("note") as Note
            setViewOrUpdateNote()
        }

        findViewById<ImageView>(R.id.imageRemoveWebURL).setOnClickListener {
            textWebURL.text = null
            layoutWebURL.visibility = View.GONE
        }

        findViewById<ImageView>(R.id.imageRemoveImage).setOnClickListener {
            imageNote.setImageBitmap(null)
            imageNote.visibility = View.GONE
            findViewById<ImageView>(R.id.imageRemoveImage).visibility = View.GONE
            selectedImagePath = ""
        }

        if (intent.getBooleanExtra("isFromQuickActions", false)) {
            val type: String? = intent.getStringExtra("quickActionType")
            if (type != null) {
                if (type == "image") {
                    selectedImagePath = intent.getStringExtra("imagePath")!!
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath))
                    imageNote.visibility = View.VISIBLE
                    findViewById<ImageView>(R.id.imageRemoveImage).visibility = View.VISIBLE
                } else if (type == "URL") {
                    textWebURL.text = intent.getStringExtra("URL")
                    layoutWebURL.visibility = View.VISIBLE
                }
            }
        }

        initMiscellaneous()
        setSubtitleIndicatorColor()
    }

    private fun setViewOrUpdateNote() {
        inputNoteTitle.setText(alreadyAvailableNote?.title)
        inputNoteSubtitle.setText(alreadyAvailableNote?.subtitle)
        inputNoteText.setText(alreadyAvailableNote?.noteText)
        textDateTime.text = alreadyAvailableNote?.dateTime

        if (alreadyAvailableNote?.imagePath != null
            && alreadyAvailableNote?.imagePath?.trim()?.isNotEmpty()!!
        ) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote?.imagePath))
            imageNote.visibility = View.VISIBLE
            findViewById<ImageView>(R.id.imageRemoveImage).visibility = View.VISIBLE
            selectedImagePath = alreadyAvailableNote?.imagePath!!
        }

        if (alreadyAvailableNote?.webLink != null
            && alreadyAvailableNote?.webLink?.trim()?.isNotEmpty()!!
        ) {
            textWebURL.text = alreadyAvailableNote?.webLink
            layoutWebURL.visibility = View.VISIBLE
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
        note.color = selectedNoteColor
        note.imagePath = selectedImagePath

        if (layoutWebURL.visibility == View.VISIBLE) {
            note.webLink = textWebURL.text.toString()
        }

        if (alreadyAvailableNote != null) {
            note.id = alreadyAvailableNote?.id!!
        }

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

        if (alreadyAvailableNote != null && alreadyAvailableNote?.color != null
            && alreadyAvailableNote?.color?.trim()?.isNotEmpty()!!
        ) {
            when (alreadyAvailableNote?.color) {
                "#FDBE3B" -> { layoutMiscellaneous.findViewById<View>(R.id.viewColor2).performClick() }
                "#FF4842" -> { layoutMiscellaneous.findViewById<View>(R.id.viewColor3).performClick() }
                "#3A52FC" -> { layoutMiscellaneous.findViewById<View>(R.id.viewColor4).performClick() }
                "#000000" -> { layoutMiscellaneous.findViewById<View>(R.id.viewColor5).performClick() }
            }
        }

        layoutMiscellaneous.findViewById<LinearLayout>(R.id.layoutAddImage).setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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

        layoutMiscellaneous.findViewById<LinearLayout>(R.id.layoutAddUrl).setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            showAddURLDialog()
        }

        if (alreadyAvailableNote != null) {
            layoutMiscellaneous.findViewById<LinearLayout>(R.id.layoutDeleteNote).visibility = View.VISIBLE
            layoutMiscellaneous.findViewById<LinearLayout>(R.id.layoutDeleteNote).setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                showDeleteNoteDialog()
            }
        }
    }

    private fun showDeleteNoteDialog() {
        if (dialogDeleteNote == null) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            val view: View = LayoutInflater
                .from(this)
                .inflate(R.layout.layout_delete_note, findViewById(R.id.layoutDeleteNoteContainer))

            builder.setView(view)
            dialogDeleteNote = builder.create()

            if (dialogDeleteNote?.window != null) {
                dialogDeleteNote?.window?.setBackgroundDrawable(ColorDrawable(0))
            }

            view.findViewById<TextView>(R.id.textDeleteNote).setOnClickListener {
                val executor = Executors.newSingleThreadExecutor()
                val handler = Handler(Looper.getMainLooper())

                executor.execute {
                    NotesDatabase.getDatabase(applicationContext)
                        .noteDao()
                        .deleteNote(alreadyAvailableNote!!)

                    handler.post {
                        val intent = Intent()
                        intent.putExtra("isNoteDeleted", true)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
            }

            view.findViewById<TextView>(R.id.textCancel).setOnClickListener {
                dialogDeleteNote?.dismiss()
            }
        }

        dialogDeleteNote?.show()
    }

    private fun setSubtitleIndicatorColor() {
        val gradientDrawable = viewSubtitleIndicator.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                val selectedImageUri: Uri? = data.data
                if (selectedImageUri != null) {
                    try {
                        val inputStream: InputStream? = contentResolver.openInputStream(selectedImageUri)
                        val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                        imageNote.setImageBitmap(bitmap)
                        imageNote.visibility = View.VISIBLE
                        findViewById<ImageView>(R.id.imageRemoveImage).visibility = View.VISIBLE
                        selectedImagePath = getPathFromUri(selectedImageUri)
                    } catch (exception: Exception) {
                        Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                    }
                }
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
                    textWebURL.text = inputURL.text.toString()
                    layoutWebURL.visibility = View.VISIBLE
                    dialogAddURL?.dismiss()
                }
            }

            view.findViewById<TextView>(R.id.textCancel).setOnClickListener {
                dialogAddURL?.dismiss()
            }
        }

        dialogAddURL?.show()
    }
}