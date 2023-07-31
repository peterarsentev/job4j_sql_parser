package ru.job4j.parser

data class Query(val columns: List<String> = emptyList(),
                 val from: List<Source> = emptyList(),
                 val joins: List<Join> = emptyList(),
                 val whereClauses: List<WhereClause> = emptyList(),
                 val groupBy: List<String> = emptyList(),
                 val sortBy: List<Sort> = emptyList(),
                 val having: String = "",
                 val paging: Paging = Paging(0, 0)) {
    data class Source(val exp: String)
    data class Join(val source: String, val on: String)
    data class WhereClause(val exp: String)
    data class Sort(val exp: String)
    data class Paging(val limit: Int, val offset: Int)
}