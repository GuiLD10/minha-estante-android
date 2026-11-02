package com.minhaestante.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ComStatusTest {

    @Test
    fun `quando muda para LIDO com total conhecido, entao a pagina atual vira o total`() {
        val resultado = livro(totalPaginas = 300, paginaAtual = 120, status = StatusLeitura.LENDO)
            .comStatus(StatusLeitura.LIDO)

        assertEquals(StatusLeitura.LIDO, resultado.status)
        assertEquals(300, resultado.paginaAtual)
    }

    @Test
    fun `quando muda para LIDO com total desconhecido, entao mantem a pagina atual`() {
        val resultado = livro(totalPaginas = null, paginaAtual = 120, status = StatusLeitura.LENDO)
            .comStatus(StatusLeitura.LIDO)

        assertEquals(StatusLeitura.LIDO, resultado.status)
        assertEquals(120, resultado.paginaAtual)
    }

    @Test
    fun `quando muda para QUERO_LER, entao a pagina atual volta para zero`() {
        val resultado = livro(paginaAtual = 120, status = StatusLeitura.LENDO)
            .comStatus(StatusLeitura.QUERO_LER)

        assertEquals(StatusLeitura.QUERO_LER, resultado.status)
        assertEquals(0, resultado.paginaAtual)
    }

    @Test
    fun `quando muda de LIDO para QUERO_LER, entao a pagina atual volta para zero`() {
        val resultado = livro(totalPaginas = 300, paginaAtual = 300, status = StatusLeitura.LIDO)
            .comStatus(StatusLeitura.QUERO_LER)

        assertEquals(0, resultado.paginaAtual)
    }

    @Test
    fun `quando muda para LENDO, entao mantem a pagina atual`() {
        val resultado = livro(paginaAtual = 0, status = StatusLeitura.QUERO_LER)
            .comStatus(StatusLeitura.LENDO)

        assertEquals(StatusLeitura.LENDO, resultado.status)
        assertEquals(0, resultado.paginaAtual)
    }

    @Test
    fun `quando muda de LIDO para LENDO, entao mantem a pagina atual`() {
        val resultado = livro(totalPaginas = 300, paginaAtual = 300, status = StatusLeitura.LIDO)
            .comStatus(StatusLeitura.LENDO)

        assertEquals(StatusLeitura.LENDO, resultado.status)
        assertEquals(300, resultado.paginaAtual)
    }

    @Test
    fun `quando muda o status, entao o livro original nao e alterado`() {
        val original = livro(paginaAtual = 120, status = StatusLeitura.LENDO)

        original.comStatus(StatusLeitura.QUERO_LER)

        assertEquals(120, original.paginaAtual)
        assertEquals(StatusLeitura.LENDO, original.status)
    }
}
