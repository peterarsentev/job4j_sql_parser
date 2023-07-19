package ru.job4j.parser

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.*

internal class LexTokenTest {

    @Test
    fun whenSingleSelect() {
        val sql = "SELECT name FROM users"
        val expected = mapOf(
            1 to "SELECT",
            2 to "name",
            3 to "FROM",
            4 to "users",)
        assertThat(LexToken().split(sql)).isEqualTo(expected)
    }

    @Test
    fun whenSelectByAsterisk() {
        val sql = "SELECT * FROM books"
        assertThat(LexToken().parse(sql))
            .isEqualTo(
                Query(listOf("*"), listOf(Source("books")))
            )
    }

    @Test
    fun whenSelectImplicitly() {
        val sql = "SELECT name FROM users"
        assertThat(LexToken().parse(sql))
            .isEqualTo(
                Query(listOf("name"), listOf(Source("users")))
            )
    }

    @Test
    fun whenSelectMultiColumns() {
        val sql = "SELECT name, email FROM users"
        assertThat(LexToken().parse(sql))
            .isEqualTo(
                Query(listOf("name", "email"), listOf(Source("users")))
            )
    }

    @Test
    fun whenSelectMultiTable() {
        val sql = "SELECT * FROM users, roles"
        assertThat(LexToken().parse(sql))
            .isEqualTo(
                Query(listOf("*"), listOf(Source("users"), Source("roles")))
            )
    }
}