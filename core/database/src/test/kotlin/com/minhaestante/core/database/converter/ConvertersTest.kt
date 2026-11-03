package com.minhaestante.core.database.converter

import com.minhaestante.core.model.StatusLeitura
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `quando converte uma lista de autores e volta, entao obtem a mesma lista`() {
        val autores = listOf("Neil Gaiman", "Terry Pratchett")

        val texto = converters.autoresParaTexto(autores)

        assertEquals(autores, converters.textoParaAutores(texto))
    }

    @Test
    fun `quando o nome do autor tem virgula ou aspas, entao a conversao preserva o nome`() {
        val autores = listOf("Tolkien, J. R. R.", "Autor \"com\" aspas")

        assertEquals(autores, converters.textoParaAutores(converters.autoresParaTexto(autores)))
    }

    @Test
    fun `quando a lista de autores e vazia, entao volta como lista vazia`() {
        val texto = converters.autoresParaTexto(emptyList())

        assertEquals(emptyList<String>(), converters.textoParaAutores(texto))
    }

    @Test
    fun `quando converte um status, entao salva o nome do enum`() {
        assertEquals("LENDO", converters.statusParaTexto(StatusLeitura.LENDO))
    }

    @Test
    fun `quando converte todos os status e volta, entao obtem os mesmos valores`() {
        StatusLeitura.entries.forEach { status ->
            assertEquals(status, converters.textoParaStatus(converters.statusParaTexto(status)))
        }
    }
}
