package com.minhaestante.core.data.repository

import com.minhaestante.core.datastore.PreferenciasDataSource
import com.minhaestante.core.model.TemaApp
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DataStorePreferenciasRepositoryTest {

    private val dataSource = mockk<PreferenciasDataSource>(relaxUnitFun = true) {
        every { ultimaBusca } returns flowOf("tolkien")
        every { tema } returns flowOf(TemaApp.ESCURO)
        every { lembreteAtivo } returns flowOf(true)
    }
    private val repository = DataStorePreferenciasRepository(dataSource)

    @Test
    fun `quando observa a ultima busca, entao repassa o valor do DataStore`() = runTest {
        assertEquals("tolkien", repository.ultimaBusca.first())
    }

    @Test
    fun `quando salva a ultima busca, entao grava no DataStore`() = runTest {
        repository.salvarUltimaBusca("machado")

        coVerify { dataSource.salvarUltimaBusca("machado") }
    }

    @Test
    fun `quando observa o tema, entao repassa o valor do DataStore`() = runTest {
        assertEquals(TemaApp.ESCURO, repository.tema.first())
    }

    @Test
    fun `quando define o tema, entao grava no DataStore`() = runTest {
        repository.definirTema(TemaApp.CLARO)

        coVerify { dataSource.definirTema(TemaApp.CLARO) }
    }

    @Test
    fun `quando observa o lembrete, entao repassa o valor do DataStore`() = runTest {
        assertTrue(repository.lembreteAtivo.first())
    }

    @Test
    fun `quando define o lembrete, entao grava no DataStore`() = runTest {
        repository.definirLembreteAtivo(false)

        coVerify { dataSource.definirLembreteAtivo(false) }
    }
}
