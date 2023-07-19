package ru.job4j.parser

import java.lang.RuntimeException
import java.util.stream.Collectors

class LexToken {

    fun split(expr : String) : Map<Int, String> {
        val rsl = HashMap<Int, String>()
        expr
            .replace("\\s+", " ")
            .replace("\\n", "")
            .split(" ")
            .forEachIndexed { index, value ->  rsl.put(index + 1, value) }
        return rsl
    }

    fun parse(expr: String) : Query {
        val tokens = split(expr)
        val selectBy = tokens.entries.stream()
            .filter { it.value == "SELECT" }
            .findFirst().orElseThrow()
        val columns = tokens.get(selectBy.key + 1)!!.split(",")
        val fromBy = tokens.entries.stream()
            .filter { it.value == "FROM" }
            .findFirst().orElseThrow()
        val sources = tokens.get(fromBy.key + 1)!!.split(",").stream()
            .map { Source(it) }
            .collect(Collectors.toList())
        return Query(columns, sources)
    }
}

data class Source(val exp: String)
data class Join(val exp: String)
data class WhereClause(val exp: String)
data class Sort(val exp: String)
data class Paging(val limit: Int, val offset: Int)

data class Query(val columns: List<String>, val from: List<Source>,
                 val joins: List<Join> = emptyList(),
                 val whereClauses: List<WhereClause> = emptyList(),
                 val groupBy: List<String> = emptyList(),
                 val sortBy: List<Sort> = emptyList(),
                 val paging: Paging = Paging(0, 0)
)