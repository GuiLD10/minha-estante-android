package com.minhaestante.core.data.repository

import com.minhaestante.core.datastore.PreferenciasDataSource
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DataStorePreferenciasRepositoryTest {

    private val dataSource = mockk<PreferenciasDataSource>(relaxUnitFun = true) {
        every { ultimaBusca } returns flowOf("tolkien")
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
}
