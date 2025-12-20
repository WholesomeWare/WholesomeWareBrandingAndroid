package com.csakitheone.wholesomeware.experiment

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.time.LocalDate

class EventRecommendationAI {
    companion object {

        private suspend fun getEventSources(): String = withContext(Dispatchers.IO) {
            val urlKoloraFacebookEvents =
                "https://www.facebook.com/profile.php?id=100094619973134&sk=events"
            val promptKolora = urlKoloraFacebookEvents.toHttpUrl().toUrl().readText().let {
                if (!it.contains("facebook.com/events/")) return@let "No upcoming events found."

                it.substringAfter("facebook.com/events/")
                    .substringBefore("facebook.com/profile.php?")
                    .replace("""[ \n\r\t]+""".toRegex(), " ")
                    .replace("""<[^>]*>""".toRegex(), "<>")
                    .replace("""(<> ?)+""".toRegex(), "<>")
                    .take(20_000)
            }

            val urlArtefolkWebsite = "https://www.artefolk.hu/programok"
            val promptArtefolk = urlArtefolkWebsite.toHttpUrl().toUrl().readText().let {
                it.substringAfter("<section id=\"programok-fejlec\"")
                    .substringAfter(">")
                    .substringBefore("</main>")
                    .replace("""&[^;]*;""".toRegex(), " ")
                    .replace("""[ \n\r\t]+""".toRegex(), " ")
                    .replace("""<[^>]*>""".toRegex(), "<>")
                    .replace("""(<> ?)+""".toRegex(), "<>")
                    .trim()
                    .take(5_000)
            }

            return@withContext """
                Kolora Egyesület Facebook események:
                $promptKolora
                
                Alba Regia Táncegyüttes programok:
                $promptArtefolk
            """.trimIndent()
        }

        suspend fun getUpcomingEventsSummary(): String? {
            val eventSources = getEventSources()
            Log.d("EventRecommendationAI", "Today's date: ${LocalDate.now()}")

            val model = Firebase.ai(backend = GenerativeBackend.vertexAI())
                .generativeModel("gemini-2.5-flash-lite")
            val prompt = "Írj egy rövid összefoglalót a következő közelgő eseményekről, amelyeket több weboldalról szedtem össze. Nem kell semmi extra szöveg, se formázás, csak az összefoglaló, magyarul. Múltbéli eseményekről ne írj! Mai dátum: ${LocalDate.now()}\n$eventSources"
            Log.d("EventRecommendationAI", "Prompt sent to AI model (${prompt.length}):\n$prompt")
            val response = model.generateContent(prompt)
            return response.text
        }

    }
}