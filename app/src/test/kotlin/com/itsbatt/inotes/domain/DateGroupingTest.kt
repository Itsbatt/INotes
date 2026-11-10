package com.itsbatt.inotes.domain

import com.itsbatt.inotes.data.room.entities.Note
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DateGroupingTest {

    @Test
    fun testDateGroupingHoyAyerEstaSemanaMesYAno() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 10, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val fixedNow = cal.timeInMillis

        // Note 1: Hoy (same day 10:00)
        val noteHoy = Note(id = 1, title = "Nota Hoy", updatedAt = fixedNow - 2 * 3600 * 1000)

        // Note 2: Ayer (previous day)
        val noteAyer = Note(id = 2, title = "Nota Ayer", updatedAt = fixedNow - 26 * 3600 * 1000)

        // Note 3: Esta semana (4 days ago)
        val noteEstaSemana = Note(id = 3, title = "Nota Esta semana", updatedAt = fixedNow - 4 * 24 * 3600 * 1000)

        // Note 4: Mes anterior / año (45 days ago)
        val noteMesYAno = Note(id = 4, title = "Nota Mes Anterior", updatedAt = fixedNow - 45L * 24 * 3600 * 1000)

        val groups = DateGroupingUtils.groupNotesByDate(
            listOf(noteHoy, noteAyer, noteEstaSemana, noteMesYAno),
            nowMillis = fixedNow
        )

        val groupTitles = groups.map { it.first }
        assertTrue("Debe contener sección 'Hoy'", groupTitles.contains("Hoy"))
        assertTrue("Debe contener sección 'Ayer'", groupTitles.contains("Ayer"))
        assertTrue("Debe contener sección 'Esta semana'", groupTitles.contains("Esta semana"))
        assertTrue("Debe contener sección de mes y año", groupTitles.size >= 4)

        assertEquals("Nota Hoy", groups.first { it.first == "Hoy" }.second[0].title)
        assertEquals("Nota Ayer", groups.first { it.first == "Ayer" }.second[0].title)
        assertEquals("Nota Esta semana", groups.first { it.first == "Esta semana" }.second[0].title)
    }
}
