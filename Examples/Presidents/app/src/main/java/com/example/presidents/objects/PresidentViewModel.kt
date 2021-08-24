package com.example.presidents.objects

import java.util.ArrayList

/**
 * Handler for a list of presidents
 *
 */
object PresidentViewModel {

    /**
     * An array of presidents.
     */
    val ITEMS: MutableList<President> = ArrayList()

    init {
        // Add some sample items.
        // construct the data source
        ITEMS.add(President("Lauri Relander", 1925, 1931, "Toka presidentti"))
        ITEMS.add(President("P. E. Svinhufvud", 1931, 1937, "Kolmas presidentti"))
        ITEMS.add(President("Kaarlo Stahlberg", 1919, 1925, "Eka presidentti"))
        ITEMS.add(President("Kyösti Kallio", 1937, 1940, "Neljas presidentti"))
        ITEMS.add(President("Risto Ryti", 1940, 1944, "Viides presidentti"))
        ITEMS.add(President("Carl Gustaf Emil Mannerheim", 1944, 1946, "Kuudes presidentti"))
        ITEMS.add(President("Juho Kusti Paasikivi", 1946, 1956, "Äkäinen ukko"))
        ITEMS.add(President("Urho Kekkonen", 1956, 1982, "Pelimies"))
        ITEMS.add(President("Mauno Koivisto", 1982, 1994, "Manu"))
        ITEMS.add(President("Martti Ahtisaari", 1994, 2000, "Mahtisaari"))
        ITEMS.add(President("Tarja Halonen", 2000, 2012, "Eka naispresidentti"))
        ITEMS.add(President("Sauli Niinistö", 2012, 2024, "Ensimmäisen koiran, Lennun, omistaja"))

    }

    private fun addItem(item: President) {
        ITEMS.add(item)
    }

    private fun createPresident(name: String): President {
        return President(name, 0,0,"")
    }

    /**
     * A President item representing a piece of content.
     */
    data class President(val name: String, val StartYear: Int, val EndYear: Int, val fact: String) {
        override fun toString(): String = "$name,$StartYear,$EndYear,$fact"
    }
}