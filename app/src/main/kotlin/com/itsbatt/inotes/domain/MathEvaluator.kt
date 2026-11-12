package com.itsbatt.inotes.domain

/**
 * Lightweight safe arithmetic evaluator for iOS-style inline math (e.g. "125 * 4 =").
 */
object MathEvaluator {
    fun tryEvaluateTrailingEquals(line: String): String? {
        val trimmed = line.trim()
        if (!trimmed.endsWith("=") || trimmed.length < 2) return null
        val expr = trimmed.dropLast(1).trim()
        if (expr.isEmpty() || !expr.any { it.isDigit() }) return null
        // Only allow numbers, operators, parentheses, spaces, and decimal points
        if (!expr.all { it.isDigit() || it in "+-*/().×÷ " }) return null

        val normalized = expr.replace('×', '*').replace('÷', '/').replace(" ", "")
        return try {
            val result = Parser(normalized).parse()
            if (result.isNaN() || result.isInfinite()) null
            else if (result == result.toLong().toDouble()) result.toLong().toString()
            else String.format(java.util.Locale.US, "%.4f", result).trimEnd('0').trimEnd('.')
        } catch (_: Exception) {
            null
        }
    }

    private class Parser(private val s: String) {
        private var pos = 0

        fun parse(): Double {
            val v = parseExpr()
            if (pos < s.length) throw IllegalArgumentException("Unexpected char")
            return v
        }

        private fun parseExpr(): Double {
            var x = parseTerm()
            while (pos < s.length) {
                when (s[pos]) {
                    '+' -> { pos++; x += parseTerm() }
                    '-' -> { pos++; x -= parseTerm() }
                    else -> return x
                }
            }
            return x
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (pos < s.length) {
                when (s[pos]) {
                    '*' -> { pos++; x *= parseFactor() }
                    '/' -> {
                        pos++
                        val d = parseFactor()
                        if (d == 0.0) throw ArithmeticException("Div by 0")
                        x /= d
                    }
                    else -> return x
                }
            }
            return x
        }

        private fun parseFactor(): Double {
            if (pos < s.length && s[pos] == '+') { pos++; return parseFactor() }
            if (pos < s.length && s[pos] == '-') { pos++; return -parseFactor() }
            if (pos < s.length && s[pos] == '(') {
                pos++
                val v = parseExpr()
                if (pos >= s.length || s[pos] != ')') throw IllegalArgumentException("Missing )")
                pos++
                return v
            }
            val start = pos
            while (pos < s.length && (s[pos].isDigit() || s[pos] == '.')) pos++
            if (start == pos) throw IllegalArgumentException("Expected number")
            return s.substring(start, pos).toDouble()
        }
    }
}
