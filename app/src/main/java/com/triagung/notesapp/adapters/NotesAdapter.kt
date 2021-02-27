package com.triagung.notesapp.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.triagung.notesapp.R
import com.triagung.notesapp.entities.Note
import javax.xml.transform.OutputKeys

class NotesAdapter(private val notes: List<Note>) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_container_note, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.setNote(notes[position])
    }

    override fun getItemCount(): Int = notes.size

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        private val textSubtitle: TextView = itemView.findViewById(R.id.textSubtitle)
        private val textDateTime: TextView = itemView.findViewById(R.id.textDateTime)
        private val layoutNote: LinearLayout = itemView.findViewById(R.id.layoutNote)

        fun setNote(note: Note) {
            textTitle.text = note.title
            if (note.subtitle?.trim().isNullOrEmpty()) {
                textSubtitle.visibility = View.GONE
            } else {
                textSubtitle.text = note.subtitle
            }
            textDateTime.text = note.dateTime

            val gradientDrawable = layoutNote.background as GradientDrawable
            if (note.color != null) {
                gradientDrawable.setColor(Color.parseColor(note.color))
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"))
            }
        }
    }
}