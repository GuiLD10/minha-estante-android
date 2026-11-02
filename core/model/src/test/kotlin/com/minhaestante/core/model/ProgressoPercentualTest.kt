package com.minhaestante.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressoPercentualTest {

    @Test
    fun `quando o total de paginas e nulo, entao o progresso e nulo`() {
        assertNull(livro(totalPaginas = null, paginaAtual = 10).progressoPercentual())
    }

    @Test
    fun `quando o total de paginas e zero, entao o progresso e nulo`() {
        assertNull(livro(totalPaginas = 0, paginaAtual = 10).progressoPercentual())
    }

    @Test
    fun `quando esta na pagina zero, entao o progresso e 0`() {
        assertEquals(0, livro(totalPaginas = 300, paginaAtual = 0).progressoPercentual())
    }

    @Test
    fun `quando leu metade, entao o progresso e 50`() {
        assertEquals(50, livro(totalPaginas = 300, paginaAtual = 150).progressoPercentual())
    }

    @Test
    fun `quando a divisao nao e exata, entao o progresso e arredondado para baixo`() {
        // 100 * 100 / 300 = 33,33... → divisão inteira resulta em 33
        assertEquals(33, livro(totalPaginas = 300, paginaAtual = 100).progressoPercentual())
    }

    @Test
    fun `quando esta na ultima pagina, entao o progresso e 100`() {
        assertEquals(100, livro(totalPaginas = 300, paginaAtual = 300).progressoPercentual())
    }

    @Test
    fun `quando a pagina atual passa do total, entao o progresso e limitado a 100`() {
        assertEquals(100, livro(totalPaginas = 300, paginaAtual = 450).progressoPercentual())
    }

    @Test
    fun `quando a pagina atual e negativa, entao o progresso e limitado a 0`() {
        assertEquals(0, livro(totalPaginas = 300, paginaAtual = -5).progressoPercentual())
    }
}
