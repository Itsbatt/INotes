package com.itsbatt.inotes.domain

/**
 * Represents a parsed line inside a note body so checklists, dividers,
 * drawings, images, tables, headings, and plain text can be rendered interactively.
 */
sealed class NoteLine {
    data class Checklist(val text: String, val checked: Boolean) : NoteLine()
    object Divider : NoteLine()
    data class Drawing(val path: String) : NoteLine()
    data class ImageAttachment(val uri: String) : NoteLine()
    data class TableRow(val cells: List<String>) : NoteLine()
    data class Plain(val text: String) : NoteLine()
}

object ChecklistUtils {
    private const val UNCHECKED_PREFIX = "- [ ] "
    private const val CHECKED_PREFIX = "- [x] "
    private const val CHECKED_PREFIX_UPPER = "- [X] "
    private const val DRAWING_PREFIX = "[drawing:"
    private const val IMAGE_PREFIX = "[image:"

    fun parseLines(body: String): List<NoteLine> {
        if (body.isEmpty()) return listOf(NoteLine.Plain(""))
        return body.split("\n").map { raw ->
            when {
                raw.startsWith(UNCHECKED_PREFIX) ->
                    NoteLine.Checklist(raw.removePrefix(UNCHECKED_PREFIX), checked = false)
                raw.startsWith(CHECKED_PREFIX) ->
                    NoteLine.Checklist(raw.removePrefix(CHECKED_PREFIX), checked = true)
                raw.startsWith(CHECKED_PREFIX_UPPER) ->
                    NoteLine.Checklist(raw.removePrefix(CHECKED_PREFIX_UPPER), checked = true)
                raw.trim() == "---" ->
                    NoteLine.Divider
                raw.startsWith(DRAWING_PREFIX) && raw.endsWith("]") ->
                    NoteLine.Drawing(raw.removePrefix(DRAWING_PREFIX).dropLast(1))
                raw.startsWith(IMAGE_PREFIX) && raw.endsWith("]") ->
                    NoteLine.ImageAttachment(raw.removePrefix(IMAGE_PREFIX).dropLast(1))
                raw.startsWith("|") && raw.endsWith("|") && raw.length >= 3 -> {
                    val cells = raw.drop(1).dropLast(1).split("|").map { it.trim() }
                    NoteLine.TableRow(cells)
                }
                else ->
                    NoteLine.Plain(raw)
            }
        }
    }

    fun serializeLines(lines: List<NoteLine>): String {
        return lines.joinToString("\n") { line ->
            when (line) {
                is NoteLine.Checklist ->
                    (if (line.checked) CHECKED_PREFIX else UNCHECKED_PREFIX) + line.text
                is NoteLine.Divider -> "---"
                is NoteLine.Drawing -> "$DRAWING_PREFIX${line.path}]"
                is NoteLine.ImageAttachment -> "$IMAGE_PREFIX${line.uri}]"
                is NoteLine.TableRow -> "| " + line.cells.joinToString(" | ") + " |"
                is NoteLine.Plain -> line.text
            }
        }
    }

    fun toggleChecklistAt(lines: List<NoteLine>, index: Int): List<NoteLine> {
        if (index !in lines.indices) return lines
        val target = lines[index] as? NoteLine.Checklist ?: return lines
        return lines.toMutableList().apply {
            this[index] = target.copy(checked = !target.checked)
        }
    }

    fun formatPreview(body: String): String {
        return body.lines()
            .filter { it.isNotBlank() && it.trim() != "---" }
            .joinToString("  ") { line ->
                when {
                    line.startsWith(UNCHECKED_PREFIX) -> "○ " + line.removePrefix(UNCHECKED_PREFIX)
                    line.startsWith(CHECKED_PREFIX) -> "✓ " + line.removePrefix(CHECKED_PREFIX)
                    line.startsWith(CHECKED_PREFIX_UPPER) -> "✓ " + line.removePrefix(CHECKED_PREFIX_UPPER)
                    line.startsWith(DRAWING_PREFIX) -> "✏️ Dibujo"
                    line.startsWith(IMAGE_PREFIX) -> "🖼️ Foto"
                    line.startsWith("|") && line.endsWith("|") -> line.replace("|", " ").trim()
                    else -> line
                }
            }
    }

    fun countWordsAndChars(title: String, body: String): Pair<Int, Int> {
        val cleanBody = body.lines().joinToString(" ") { line ->
            when {
                line.startsWith(UNCHECKED_PREFIX) -> line.removePrefix(UNCHECKED_PREFIX)
                line.startsWith(CHECKED_PREFIX) -> line.removePrefix(CHECKED_PREFIX)
                line.startsWith(CHECKED_PREFIX_UPPER) -> line.removePrefix(CHECKED_PREFIX_UPPER)
                line.trim() == "---" || line.startsWith(DRAWING_PREFIX) || line.startsWith(IMAGE_PREFIX) -> ""
                else -> line
            }
        }
        val combined = "$title $cleanBody".trim()
        val words = if (combined.isEmpty()) 0 else combined.split(Regex("\\s+")).size
        val chars = (title + cleanBody.replace(" ", "")).length
        return words to chars
    }
}
