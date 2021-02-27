package com.triagung.notesapp.listeners

import com.triagung.notesapp.entities.Note

interface NotesListener {
    fun onNoteClicked(note: Note, position: Int)
}