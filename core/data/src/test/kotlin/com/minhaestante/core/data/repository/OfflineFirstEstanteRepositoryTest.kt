package com.minhaestante.core.data.repository

import com.minhaestante.core.database.dao.LivroDao
import com.minhaestante.core.database.entity.LivroEntity
import com.minhaestante.core.model.ResultadoBusca
import com.minhaestante.core.model.StatusLeitura
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineFirstEstanteRepositoryTest {

    // relaxUnitFun: funções que retornam Unit (atualizar, remover) não precisam de `coEvery`.
    private val dao = mockk<LivroDao>(relaxUnitFun = true)
    private val repository = OfflineFirstEstanteRepository(dao)

    private fun entity(
        id: String = "/works/1",
        status: StatusLeitura = StatusLeitura.QUERO_LER,
        paginaAtual: Int = 0,
        totalPaginas: Int? = 200,
    ) = LivroEntity(
        id = id,
        titulo = "Livro $id",
        autores = listOf("Autor"),
        anoPublicacao = 2000,
        capaId = null,
        totalPaginas = totalPaginas,
        status = status,
        paginaAtual = paginaAtual,
        adicionadoEm = 1L,
    )

    private val resultado = ResultadoBusca(
        id = "/works/1",
        titulo = "Livro novo",
        autores = listOf("Autor"),
        anoPublicacao = 2020,
        capaId = 10L,
        totalPaginas = 300,
        naEstante = false,
    )

    @Test
    fun `quando observa sem filtro, entao usa observarTodos e mapeia para dominio`() = runTest {
        every { dao.observarTodos() } returns flowOf(listOf(entity("/works/1"), entity("/works/2")))

        val livros = repository.observarEstante(filtro = null).first()

        assertEquals(listOf("/works/1", "/works/2"), livros.map { it.id })
        verify(exactly = 0) { dao.observarPorStatus(any()) }
    }

    @Test
    fun `quando observa com filtro, entao usa observarPorStatus com o status escolhido`() = runTest {
        every { dao.observarPorStatus(StatusLeitura.LENDO) } returns
            flowOf(listOf(entity(status = StatusLeitura.LENDO)))

        val livros = repository.observarEstante(filtro = StatusLeitura.LENDO).first()

        assertEquals(StatusLeitura.LENDO, livros.single().status)
        verify(exactly = 0) { dao.observarTodos() }
    }

    @Test
    fun `quando observa um livro existente, entao retorna o livro mapeado`() = runTest {
        every { dao.observarPorId("/works/1") } returns flowOf(entity("/works/1"))

        assertEquals("/works/1", repository.observarLivro("/works/1").first()?.id)
    }

    @Test
    fun `quando observa um livro inexistente, entao retorna nulo`() = runTest {
        every { dao.observarPorId("/works/x") } returns flowOf(null)

        assertNull(repository.observarLivro("/works/x").first())
    }

    @Test
    fun `quando observa os ids, entao retorna um conjunto`() = runTest {
        every { dao.observarIds() } returns flowOf(listOf("/works/1", "/works/2"))

        assertEquals(setOf("/works/1", "/works/2"), repository.observarIdsNaEstante().first())
    }

    @Test
    fun `quando adiciona um livro novo, entao retorna true e grava em QUERO_LER na pagina zero`() = runTest {
        val gravado = slot<LivroEntity>()
        coEvery { dao.inserir(capture(gravado)) } returns 1L
        val antes = System.currentTimeMillis()

        val inseriu = repository.adicionar(resultado)

        assertTrue(inseriu)
        assertEquals(resultado.id, gravado.captured.id)
        assertEquals(resultado.titulo, gravado.captured.titulo)
        assertEquals(StatusLeitura.QUERO_LER, gravado.captured.status)
        assertEquals(0, gravado.captured.paginaAtual)
        assertTrue(gravado.captured.adicionadoEm in antes..System.currentTimeMillis())
    }

    @Test
    fun `quando adiciona um livro duplicado, entao retorna false`() = runTest {
        coEvery { dao.inserir(any()) } returns -1L

        assertFalse(repository.adicionar(resultado))
    }

    @Test
    fun `quando atualiza o status para LIDO, entao grava com a pagina igual ao total`() = runTest {
        every { dao.observarPorId("/works/1") } returns
            flowOf(entity(status = StatusLeitura.LENDO, paginaAtual = 50, totalPaginas = 200))

        repository.atualizarStatus("/works/1", StatusLeitura.LIDO)

        coVerify { dao.atualizar(entity(status = StatusLeitura.LIDO, paginaAtual = 200, totalPaginas = 200)) }
    }

    @Test
    fun `quando atualiza o status para QUERO_LER, entao grava com a pagina zero`() = runTest {
        every { dao.observarPorId("/works/1") } returns
            flowOf(entity(status = StatusLeitura.LENDO, paginaAtual = 50))

        repository.atualizarStatus("/works/1", StatusLeitura.QUERO_LER)

        coVerify { dao.atualizar(entity(status = StatusLeitura.QUERO_LER, paginaAtual = 0)) }
    }

    @Test
    fun `quando atualiza o progresso de um livro QUERO_LER, entao grava como LENDO`() = runTest {
        every { dao.observarPorId("/works/1") } returns flowOf(entity(status = StatusLeitura.QUERO_LER))

        repository.atualizarProgresso("/works/1", pagina = 30)

        coVerify { dao.atualizar(entity(status = StatusLeitura.LENDO, paginaAtual = 30)) }
    }

    @Test
    fun `quando o progresso chega ao total, entao grava como LIDO`() = runTest {
        every { dao.observarPorId("/works/1") } returns
            flowOf(entity(status = StatusLeitura.LENDO, paginaAtual = 10, totalPaginas = 200))

        repository.atualizarProgresso("/works/1", pagina = 250)

        coVerify { dao.atualizar(entity(status = StatusLeitura.LIDO, paginaAtual = 200, totalPaginas = 200)) }
    }

    @Test
    fun `quando atualiza um livro que nao existe, entao nao grava nada`() = runTest {
        every { dao.observarPorId(any()) } returns flowOf(null)

        repository.atualizarStatus("/works/x", StatusLeitura.LIDO)
        repository.atualizarProgresso("/works/x", pagina = 10)

        coVerify(exactly = 0) { dao.atualizar(any()) }
    }

    @Test
    fun `quando remove, entao delega ao dao`() = runTest {
        repository.remover("/works/1")

        coVerify { dao.remover("/works/1") }
    }

    @Test
    fun `quando busca livros sendo lidos, entao retorna os livros LENDO mapeados`() = runTest {
        coEvery { dao.buscarLendo() } returns listOf(entity(status = StatusLeitura.LENDO, paginaAtual = 5))

        val livros = repository.livrosSendoLidos()

        assertEquals(StatusLeitura.LENDO, livros.single().status)
        assertEquals(5, livros.single().paginaAtual)
    }
}
