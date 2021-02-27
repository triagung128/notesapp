package com.triagung.notesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class CreateNoteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        val imageBack = findViewById<ImageView>(R.id.imageBack)
        imageBack.setOnClickListener { onBackPressed() }
    }
}