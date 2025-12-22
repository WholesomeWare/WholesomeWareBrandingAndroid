package com.csakitheone.wholesomeware.model

data class Artist(
    val name: String,
    val socials: Map<String, String> = emptyMap(),
) {
    override fun toString(): String {
        return name
    }
}

val ARTIST_CSAKI = Artist(
    name = "Csaki The One",
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