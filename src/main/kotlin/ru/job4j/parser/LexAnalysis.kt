package ru.job4j.parser

class LexAnalysis(expr: String) {
    private val tokens = ArrayList<String>()

    init {
        expr.replace("\\s+", " ")
            .replace("\\n", "")
            .split(" ")
            .forEach { tokens.add(it) }
    }

    fun data(startAt: List<String>, endAt: List<String>) : List<String> {
        var posStartAt = -1
        var posEndAt = tokens.size
        for ((pos, value) in tokens.withIndex()) {
            if (posStartAt == -1 && startAt.contains(value)) {
                posStartAt = pos
            }
            if (endAt.contains(value)) {
                posEndAt = pos
                break
            }
        }
        if (posStartAt == -1) return emptyList()
        val list = ArrayList<String>()
        for (index in posStartAt + 1 until posEndAt) {
            val value = tokens[index]
            list.add(value)
        }
        for (index in posEndAt - 1  downTo posStartAt) {
            tokens.removeAt(index)
        }
        return list
    }
}