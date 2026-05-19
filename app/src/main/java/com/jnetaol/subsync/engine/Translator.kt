package com.jnetaol.subsync.engine

import com.jnetaol.subsync.logger.DebugLogger

object Translator {

    private val enToEs = mapOf(
        "hello" to "hola", "how" to "cómo", "are" to "estás", "you" to "tú",
        "where" to "dónde", "going" to "vas", "i" to "yo", "need" to "necesito",
        "to" to "a", "find" to "encontrar", "the" to "la", "station" to "estación",
        "can" to "puedes", "help" to "ayudar", "me" to "me", "with" to "con",
        "this" to "esto", "weather" to "clima", "is" to "es", "beautiful" to "hermoso",
        "today" to "hoy", "what" to "qué", "time" to "hora", "does" to "",
        "show" to "programa", "start" to "empieza", "like" to "gustaría",
        "order" to "pedir", "some" to "algo de", "food" to "comida",
        "great" to "gran", "opportunity" to "oportunidad",
        "please" to "por favor", "wait" to "espera", "moment" to "momento",
        "thank" to "gracias", "your" to "tu", "don't" to "no",
        "understand" to "entiendo", "mean" to "quieres decir",
        "let's" to "vamos a", "meet" to "encontrarnos",
        "at" to "en", "same" to "mismo", "place" to "lugar",
        "that" to "eso", "sounds" to "suena", "like" to "como",
        "good" to "buena", "idea" to "idea",
        "been" to "estado", "waiting" to "esperando", "hours" to "horas",
        "could" to "podrías", "repeat" to "repetir", "that" to "eso",
        "it's" to "es", "getting" to "está haciendo", "late" to "tarde",
        "we" to "nosotros", "should" to "deberíamos", "go" to "ir",
        "have" to "has", "seen" to "visto", "my" to "mi", "phone" to "teléfono",
        "be" to "estaré", "there" to "allí", "in" to "en", "five" to "cinco",
        "minutes" to "minutos", "password" to "contraseña", "wi-fi" to "wifi",
        "she" to "ella", "said" to "dijo", "would" to "estaría",
        "ready" to "listo", "by" to "para el", "noon" to "mediodía",
        "a" to "un/una", "an" to "un/una", "and" to "y",
        "for" to "por", "no" to "no", "yes" to "sí",
        "morning" to "mañana", "night" to "noche", "day" to "día",
        "man" to "hombre", "woman" to "mujer", "child" to "niño",
        "friend" to "amigo", "family" to "familia", "house" to "casa",
        "car" to "coche", "water" to "agua", "know" to "saber",
        "think" to "pensar", "want" to "querer", "love" to "amor",
        "work" to "trabajo", "school" to "escuela", "city" to "ciudad"
    )

    private val enToFr = mapOf(
        "hello" to "bonjour", "how" to "comment", "are" to "es",
        "you" to "vous", "where" to "où", "going" to "allez",
        "i" to "je", "need" to "ai besoin", "find" to "trouver",
        "station" to "gare", "help" to "aider", "me" to "moi",
        "weather" to "temps", "beautiful" to "beau", "today" to "aujourd'hui",
        "time" to "heure", "start" to "commence", "food" to "nourriture",
        "great" to "grand", "opportunity" to "opportunité",
        "please" to "s'il vous plaît", "wait" to "attendez",
        "thank" to "merci", "understand" to "comprends",
        "meet" to "rencontrer", "place" to "endroit",
        "good" to "bonne", "idea" to "idée",
        "late" to "tard", "should" to "devrions", "go" to "aller",
        "phone" to "téléphone", "minutes" to "minutes",
        "ready" to "prêt", "noon" to "midi",
        "morning" to "matin", "night" to "nuit", "day" to "jour",
        "friend" to "ami", "family" to "famille", "house" to "maison",
        "car" to "voiture", "water" to "eau", "love" to "amour"
    )

    private val enToDe = mapOf(
        "hello" to "hallo", "how" to "wie", "are" to "bist",
        "you" to "du", "where" to "wo", "going" to "gehst",
        "i" to "ich", "need" to "brauche", "find" to "finden",
        "station" to "bahnhof", "help" to "helfen", "me" to "mir",
        "weather" to "Wetter", "beautiful" to "schön", "today" to "heute",
        "time" to "Uhr", "start" to "beginnt", "food" to "Essen",
        "great" to "großartig", "please" to "bitte", "wait" to "warten",
        "thank" to "danke", "understand" to "verstehe",
        "good" to "gute", "idea" to "Idee",
        "late" to "spät", "go" to "gehen", "phone" to "Handy",
        "minutes" to "Minuten", "ready" to "fertig", "noon" to "Mittag",
        "morning" to "Morgen", "night" to "Nacht", "day" to "Tag",
        "friend" to "Freund", "house" to "Haus"
    )

    private val translationMaps = mapOf(
        "es" to enToEs,
        "fr" to enToFr,
        "de" to enToDe
    )

    fun translate(text: String, targetLanguage: String): String {
        DebugLogger.i("SS-040", "Translating to $targetLanguage: ${text.take(50)}...")
        val dict = translationMaps[targetLanguage] ?: enToEs

        val words = text.lowercase().split(Regex("\\s+"))
        val translated = words.joinToString(" ") { word ->
            val cleanWord = word.trimEnd(',', '.', '!', '?', ':', ';')
            val suffix = if (word.endsWith(',') || word.endsWith('.') || word.endsWith('!') ||
                word.endsWith('?') || word.endsWith(':') || word.endsWith(';')) {
                word.last().toString()
            } else ""

            dict[cleanWord]?.let { it + suffix } ?: word
        }

        return translated.replaceFirstChar { it.uppercase() }
    }

    fun translateEntries(
        entries: List<com.jnetaol.subsync.data.model.SubtitleEntry>,
        targetLanguage: String
    ): List<com.jnetaol.subsync.data.model.SubtitleEntry> {
        DebugLogger.i("SS-041", "Translating ${entries.size} entries to $targetLanguage")
        return entries.map { entry ->
            entry.copy(translatedText = translate(entry.text, targetLanguage))
        }
    }

    fun getSupportedLanguages(): List<Pair<String, String>> {
        return listOf(
            "en" to "English",
            "es" to "Español",
            "fr" to "Français",
            "de" to "Deutsch",
            "pt" to "Português",
            "it" to "Italiano",
            "ru" to "Русский",
            "ja" to "日本語",
            "ko" to "한국어",
            "zh" to "中文"
        )
    }
}
