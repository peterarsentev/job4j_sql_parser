package ru.job4j.parser

import ru.job4j.parser.GrammarSQL.Companion.EMPTY
import ru.job4j.parser.GrammarSQL.Companion.FROM
import ru.job4j.parser.GrammarSQL.Companion.FULL
import ru.job4j.parser.GrammarSQL.Companion.GROUP
import ru.job4j.parser.GrammarSQL.Companion.HAVING
import ru.job4j.parser.GrammarSQL.Companion.JOIN
import ru.job4j.parser.GrammarSQL.Companion.LEFT
import ru.job4j.parser.GrammarSQL.Companion.LIMIT
import ru.job4j.parser.GrammarSQL.Companion.OFFSET
import ru.job4j.parser.GrammarSQL.Companion.ON
import ru.job4j.parser.GrammarSQL.Companion.ORDER
import ru.job4j.parser.GrammarSQL.Companion.RIGHT
import ru.job4j.parser.GrammarSQL.Companion.SELECT
import ru.job4j.parser.GrammarSQL.Companion.WHERE

class LexToken {

    fun String.grammar()  : Pair<LexAnalysis, Query> =  Pair(LexAnalysis(this), Query())

    fun Pair<LexAnalysis, Query>.select() : Pair<LexAnalysis, Query>
            = Pair(this.first, this.second.copy(columns = SelectFrom(this.first).get()))

    fun Pair<LexAnalysis, Query>.from() : Pair<LexAnalysis, Query>
            = Pair(this.first, this.second.copy(from = From(this.first).get()))

    fun Pair<LexAnalysis, Query>.join() : Pair<LexAnalysis, Query>
            = Pair(this.first, this.second.copy(joins = Join(this.first).get()))

    fun Pair<LexAnalysis, Query>.where() : Pair<LexAnalysis, Query>
            = Pair(this.first, this.second.copy(whereClauses = Where(this.first).get()))

    fun Pair<LexAnalysis, Query>.groupBy() : Pair<LexAnalysis, Query>
            = Pair(this.first, this.second.copy(groupBy = GroupBy(this.first).get()))

    fun Pair<LexAnalysis, Query>.order() : Pair<LexAnalysis, Query>
            = Pair(this.first, this.second.copy(sortBy = OrderBy(this.first).get()))

    fun Pair<LexAnalysis, Query>.having() : Pair<LexAnalysis, Query>
            = Pair(this.first, this.second.copy(having = Having(this.first).get()))

    fun Pair<LexAnalysis, Query>.paging() : Pair<LexAnalysis, Query>
            = Pair(this.first, this.second.copy(paging = Paging(this.first).get()))

    fun Pair<LexAnalysis, Query>.asQuery() : Query = this.second

    fun parse(expr: String) : Query = expr.grammar()
        .select().from()
        .join().where()
        .groupBy().order()
        .having().paging()
        .asQuery()
}

interface GrammarSQL<T> {

    companion object {
        const val SELECT = "SELECT"
        const val FROM = "FROM"
        const val GROUP = "GROUP"
        const val WHERE = "WHERE"
        const val LIMIT = "LIMIT"
        const val OFFSET = "OFFSET"
        const val ORDER = "ORDER"
        const val JOIN = "JOIN"
        const val ON = "ON"
        const val RIGHT = "RIGHT"
        const val FULL = "FULL"
        const val LEFT = "LEFT"
        const val HAVING = "HAVING"
        val EMPTY = emptyList<String>()
    }

    fun get() : T
}

infix operator fun String.div(that: String) : List<String> = listOf(this, that)

infix operator fun List<String>.div(that: String) : List<String> {
    val list = ArrayList<String>()
    this.forEach { list.add(it) }
    list.add(that)
    return list
}

infix operator fun String.rangeTo(endAt: String) : Pair<String, List<String>> = Pair(this, listOf(endAt))

infix operator fun String.rangeTo(endAt: List<String>) : Pair<String, List<String>> = Pair(this, endAt)

class SelectFrom(val lex: LexAnalysis) : GrammarSQL<List<String>> {
    override fun get(): List<String> =
        lex.range(SELECT .. FROM)
            .map { if (it.endsWith(",")) it.substring(0, it.length - 1) else it }
            .toList()
}

class From(val lex: LexAnalysis) : GrammarSQL<List<Query.Source>> {
    override fun get(): List<Query.Source> =
        lex.range(FROM .. RIGHT / LEFT / FULL / WHERE / JOIN / GROUP / ORDER / LIMIT / OFFSET / JOIN)
            .joinToString(" ")
            .split(",")
            .map { it.trim() }
            .map { Query.Source(it) }
            .toList().toList()
}

class Join(val lex: LexAnalysis) : GrammarSQL<List<Query.Join>> {
    override fun get(): List<Query.Join> {
        val source = lex.range(JOIN .. ON)
        val on = lex.range(ON .. WHERE / JOIN / GROUP / ORDER / LIMIT / OFFSET)
            .joinToString(separator = " ")
        if (source.isEmpty()) return emptyList()
        return listOf(Query.Join(source[0], on))
    }
}

class Where(val lex: LexAnalysis) : GrammarSQL<List<Query.WhereClause>> {
    override fun get(): List<Query.WhereClause> {
        val where = lex.range(WHERE .. ORDER / GROUP / OFFSET / LIMIT)
            .joinToString(separator = " ")
        return listOf(Query.WhereClause(where))
    }
}

class GroupBy(val lex: LexAnalysis) : GrammarSQL<List<String>> {
    override fun get(): List<String>
            = lex.range(GROUP .. HAVING / ORDER / LIMIT / OFFSET).drop(1)
}

class Having(val lex: LexAnalysis) : GrammarSQL<String> {
    override fun get(): String {
        return lex.range(HAVING .. LIMIT / OFFSET)
            .joinToString(separator = " ")
    }
}

class OrderBy(val lex: LexAnalysis) : GrammarSQL<List<Query.Sort>> {
    override fun get(): List<Query.Sort> {
        return lex.range(ORDER .. GROUP / LIMIT / OFFSET)
            .drop(1)
            .joinToString(separator = " ")
            .split(",")
            .map { Query.Sort(it) }
            .toList()
    }
}

class Paging(val lex: LexAnalysis) : GrammarSQL<Query.Paging> {
    override fun get(): Query.Paging {
        val limit = lex.range(LIMIT .. OFFSET)
        val offset = lex.range(OFFSET .. EMPTY)
        return Query.Paging(
            if (limit.isEmpty()) 0 else limit.iterator().next().toInt(),
            if (offset.isEmpty()) 0 else offset.iterator().next().toInt()
        )
    }
}


