package com.csakitheone.wholesomeware.experiment

class DartsHelper {
    companion object {

        fun getLeastThrowsToWin(
            startingScore: Int,
            excludePreciseThrows: Boolean = false,
        ): List<String> {
            var score = startingScore
            val possibleThrows = if (excludePreciseThrows) (1..20).flatMap {
                listOf(
                    it to "$it",
                    it * 2 to "2x$it",
                )
            }.toMap()
            else (1..20).flatMap {
                listOf(
                    it to "$it",
                    it * 2 to "2x$it",
                    it * 3 to "3x$it",
                )
            }.toMap() + mapOf(25 to "Bullseye (25)", 50 to "Double bullseye (50)")
            val throwsList = mutableListOf<String>()
            while (score > 0) {
                val throwEntry = possibleThrows.entries
                    .filter { it.key <= score }
                    .maxByOrNull { it.key } ?: break
                throwsList.add(throwEntry.value)
                score -= throwEntry.key
            }
            return throwsList
        }

    }
}