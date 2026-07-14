package com.csakitheone.wholesomeware.data

import java.time.LocalDateTime

data class InkoFesztEvent(
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val stage: String,
    val emoji: String,
)

class InkognitoFesztRepository {
    companion object {

        const val facebookEventUrl = "https://www.facebook.com/events/2341812502895160"

        val programs = listOf(
            // July 17
            InkoFesztEvent("Megnyitó", LocalDateTime.of(2026, 7, 17, 18, 0), null, "Külső színpad", "🎉"),
            InkoFesztEvent("Slam Poetry Open Mic", LocalDateTime.of(2026, 7, 17, 18, 5), LocalDateTime.of(2026, 7, 17, 19, 15), "Külső színpad", "🎤"),
            InkoFesztEvent("Unpluggeddon koncert", LocalDateTime.of(2026, 7, 17, 19, 30), LocalDateTime.of(2026, 7, 17, 20, 40), "Külső színpad", "🎸"),
            InkoFesztEvent("Akusztikus Jam Session", LocalDateTime.of(2026, 7, 17, 20, 50), LocalDateTime.of(2026, 7, 17, 21, 30), "Külső színpad", "🎶"),

            // July 18
            InkoFesztEvent("The Out", LocalDateTime.of(2026, 7, 18, 16, 0), LocalDateTime.of(2026, 7, 18, 17, 20), "Külső színpad", "🎵"),
            InkoFesztEvent("🤫", LocalDateTime.of(2026, 7, 18, 17, 30), LocalDateTime.of(2026, 7, 18, 18, 50), "Külső színpad", "🎸"),
            InkoFesztEvent("Féltucat", LocalDateTime.of(2026, 7, 18, 19, 0), LocalDateTime.of(2026, 7, 18, 20, 20), "Külső színpad", "🎤"),
            InkoFesztEvent("N’drews Acoustic", LocalDateTime.of(2026, 7, 18, 20, 30), LocalDateTime.of(2026, 7, 18, 21, 30), "Külső színpad", "🎶"),
            InkoFesztEvent("INKognito Talk – Művészet (Diákszemből a műalkotás)", LocalDateTime.of(2026, 7, 18, 17, 30), LocalDateTime.of(2026, 7, 18, 18, 0), "Belső színpad", "🎙️"),
            InkoFesztEvent("Kiállítás megnyitó", LocalDateTime.of(2026, 7, 18, 18, 0), null, "Belső színpad", "🖼️"),
            InkoFesztEvent("Kiállítás látogatható", LocalDateTime.of(2026, 7, 18, 19, 0), LocalDateTime.of(2026, 7, 18, 20, 0), "Belső színpad", "🎨"),
            InkoFesztEvent("DJ Set / Chill", LocalDateTime.of(2026, 7, 18, 20, 20), LocalDateTime.of(2026, 7, 18, 21, 40), "Belső színpad", "🎧"),

            // July 19
            InkoFesztEvent("Zene Nélkül Duó", LocalDateTime.of(2026, 7, 19, 16, 0), LocalDateTime.of(2026, 7, 19, 17, 30), "Külső színpad", "🎼"),
            InkoFesztEvent("30-as Tábla – akusztikus koncert", LocalDateTime.of(2026, 7, 19, 17, 45), LocalDateTime.of(2026, 7, 19, 21, 30), "Külső színpad", "🎸"),
            InkoFesztEvent("INKognito Talk – Vörös Janka- Zene világa", LocalDateTime.of(2026, 7, 19, 17, 30), LocalDateTime.of(2026, 7, 19, 18, 0), "Belső színpad", "🎙️"),
            InkoFesztEvent("Kiállítás folyamatosan látogatható", LocalDateTime.of(2026, 7, 19, 18, 0), LocalDateTime.of(2026, 7, 19, 22, 0), "Belső színpad", "🖼️"),
        ).sortedBy { it.startTime }

    }
}
