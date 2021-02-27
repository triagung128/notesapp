package com.triagung.notesapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.triagung.notesapp.R

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
    }
}