package com.itsbatt.inotes.domain

import com.itsbatt.inotes.data.room.entities.Note
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateGroupingUtils {

    fun groupNotesByDate(notes: List<Note>, nowMillis: Long = System.currentTimeMillis()): List<Pair<String, List<Note>>> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = cal.timeInMillis

        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStart = cal.timeInMillis

        cal.add(Calendar.DAY_OF_YEAR, -6)
        val thisWeekStart = cal.timeInMillis

        val hoy = mutableListOf<Note>()
        val ayer = mutableListOf<Note>()
        val estaSemana = mutableListOf<Note>()
        val mesYAno = mutableListOf<Note>()

        for (note in notes) {
            when {
                note.updatedAt >= todayStart -> hoy.add(note)
                note.updatedAt >= yesterdayStart -> ayer.add(note)
                note.updatedAt >= thisWeekStart -> estaSemana.add(note)
                else -> mesYAno.add(note)
            }
        }

        val result = mutableListOf<Pair<String, List<Note>>>()
        if (hoy.isNotEmpty()) result.add("Hoy" to hoy)
        if (ayer.isNotEmpty()) result.add("Ayer" to ayer)
        if (estaSemana.isNotEmpty()) result.add("Esta semana" to estaSemana)
        if (mesYAno.isNotEmpty()) {
            // Group older notes by month and year, e.g. "Noviembre de 2026" or "Mes y año"
            val monthGroups = mesYAno.groupBy { note ->
                SimpleDateFormat("MMMM 'de' yyyy", Locale("es")).format(Date(note.updatedAt)).replaceFirstChar { it.uppercase() }
            }
            monthGroups.forEach { (monthLabel, groupNotes) ->
                result.add(monthLabel to groupNotes)
            }
        }

        return result
    }

    fun formatNoteDate(timeMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val diff = nowMillis - timeMillis
        val noteDate = Date(timeMillis)

        return when {
            diff < 24 * 60 * 60 * 1000 -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(noteDate)
            }
            diff < 7 * 24 * 60 * 60 * 1000 -> {
                SimpleDateFormat("EEE", Locale("es")).format(noteDate).replaceFirstChar { it.uppercase() }
            }
            else -> {
                SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(noteDate)
            }
        }
    }
}
