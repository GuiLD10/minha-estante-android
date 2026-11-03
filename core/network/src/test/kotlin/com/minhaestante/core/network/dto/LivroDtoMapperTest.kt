package com.minhaestante.core.network.dto

import com.minhaestante.core.network.openLibraryJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LivroDtoMapperTest {

    @Test
    fun `quando o dto tem todos os campos, entao copia tudo para o resultado`() {
        val dto = LivroDto(
            key = "/works/OL45804W",
            title = "Fantastic Mr Fox",
            autores = listOf("Roald Dahl"),
            ano = 1970,
            capaId = 6498519L,
            paginas = 96,
        )

        val resultado = dto.toResultadoBusca()

        assertEquals("/works/OL45804W", resultado.id)
        assertEquals("Fantastic Mr Fox", resultado.titulo)
        assertEquals(listOf("Roald Dahl"), resultado.autores)
        assertEquals(1970, resultado.anoPublicacao)
        assertEquals(6498519L, resultado.capaId)
        assertEquals(96, resultado.totalPaginas)
    }

    @Test
    fun `quando mapeia, entao o resultado ainda nao esta na estante`() {
        assertFalse(LivroDto(key = "/works/1", title = "Livro").toResultadoBusca().naEstante)
    }

    @Test
    fun `quando o json nao traz os campos opcionais, entao usa nulos e lista vazia`() {
        val json = """{"key": "/works/1", "title": "Livro sem detalhes"}"""

        val resultado = openLibraryJson.decodeFromString<LivroDto>(json).toResultadoBusca()

        assertTrue(resultado.autores.isEmpty())
        assertNull(resultado.anoPublicacao)
        assertNull(resultado.capaId)
        assertNull(resultado.totalPaginas)
    }

    @Test
    fun `quando o json traz campos desconhecidos, entao eles sao ignorados`() {
        val json = """
            {
              "numFound": 1,
              "docs": [
                {"key": "/works/1", "title": "Livro", "author_name": ["Autora"], "ratings_average": 4.5}
              ]
            }
        """.trimIndent()

        val resposta = openLibraryJson.decodeFromString<BuscaResponse>(json)

        assertEquals(listOf("Autora"), resposta.docs.single().toResultadoBusca().autores)
    }
}
