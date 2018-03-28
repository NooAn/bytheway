package ru.a1024bits.bytheway.util

/**
 * Created by Andrei_Gusenkov on 3/16/2018.
 */
class Translit {
    private fun cyr2lat(ch: Char): String {
        when (ch) {
            'А' -> return "A"
            'Б' -> return "B"
            'В' -> return "V"
            'Г' -> return "G"
            'Д' -> return "D"
            'Е' -> return "E"
            'Ё' -> return "JE"
            'Ж' -> return "ZH"
            'З' -> return "Z"
            'И' -> return "I"
            'Й' -> return "Y"
            'К' -> return "K"
            'Л' -> return "L"
            'М' -> return "M"
            'Н' -> return "N"
            'О' -> return "O"
            'П' -> return "P"
            'Р' -> return "R"
            'С' -> return "S"
            'Т' -> return "T"
            'У' -> return "U"
            'Ф' -> return "F"
            'Х' -> return "KH"
            'Ц' -> return "C"
            'Ч' -> return "CH"
            'Ш' -> return "SH"
            'Щ' -> return "JSH"
            'Ъ' -> return "HH"
            'Ы' -> return "IH"
            'Ь' -> return "JH"
            'Э' -> return "EH"
            'Ю' -> return "JU"
            'Я' -> return "JA"
            else -> return ch.toString()
        }
    }

    fun cyr2lat(s: String): String {
        val sb = StringBuilder(s.length * 2)
        for (ch in s.toUpperCase().toCharArray()) {
            sb.append(cyr2lat(ch))
        }
        return sb.toString()
    }
}