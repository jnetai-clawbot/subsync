package com.jnetaol.subsync.engine

import com.jnetaol.subsync.data.model.SubtitleEntry
import com.jnetaol.subsync.logger.DebugLogger
import java.io.File

data class SubtitleMatch(
    val title: String,
    val language: String,
    val year: String = "",
    val matchScore: Int = 80,
    val downloadUrl: String = "",
    val format: String = "SRT",
    val entryCount: Int = 0
)

object SubtitleEngine {

    fun parseSrt(content: String): List<SubtitleEntry> {
        DebugLogger.i("SS-030", "Parsing SRT content, length: ${content.length}")
        val entries = mutableListOf<SubtitleEntry>()
        val blocks = content.trim().split(Regex("\\n\\s*\\n"))

        for (block in blocks) {
            val lines = block.trim().split("\n")
            if (lines.size < 3) continue

            val index = lines[0].trim().toIntOrNull() ?: continue
            val timeMatch = Regex("(\\d{2}):(\\d{2}):(\\d{2})[.,](\\d{3})\\s*-->\\s*(\\d{2}):(\\d{2}):(\\d{2})[.,](\\d{3})")
                .find(lines[1].trim()) ?: continue

            val (h1, m1, s1, ms1, h2, m2, s2, ms2) = timeMatch.destructured
            val startMs = h1.toLong() * 3600000 + m1.toLong() * 60000 + s1.toLong() * 1000 + ms1.toLong()
            val endMs = h2.toLong() * 3600000 + m2.toLong() * 60000 + s2.toLong() * 1000 + ms2.toLong()

            val text = lines.drop(2).joinToString("\n").trim()

            entries.add(
                SubtitleEntry(
                    trackId = 0,
                    index = index,
                    startMs = startMs,
                    endMs = endMs,
                    text = text,
                    translatedText = ""
                )
            )
        }

        DebugLogger.i("SS-031", "Parsed ${entries.size} entries")
        return entries
    }

    fun writeSrt(entries: List<SubtitleEntry>, useTranslated: Boolean = false): String {
        DebugLogger.i("SS-032", "Writing SRT with ${entries.size} entries")
        val sb = StringBuilder()

        val sorted = entries.sortedBy { it.index }

        for ((i, entry) in sorted.withIndex()) {
            sb.appendLine(i + 1)
            sb.appendLine("${formatTimestamp(entry.startMs)} --> ${formatTimestamp(entry.endMs)}")
            val text = if (useTranslated && entry.translatedText.isNotBlank()) {
                entry.translatedText
            } else {
                entry.text
            }
            sb.appendLine(text)
            sb.appendLine()
        }

        return sb.toString()
    }

    fun formatTimestamp(ms: Long): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val millis = ms % 1000
        return "%02d:%02d:%02d,%03d".format(hours, minutes, seconds, millis)
    }

    fun applySyncOffset(entries: List<SubtitleEntry>, offsetMs: Long): List<SubtitleEntry> {
        DebugLogger.i("SS-033", "Applying sync offset: ${offsetMs}ms to ${entries.size} entries")
        return entries.map { entry ->
            entry.copy(
                startMs = (entry.startMs + offsetMs).coerceAtLeast(0),
                endMs = (entry.endMs + offsetMs).coerceAtLeast(0)
            )
        }
    }

    fun scaleTiming(entries: List<SubtitleEntry>, factor: Float): List<SubtitleEntry> {
        DebugLogger.i("SS-034", "Scaling timing by factor: $factor")
        return entries.map { entry ->
            entry.copy(
                startMs = (entry.startMs * factor).toLong(),
                endMs = (entry.endMs * factor).toLong()
            )
        }
    }

    fun adjustDuration(entries: List<SubtitleEntry>, deltaMs: Long): List<SubtitleEntry> {
        return entries.map { entry ->
            entry.copy(
                endMs = (entry.endMs + deltaMs).coerceAtLeast(entry.startMs + 100)
            )
        }
    }

    fun searchMatches(originalFilename: String): List<SubtitleMatch> {
        DebugLogger.i("SS-035", "Searching subtitles for: $originalFilename")
        val baseName = originalFilename.substringBeforeLast(".").lowercase()
            .replace(Regex("[._\\-]"), " ")
            .replace(Regex("\\d{4}"), "")
            .trim()

        val mockMatches = mutableListOf<SubtitleMatch>()
        val languages = listOf("English", "Spanish", "French", "German",
            "Portuguese", "Italian", "Russian", "Japanese", "Korean", "Chinese")

        for ((i, lang) in languages.withIndex()) {
            mockMatches.add(
                SubtitleMatch(
                    title = baseName.replaceFirstChar { it.uppercase() },
                    language = lang,
                    matchScore = (90 - i * 3).coerceAtLeast(50),
                    format = "SRT",
                    entryCount = (50 + (i * 17)).coerceAtMost(300)
                )
            )
        }

        return mockMatches
    }

    fun generateMockSubtitles(count: Int = 100): List<SubtitleEntry> {
        val entries = mutableListOf<SubtitleEntry>()
        val sampleLines = listOf(
            "Hello, how are you?",
            "Where are you going?",
            "I need to find the station.",
            "Can you help me with this?",
            "The weather is beautiful today.",
            "What time does the show start?",
            "I'd like to order some food.",
            "This is a great opportunity.",
            "Please wait for a moment.",
            "Thank you for your help.",
            "I don't understand what you mean.",
            "Let's meet at the same place.",
            "That sounds like a good idea.",
            "I've been waiting for hours.",
            "Could you repeat that please?",
            "It's getting late, we should go.",
            "Have you seen my phone?",
            "I'll be there in five minutes.",
            "What's the password for the Wi-Fi?",
            "She said it would be ready by noon."
        )

        val interval = 4000L
        val duration = 3000L

        for (i in 0 until count) {
            val start = i * interval
            entries.add(
                SubtitleEntry(
                    trackId = 0,
                    index = i + 1,
                    startMs = start,
                    endMs = start + duration,
                    text = sampleLines.random()
                )
            )
        }

        return entries
    }

    fun importFromFile(entries: List<SubtitleEntry>): String? {
        DebugLogger.i("SS-036", "Importing ${entries.size} entries")
        return entries.firstOrNull()?.text
    }
}
