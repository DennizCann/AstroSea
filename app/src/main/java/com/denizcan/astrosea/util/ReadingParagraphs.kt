package com.denizcan.astrosea.util

/** Keep paragraph boundaries and bound even an unusually long single paragraph. */
fun readingParagraphs(text: String, maxLength: Int = 1200): List<String> {
    require(maxLength > 0)
    return text.replace("\r\n", "\n").split(Regex("\n\\s*\n")).flatMap { paragraph ->
        buildList {
            var rest = paragraph.trim()
            while (rest.length > maxLength) {
                var end = rest.lastIndexOf(' ', maxLength)
                if (end < maxLength / 2) end = maxLength
                if (end > 0 && Character.isHighSurrogate(rest[end - 1])) end--
                if (end == 0) end = 2 // Keep a surrogate pair together for a tiny test limit.
                add(rest.substring(0, end))
                rest = rest.substring(end).trimStart()
            }
            if (rest.isNotBlank()) add(rest)
        }
    }
}
