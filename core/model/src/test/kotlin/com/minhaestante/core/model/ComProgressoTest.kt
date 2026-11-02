package com.minhaestante.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ComProgressoTest {

    @Test
    fun `quando a pagina esta dentro do limite, entao ela e salva como informada`() {
        val resultado = livro(status = StatusLeitura.LENDO, paginaAtual = 10).comProgresso(120)

        assertEquals(120, resultado.paginaAtual)
        assertEquals(StatusLeitura.LENDO, resultado.status)
    }

    @Test
    fun `quando a pagina e negativa, entao ela e limitada a zero`() {
        val resultado = livro(status = StatusLeitura.LENDO, paginaAtual = 10).comProgresso(-3)

        assertEquals(0, resultado.paginaAtual)
    }

    @Test
    fun `quando a pagina passa do total, entao ela e limitada ao total`() {
        val resultado = livro(totalPaginas = 300, status = StatusLeitura.LENDO).comProgresso(999)

        assertEquals(300, resultado.paginaAtual)
    }

    @Test
    fun `quando o total e desconhecido, entao a pagina so e limitada a zero`() {
        val resultado = livro(totalPaginas = null, status = StatusLeitura.LENDO).comProgresso(999)

        assertEquals(999, resultado.paginaAtual)
        assertEquals(StatusLeitura.LENDO, resultado.status)
    }

    @Test
    fun `quando o total e desconhecido e a pagina e negativa, entao ela e limitada a zero`() {
        val resultado = livro(totalPaginas = null, status = StatusLeitura.LENDO).comProgresso(-1)

        assertEquals(0, resultado.paginaAtual)
    }

    @Test
    fun `quando estava em QUERO_LER e a pagina e maior que zero, entao passa para LENDO`() {
        val resultado = livro(status = StatusLeitura.QUERO_LER).comProgresso(1)

        assertEquals(StatusLeitura.LENDO, resultado.status)
        assertEquals(1, resultado.paginaAtual)
    }

    @Test
    fun `quando estava em QUERO_LER e a pagina e zero, entao continua em QUERO_LER`() {
        val resultado = livro(status = StatusLeitura.QUERO_LER).comProgresso(0)

        assertEquals(StatusLeitura.QUERO_LER, resultado.status)
    }

    @Test
    fun `quando estava em QUERO_LER e o total e desconhecido, entao passa para LENDO`() {
        val resultado = livro(totalPaginas = null, status = StatusLeitura.QUERO_LER).comProgresso(50)

        assertEquals(StatusLeitura.LENDO, resultado.status)
    }

    @Test
    fun `quando a pagina atinge o total, entao passa para LIDO`() {
        val resultado = livro(totalPaginas = 300, status = StatusLeitura.LENDO).comProgresso(300)

        assertEquals(StatusLeitura.LIDO, resultado.status)
        assertEquals(300, resultado.paginaAtual)
    }

    @Test
    fun `quando estava em QUERO_LER e vai direto para a ultima pagina, entao passa para LIDO`() {
        val resultado = livro(totalPaginas = 300, status = StatusLeitura.QUERO_LER).comProgresso(300)

        assertEquals(StatusLeitura.LIDO, resultado.status)
    }

    @Test
    fun `quando a pagina passa do total, entao passa para LIDO`() {
        val resultado = livro(totalPaginas = 300, status = StatusLeitura.LENDO).comProgresso(301)

        assertEquals(StatusLeitura.LIDO, resultado.status)
    }

    @Test
    fun `quando o total e desconhecido, entao nunca passa para LIDO automaticamente`() {
        val resultado = livro(totalPaginas = null, status = StatusLeitura.LENDO).comProgresso(10_000)

        assertEquals(StatusLeitura.LENDO, resultado.status)
    }

    @Test
    fun `quando atualiza o progresso, entao os outros campos nao mudam`() {
        val original = livro(status = StatusLeitura.LENDO, paginaAtual = 10)

        val resultado = original.comProgresso(20)

        assertEquals(original.copy(paginaAtual = 20), resultado)
    }
}
