package com.itsbatt.inotes.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration

/**
 * Lightweight 1:1 VisualTransformation that styles inline Markdown (**bold**, *italic*,
 * __underline__, ~~strike~~, ==highlight==), #tags, URLs, and [[id:title]] internal links
 * without altering character offsets so cursor positioning stays exact.
 */
class RichTextTransformation(
    private val accentColor: Color = IOSYellow,
    private val linkColor: Color = Color(0xFF0A84FF),
    private val highlightBg: Color = Color(0x55FFD60A),
    private val markerAlphaColor: Color = Color(0x668E8E93)
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val builder = AnnotatedString.Builder(raw)

        // **bold**
        Regex("\\*\\*(.+?)\\*\\*").findAll(raw).forEach { m ->
            builder.addStyle(SpanStyle(color = markerAlphaColor), m.range.first, m.range.first + 2)
            builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), m.range.first + 2, m.range.last - 1)
            builder.addStyle(SpanStyle(color = markerAlphaColor), m.range.last - 1, m.range.last + 1)
        }

        // *italic* (avoid matching **bold**)
        Regex("(?<!\\*)\\*(?!\\*)([^*\\n]+?)(?<!\\*)\\*(?!\\*)").findAll(raw).forEach { m ->
            builder.addStyle(SpanStyle(color = markerAlphaColor), m.range.first, m.range.first + 1)
            builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), m.range.first + 1, m.range.last)
            builder.addStyle(SpanStyle(color = markerAlphaColor), m.range.last, m.range.last + 1)
        }

        // __underline__
        Regex("__(.+?)__").findAll(raw).forEach { m ->
            builder.addStyle(SpanStyle(color = markerAlphaColor), m.range.first, m.range.first + 2)
            builder.addStyle(SpanStyle(textDecoration = TextDecoration.Underline), m.range.first + 2, m.range.last - 1)
            builder.addStyle(SpanStyle(color = markerAlphaColor), m.range.last - 1, m.range.last + 1)
        }

        // ~~strikethrough~~
        Regex("~~(.+?)~~").findAll(raw).forEach { m ->
            builder.addStyle(SpanStyle(color = markerAlphaColor), m.range.first, m.range.first + 2)
            builder.addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), m.range.first + 2, m.range.last - 1)
            builder.addStyle(SpanStyle(color = markerAlphaColor), m.range.last - 1, m.range.last + 1)
        }

        // ==highlight==
        Regex("==(.+?)==").findAll(raw).forEach { m ->
            builder.addStyle(SpanStyle(color = markerAlphaColor), m.range.first, m.range.first + 2)
            builder.addStyle(SpanStyle(background = highlightBg), m.range.first + 2, m.range.last - 1)
            builder.addStyle(SpanStyle(color = markerAlphaColor), m.range.last - 1, m.range.last + 1)
        }

        // #tags
        Regex("#[\\p{L}\\w_-]+").findAll(raw).forEach { m ->
            builder.addStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.SemiBold), m.range.first, m.range.last + 1)
        }

        // URLs
        Regex("https?://[^\\s]+").findAll(raw).forEach { m ->
            builder.addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline), m.range.first, m.range.last + 1)
        }

        // [[id:title]] internal note links
        Regex("\\[\\[(\\d+):([^\\]]+)]]").findAll(raw).forEach { m ->
            builder.addStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline), m.range.first, m.range.last + 1)
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}

sealed class DetectedLink {
    data class WebUrl(val url: String) : DetectedLink()
    data class Email(val address: String) : DetectedLink()
    data class Phone(val number: String) : DetectedLink()
    data class InternalNote(val noteId: Long, val title: String) : DetectedLink()
}

object LinkDetector {
    fun detect(text: String): List<DetectedLink> {
        val result = mutableListOf<DetectedLink>()
        Regex("\\[\\[(\\d+):([^\\]]+)]]").findAll(text).forEach { m ->
            val id = m.groupValues[1].toLongOrNull() ?: return@forEach
            result.add(DetectedLink.InternalNote(id, m.groupValues[2]))
        }
        Regex("https?://[^\\s]+").findAll(text).forEach { m ->
            result.add(DetectedLink.WebUrl(m.value))
        }
        Regex("[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}").findAll(text).forEach { m ->
            result.add(DetectedLink.Email(m.value))
        }
        Regex("(?<!\\d)\\+?\\d[\\d\\s-]{6,14}\\d(?!\\d)").findAll(text).forEach { m ->
            result.add(DetectedLink.Phone(m.value.trim()))
        }
        return result.distinct()
    }
}
