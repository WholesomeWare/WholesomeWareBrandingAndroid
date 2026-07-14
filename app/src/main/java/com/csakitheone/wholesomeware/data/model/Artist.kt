package com.csakitheone.wholesomeware.data.model

data class Artist(
    val name: String,
    val socials: Map<String, String> = emptyMap(),
) {
    override fun toString(): String {
        return name
    }
}

val ARTIST_CSAKI = Artist(
    name = "Csáki",
    socials = mapOf(
        "Instagram" to "https://www.instagram.com/csakitheone/",
    ),
)

val ARTIST_HELKA = Artist(
    name = "Helka",
    socials = mapOf(
        "Weboldal" to "https://www.helkamusic.hu/",
        "Zenés Insta" to "https://www.instagram.com/helkamusic/",
        "Illusztrációs Insta" to "https://www.instagram.com/helkauniverzum/",
    ),
)

val ARTIST_M_LIA = Artist(
    name = "M. Lia",
)

val ARTIST_EMSI_KOVACS = Artist(
    name = "Kovács Emese",
    socials = mapOf(
        "Instagram" to "https://www.instagram.com/emsi_kovacs/",
        "TikTok" to "https://www.tiktok.com/@emese.kovcs76",
        "YouTube" to "https://www.youtube.com/@emesekovacs4007",
    ),
)

val ARTIST_UNKNOWN = Artist(
    name = "Ismeretlen",
)
