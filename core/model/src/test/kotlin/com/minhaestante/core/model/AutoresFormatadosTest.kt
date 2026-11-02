package com.minhaestante.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AutoresFormatadosTest {

    @Test
    fun `quando ha um autor, entao retorna apenas o nome dele`() {
        assertEquals("Machado de Assis", livro(autores = listOf("Machado de Assis")).autoresFormatados())
    }

    @Test
    fun `quando ha varios autores, entao junta com virgula e espaco`() {
        val autores = listOf("Neil Gaiman", "Terry Pratchett")

        assertEquals("Neil Gaiman, Terry Pratchett", livro(autores = autores).autoresFormatados())
    }

    @Test
    fun `quando a lista de autores e vazia, entao retorna Autor desconhecido`() {
        assertEquals(AUTOR_DESCONHECIDO, livro(autores = emptyList()).autoresFormatados())
    }

    @Test
    fun `quando ha nomes em branco, entao eles sao ignorados`() {
        val autores = listOf("", "Clarice Lispector", "   ")

        assertEquals("Clarice Lispector", livro(autores = autores).autoresFormatados())
    }

    @Test
    fun `quando todos os nomes estao em branco, entao retorna Autor desconhecido`() {
        assertEquals(AUTOR_DESCONHECIDO, livro(autores = listOf(" ", "")).autoresFormatados())
    }
}
