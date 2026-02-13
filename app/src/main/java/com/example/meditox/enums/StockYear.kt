package com.example.meditox.enums

enum class StockYear(val year: Int) {
    Y2023(2023),
    Y2024(2024),
    Y2025(2025),
    Y2026(2026),
    Y2027(2027),
    Y2028(2028),
    Y2029(2029),
    Y2030(2030);

    override fun toString(): String = year.toString()
}
