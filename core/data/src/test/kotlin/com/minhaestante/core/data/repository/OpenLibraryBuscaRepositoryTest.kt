package com.minhaestante.core.data.repository

import com.minhaestante.core.model.ResultadoBusca
import com.minhaestante.core.network.BuscaRemoteDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class OpenLibraryBuscaRepositoryTest {

    private val remoteDataSource = mockk<BuscaRemoteDataSource>()
    private val repository = OpenLibraryBuscaRepository(remoteDataSource)

    private val resultado = ResultadoBusca(
        id = "/works/1",
        titulo = "Livro",
        autores = listOf("Autor"),
        anoPublicacao = 2000,
        capaId = null,
        totalPaginas = 100,
        naEstante = false,
    )

    @Test
    fun `quando busca, entao retorna os resultados da rede para o termo`() = runTest {
        coEvery { remoteDataSource.buscar("tolkien") } returns listOf(resultado)

        assertEquals(listOf(resultado), repository.buscar("tolkien"))
    }

    @Test(expected = IOException::class)
    fun `quando a rede falha, entao a excecao sobe para quem chamou`() = runTest {
        coEvery { remoteDataSource.buscar(any()) } throws IOException("sem rede")

        repository.buscar("tolkien")
    }
}
