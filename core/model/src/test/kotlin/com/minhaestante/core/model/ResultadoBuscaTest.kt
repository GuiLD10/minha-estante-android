package com.minhaestante.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResultadoBuscaTest {

    private val resultado = ResultadoBusca(
        id = "/works/OL45804W",
        titulo = "Fantastic Mr Fox",
        autores = listOf("Roald Dahl"),
        anoPublicacao = 1970,
        capaId = 6498519L,
        totalPaginas = 96,
        naEstante = false,
    )

    @Test
    fun `quando converte para livro, entao copia os dados do resultado`() {
        val livro = resultado.paraLivro(agora = 123L)

        assertEquals(resultado.id, livro.id)
        assertEquals(resultado.titulo, livro.titulo)
        assertEquals(resultado.autores, livro.autores)
        assertEquals(resultado.anoPublicacao, livro.anoPublicacao)
        assertEquals(resultado.capaId, livro.capaId)
        assertEquals(resultado.totalPaginas, livro.totalPaginas)
    }

    @Test
    fun `quando converte para livro, entao comeca em QUERO_LER na pagina zero`() {
        val livro = resultado.paraLivro(agora = 123L)

        assertEquals(StatusLeitura.QUERO_LER, livro.status)
        assertEquals(0, livro.paginaAtual)
    }

    @Test
    fun `quando converte para livro, entao usa o horario informado em adicionadoEm`() {
        assertEquals(1_700_000_000_000L, resultado.paraLivro(agora = 1_700_000_000_000L).adicionadoEm)
    }

    @Test
    fun `quando campos opcionais sao nulos, entao o livro tambem os mantem nulos`() {
        val livro = resultado.copy(anoPublicacao = null, capaId = null, totalPaginas = null)
            .paraLivro(agora = 123L)

        assertNull(livro.anoPublicacao)
        assertNull(livro.capaId)
        assertNull(livro.totalPaginas)
    }

    @Test
    fun `quando ha varios autores, entao os junta com virgula`() {
        val comDois = resultado.copy(autores = listOf("Neil Gaiman", "Terry Pratchett"))

        assertEquals("Neil Gaiman, Terry Pratchett", comDois.autoresFormatados())
    }

    @Test
    fun `quando nao ha autores, entao retorna autor desconhecido`() {
        assertEquals(AUTOR_DESCONHECIDO, resultado.copy(autores = emptyList()).autoresFormatados())
    }
}
