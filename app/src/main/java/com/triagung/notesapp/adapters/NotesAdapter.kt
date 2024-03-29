package com.triagung.notesapp.adapters

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.RoundedImageView
import com.triagung.notesapp.R
import com.triagung.notesapp.entities.Note
import com.triagung.notesapp.listeners.NotesListener
import java.util.*
import kotlin.collections.ArrayList

class NotesAdapter(private var notes: List<Note>, private val notesListener: NotesListener)
    : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>()
{
    private var timer: Timer? = null
    private val notesSource: List<Note> = notes

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_container_note, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.setNote(notes[position])
        holder.layoutNote.setOnClickListener {
            notesListener.onNoteClicked(notes[position], position)
        }
    }

    override fun getItemCount(): Int = notes.size

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutNote: LinearLayout = itemView.findViewById(R.id.layoutNote)

        private val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        private val textSubtitle: TextView = itemView.findViewById(R.id.textSubtitle)
        private val textDateTime: TextView = itemView.findViewById(R.id.textDateTime)
        private val imageNote: RoundedImageView = itemView.findViewById(R.id.imageNote)

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

            if (note.imagePath != null) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.imagePath))
                imageNote.visibility = View.VISIBLE
            } else {
                imageNote.visibility = View.GONE
            }
        }
    }

    fun searchNotes(searchKeyword: String) {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                notes = 
                    if (searchKeyword.trim().isEmpty()) {
                        notesSource
                    } else {
                        val temp: ArrayList<Note> = ArrayList()
                        for (note in notesSource) {
                            if (note.title?.toLowerCase()?.contains(searchKeyword.toLowerCase())!!
                                || note.subtitle?.toLowerCase()?.contains(searchKeyword.toLowerCase())!!
                                || note.noteText?.toLowerCase()?.contains(searchKeyword.toLowerCase())!!
                            ) {
                                temp.add(note)
                            }
                        }
                        temp
                    }

                Handler(Looper.getMainLooper()).post {
                    notifyDataSetChanged()
                }
            }

        }, 500)
    }

    fun cancelTimer() {
        if (timer != null) {
            timer?.cancel()
        }
    }
}
