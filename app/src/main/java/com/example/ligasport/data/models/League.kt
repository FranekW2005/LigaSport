package com.example.ligasport.data.models

/**
 * Reprezentuje ligę w systemie.
 * @param id - identyfikator ligi
 * @param name - nazwa ligi
 * @param adminId - identyfikator użytkownika, który utworzył ligę
 */
data class League(val id: String = "", val name: String = "", val adminId: String = "")