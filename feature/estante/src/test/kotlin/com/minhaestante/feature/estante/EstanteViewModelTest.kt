package com.minhaestante.feature.estante

import app.cash.turbine.test
import com.minhaestante.core.model.StatusLeitura
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EstanteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeEstanteRepository
    private lateinit var viewModel: EstanteViewModel

    private val queroLer = livroTeste("/works/1", StatusLeitura.QUERO_LER)
    private val lendo = livroTeste("/works/2", StatusLeitura.LENDO)
    private val lido = livroTeste("/works/3", StatusLeitura.LIDO)

    @Before
    fun setUp() {
        repository = FakeEstanteRepository()
        viewModel = EstanteViewModel(repository)
    }

    @Test
    fun `quando o repositorio ainda nao emitiu, entao o estado e Loading`() = runTest {
        viewModel.uiState.test {
            assertEquals(EstanteUiState.Loading, awaitItem())
        }
    }

    @Test
    fun `quando ha livros, entao o estado e Success com todos e sem filtro`() = runTest {
        viewModel.uiState.test {
            assertEquals(EstanteUiState.Loading, awaitItem())

            repository.emitir(listOf(queroLer, lendo, lido))

            assertEquals(
                EstanteUiState.Success(listOf(queroLer, lendo, lido), filtro = null),
                awaitItem(),
            )
        }
    }

    @Test
    fun `quando a estante esta vazia, entao o estado e Vazia sem filtro`() = runTest {
        viewModel.uiState.test {
            assertEquals(EstanteUiState.Loading, awaitItem())

            repository.emitir(emptyList())

            assertEquals(EstanteUiState.Vazia(filtro = null), awaitItem())
        }
    }

    @Test
    fun `quando seleciona um filtro, entao mostra apenas os livros daquele status`() = runTest {
        viewModel.uiState.test {
            assertEquals(EstanteUiState.Loading, awaitItem())
            repository.emitir(listOf(queroLer, lendo, lido))
            awaitItem() // estado inicial sem filtro

            viewModel.selecionarFiltro(StatusLeitura.LENDO)

            assertEquals(EstanteUiState.Success(listOf(lendo), StatusLeitura.LENDO), awaitItem())
        }
    }

    @Test
    fun `quando o filtro nao tem livros, entao o estado e Vazia com o filtro`() = runTest {
        viewModel.uiState.test {
            assertEquals(EstanteUiState.Loading, awaitItem())
            repository.emitir(listOf(queroLer))
            awaitItem() // estado inicial sem filtro

            viewModel.selecionarFiltro(StatusLeitura.LIDO)

            assertEquals(EstanteUiState.Vazia(StatusLeitura.LIDO), awaitItem())
        }
    }

    @Test
    fun `quando volta para Todos, entao mostra todos os livros de novo`() = runTest {
        viewModel.uiState.test {
            assertEquals(EstanteUiState.Loading, awaitItem())
            repository.emitir(listOf(queroLer, lendo))
            awaitItem() // estado inicial sem filtro
            viewModel.selecionarFiltro(StatusLeitura.LENDO)
            awaitItem()

            viewModel.selecionarFiltro(null)

            assertEquals(EstanteUiState.Success(listOf(queroLer, lendo), filtro = null), awaitItem())
        }
    }

    @Test
    fun `quando o banco muda, entao o estado e atualizado automaticamente`() = runTest {
        viewModel.uiState.test {
            assertEquals(EstanteUiState.Loading, awaitItem())
            repository.emitir(listOf(queroLer))
            awaitItem() // estado inicial sem filtro

            repository.atualizarStatus(queroLer.id, StatusLeitura.LENDO)

            val estado = awaitItem() as EstanteUiState.Success
            assertEquals(StatusLeitura.LENDO, estado.livros.single().status)
        }
    }
}
