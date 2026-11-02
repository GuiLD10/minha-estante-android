package com.minhaestante.core.data.mapper

import com.minhaestante.core.database.entity.LivroEntity
import com.minhaestante.core.model.Livro
import com.minhaestante.core.model.StatusLeitura
import org.junit.Assert.assertEquals
import org.junit.Test

class LivroMapperTest {

    private val entity = LivroEntity(
        id = "/works/OL45804W",
        titulo = "Fantastic Mr Fox",
        autores = listOf("Roald Dahl"),
        anoPublicacao = 1970,
        capaId = 6498519L,
        totalPaginas = 96,
        status = StatusLeitura.LENDO,
        paginaAtual = 40,
        adicionadoEm = 1_700_000_000_000L,
    )

    private val livro = Livro(
        id = "/works/OL45804W",
        titulo = "Fantastic Mr Fox",
        autores = listOf("Roald Dahl"),
        anoPublicacao = 1970,
        capaId = 6498519L,
        totalPaginas = 96,
        status = StatusLeitura.LENDO,
        paginaAtual = 40,
        adicionadoEm = 1_700_000_000_000L,
    )

    @Test
    fun `quando converte entity para dominio, entao todos os campos sao copiados`() {
        assertEquals(livro, entity.toDomain())
    }

    @Test
    fun `quando converte dominio para entity, entao todos os campos sao copiados`() {
        assertEquals(entity, livro.toEntity())
    }

    @Test
    fun `quando campos opcionais sao nulos, entao continuam nulos apos ida e volta`() {
        val semOpcionais = entity.copy(anoPublicacao = null, capaId = null, totalPaginas = null)

        assertEquals(semOpcionais, semOpcionais.toDomain().toEntity())
    }
}
