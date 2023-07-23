package ru.job4j.parser

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

    fun parse(expr: String) : Query {
        val lex = LexAnalysis(expr)
        return Query(
            SelectFrom(lex).get(),
            From(lex).get(),
            Join(lex).get(),
            Where(lex).get(),
            GroupBy(lex).get(),
            OrderBy(lex).get(),
            Having(lex).get(),
            Paging(lex).get()
        )
    }
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
    }

    fun get() : T
}

class SelectFrom(val lex: LexAnalysis) : GrammarSQL<List<String>> {
    override fun get(): List<String> =
        lex.data(listOf(SELECT), listOf(FROM))
            .map { if (it.endsWith(",")) it.substring(0, it.length - 1) else it }
            .toList()
}

class From(val lex: LexAnalysis) : GrammarSQL<List<Query.Source>> {
    override fun get(): List<Query.Source> =
        lex.data(listOf(FROM), listOf(
            RIGHT, LEFT, FULL, WHERE, JOIN, GROUP, ORDER, LIMIT, OFFSET, JOIN)
        ).joinToString(" ")
            .split(",")
            .map { it.trim() }
            .map { Query.Source(it) }
            .toList().toList()
}

class Join(val lex: LexAnalysis) : GrammarSQL<List<Query.Join>> {
    override fun get(): List<Query.Join> {
        val source = lex.data(
            listOf(JOIN), listOf(ON)
        )
        val on = lex.data(listOf(ON),
            listOf(
                WHERE, JOIN, GROUP, ORDER, LIMIT, OFFSET)
        ).joinToString(separator = " ")
        if (source.isEmpty()) return emptyList()
        return listOf(Query.Join(source[0], on))
    }
}

class Where(val lex: LexAnalysis) : GrammarSQL<List<Query.WhereClause>> {
    override fun get(): List<Query.WhereClause> {
        val where = lex.data(
            listOf(WHERE),
            listOf(ORDER, GROUP, OFFSET, LIMIT)
        ).joinToString(separator = " ")
        return listOf(Query.WhereClause(where))
    }
}

class GroupBy(val lex: LexAnalysis) : GrammarSQL<List<String>> {
    override fun get(): List<String> {
        return lex.data(
            listOf(GROUP),
            listOf(HAVING, ORDER, LIMIT, OFFSET)
        ).drop(1)
    }
}

class Having(val lex: LexAnalysis) : GrammarSQL<String> {
    override fun get(): String {
        return lex.data(
            listOf(HAVING),
            listOf(LIMIT, OFFSET)
        ).joinToString(separator = " ")
    }
}

class OrderBy(val lex: LexAnalysis) : GrammarSQL<List<Query.Sort>> {
    override fun get(): List<Query.Sort> {
        return lex.data(
            listOf(ORDER),
            listOf(GROUP, LIMIT, OFFSET)
        ).drop(1)
            .joinToString(separator = " ")
            .split(",")
            .map { Query.Sort(it) }
            .toList()
    }
}

class Paging(val lex: LexAnalysis) : GrammarSQL<Query.Paging> {
    override fun get(): Query.Paging {
        val limit = lex.data(
            listOf(LIMIT),
            listOf(OFFSET)
        )
        val offset = lex.data(
            listOf(OFFSET),
            listOf()
        )
        return Query.Paging(
            if (limit.isEmpty()) 0 else limit.iterator().next().toInt(),
            if (offset.isEmpty()) 0 else offset.iterator().next().toInt()
        )
    }
}


