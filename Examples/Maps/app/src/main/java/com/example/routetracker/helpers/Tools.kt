package com.example.routetracker.helpers

// Math
fun Double.roundToDecimal(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return Math.round(this * multiplier) / multiplier
}