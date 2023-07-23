package ru.job4j.parser

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class QueryParserTest {

    @Test
    fun parseQueryWithColumnsAndFrom() {
        val query = LexToken().parse("SELECT name, age FROM users")
        assertEquals(listOf("name", "age"), query.columns)
        assertEquals(listOf(Query.Source("users")), query.from)
    }

    @Test
    fun parseQueryWithJoins() {
        val query = LexToken().parse("SELECT name, age FROM users JOIN orders ON users.id = orders.user_id")
        assertEquals(listOf(Query.Join("orders", "users.id = orders.user_id")), query.joins)
    }

    @Test
    fun parseQueryWithWhereClause() {
        val query = LexToken().parse("SELECT name, age FROM users WHERE age > 18")
        assertEquals(listOf(Query.WhereClause("age > 18")), query.whereClauses)
    }

    @Test
    fun parseQueryWithGroupBy() {
        val query = LexToken().parse("SELECT name, COUNT(*) FROM users GROUP BY name")
        assertThat(query.groupBy)
            .isEqualTo(listOf("name"))
    }

    @Test
    fun parseQueryWithSort() {
        val query = LexToken().parse("SELECT name, age FROM users ORDER BY age DESC")
        assertThat(query.sortBy)
            .isEqualTo(listOf(Query.Sort("age DESC")))
    }

    @Test
    fun parseQueryWithPaging() {
        val query = LexToken().parse("SELECT name, age FROM users LIMIT 10 OFFSET 20")
        assertEquals(Query.Paging(10, 20), query.paging)
    }
}