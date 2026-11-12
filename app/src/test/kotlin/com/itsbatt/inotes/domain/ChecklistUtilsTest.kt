package com.itsbatt.inotes.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChecklistUtilsTest {

    @Test
    fun parseAndSerializeRoundTripPreservesChecklistsDividersAndPlainText() {
        val raw = "Intro\n- [ ] Leche\n- [x] Pan\n---\nFin"
        val parsed = ChecklistUtils.parseLines(raw)

        assertEquals(5, parsed.size)
        assertEquals(NoteLine.Plain("Intro"), parsed[0])
        assertEquals(NoteLine.Checklist("Leche", checked = false), parsed[1])
        assertEquals(NoteLine.Checklist("Pan", checked = true), parsed[2])
        assertTrue(parsed[3] is NoteLine.Divider)
        assertEquals(NoteLine.Plain("Fin"), parsed[4])

        assertEquals(raw, ChecklistUtils.serializeLines(parsed))
    }

    @Test
    fun toggleChecklistAtFlipsCheckedState() {
        val lines = ChecklistUtils.parseLines("- [ ] Comprar café\n- [x] Llamar")
        val toggled = ChecklistUtils.toggleChecklistAt(lines, 0)

        assertEquals(NoteLine.Checklist("Comprar café", checked = true), toggled[0])
        assertEquals(NoteLine.Checklist("Llamar", checked = true), toggled[1])
    }

    @Test
    fun formatPreviewRendersCheckSymbolsAndIgnoresDividers() {
        val preview = ChecklistUtils.formatPreview("- [ ] Leche\n---\n- [x] Pan")
        assertEquals("○ Leche  ✓ Pan", preview)
    }

    @Test
    fun countWordsAndCharsIgnoresMarkdownPrefixes() {
        val (words, chars) = ChecklistUtils.countWordsAndChars("Lista", "- [ ] Leche\n- [x] Pan\n---")
        assertEquals(3, words) // Lista, Leche, Pan
        assertEquals(13, chars) // Lista (5) + Leche (5) + Pan (3)
    }
}
