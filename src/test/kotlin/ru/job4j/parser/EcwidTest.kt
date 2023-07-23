package ru.job4j.parser

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.*

internal class EcwidTest {

    @Test
    fun whenSelectByAsterisk() {
        val sql = "SELECT * FROM book"
        val query = LexToken().parse(sql)
        assertThat(query.columns)
            .isEqualTo(listOf("*"))
        assertThat(query.from)
            .isEqualTo(listOf(Query.Source("book")))
    }

    @Test
    fun whenSelectImplicitly() {
        val sql = "SELECT author.name, count(book.id), sum(book.cost) " +
                "FROM author " +
                "LEFT JOIN book ON (author.id = book.author_id) " +
                "GROUP BY author.name " +
                "HAVING COUNT(*) > 1 AND SUM(book.cost) > 500 " +
                "LIMIT 10"
        val query = LexToken().parse(sql)
        assertThat(query.columns)
            .isEqualTo(listOf("author.name", "count(book.id)", "sum(book.cost)"))
        assertThat(query.from)
            .isEqualTo(listOf(Query.Source("author")))
        assertThat(query.joins)
            .isEqualTo(listOf(Query.Join("book", "(author.id = book.author_id)")))
        assertThat(query.groupBy)
            .isEqualTo(listOf("author.name"))
        assertThat(query.having)
            .isEqualTo("COUNT(*) > 1 AND SUM(book.cost) > 500")
        assertThat(query.paging)
            .isEqualTo(Query.Paging(10, 0))
    }

    @Test
    fun whenSubSelect() {
        val sql = "SELECT * FROM (SELECT * FROM A) a_alias"
        val query = LexToken().parse(sql)
        assertThat(query.columns)
            .isEqualTo(listOf("*"))
        assertThat(query.from)
            .isEqualTo(listOf(Query.Source("(SELECT * FROM A) a_alias")))
    }
}