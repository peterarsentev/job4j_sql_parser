package ru.job4j.parser

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.*

internal class LexTokenTest {

    @Test
    fun whenSelectByAsterisk() {
        val sql = "SELECT * FROM books"
        val query = LexToken().parse(sql)
        assertThat(query.columns)
            .isEqualTo(listOf("*"))
    }

    @Test
    fun whenSelectImplicitly() {
        val sql = "SELECT name FROM users"
        val query = LexToken().parse(sql)
        assertThat(query.columns)
            .isEqualTo(listOf("name"))
    }

    @Test
    fun whenSelectMultiColumns() {
        val sql = "SELECT name, email FROM users"
        val query = LexToken().parse(sql)
        assertThat(query.columns)
            .isEqualTo(listOf("name", "email"))
    }

    @Test
    fun whenSelectMultiTable() {
        val sql = "SELECT * FROM users, roles"
        assertThat(LexToken().parse(sql).from)
            .isEqualTo(
                listOf(Query.Source("users"), Query.Source("roles"))
            )
    }
}