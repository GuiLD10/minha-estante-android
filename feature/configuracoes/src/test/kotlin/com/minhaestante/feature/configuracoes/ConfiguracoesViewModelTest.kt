package com.minhaestante.feature.configuracoes

import app.cash.turbine.test
import com.minhaestante.core.data.repository.PreferenciasRepository
import com.minhaestante.core.lembrete.AgendadorLembrete
import com.minhaestante.core.model.TemaApp
import com.minhaestante.core.testing.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ConfiguracoesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /*
     * MockK: em vez de um fake completo, criamos um "dublê" do Repository e dizemos o que cada
     * propriedade devolve (`every { ... } returns ...`). `relaxUnitFun = true` faz as funções
     * que retornam Unit (definirTema, definirLembreteAtivo) não fazerem nada, e depois
     * conferimos com `coVerify` que foram chamadas com os argumentos certos.
     */
    private val tema = MutableStateFlow(TemaApp.SISTEMA)
    private val lembreteAtivo = MutableStateFlow(false)
    private val preferencias = mockk<PreferenciasRepository>(relaxUnitFun = true) {
        every { tema } returns this@ConfiguracoesViewModelTest.tema
        every { lembreteAtivo } returns this@ConfiguracoesViewModelTest.lembreteAtivo
    }
    /** `relaxed = true`: todas as funções do mock simplesmente não fazem nada. */
    private val agendador = mockk<AgendadorLembrete>(relaxed = true)
    private val viewModel = ConfiguracoesViewModel(preferencias, agendador)

    @Test
    fun `quando as preferencias ainda nao chegaram, entao o estado e Carregando`() = runTest {
        // SharedFlow sem valor: simula um DataStore que ainda não respondeu.
        val semResposta = mockk<PreferenciasRepository> {
            every { tema } returns MutableSharedFlow()
            every { lembreteAtivo } returns MutableSharedFlow()
        }

        ConfiguracoesViewModel(semResposta, agendador).uiState.test {
            assertEquals(ConfiguracoesUiState.Carregando, awaitItem())
        }
    }

    @Test
    fun `quando as preferencias chegam, entao o estado e Pronto com os valores salvos`() = runTest {
        tema.value = TemaApp.ESCURO
        lembreteAtivo.value = true

        viewModel.uiState.test {
            // O `initialValue` do stateIn sempre vem primeiro; depois chegam os valores reais.
            assertEquals(ConfiguracoesUiState.Carregando, awaitItem())
            assertEquals(ConfiguracoesUiState.Pronto(TemaApp.ESCURO, lembreteAtivo = true), awaitItem())
        }
    }

    @Test
    fun `quando uma preferencia muda, entao o estado e atualizado`() = runTest {
        viewModel.uiState.test {
            assertEquals(ConfiguracoesUiState.Carregando, awaitItem())
            assertEquals(ConfiguracoesUiState.Pronto(TemaApp.SISTEMA, lembreteAtivo = false), awaitItem())

            tema.value = TemaApp.CLARO

            assertEquals(ConfiguracoesUiState.Pronto(TemaApp.CLARO, lembreteAtivo = false), awaitItem())
        }
    }

    @Test
    fun `quando define o tema, entao grava no repositorio`() = runTest {
        viewModel.definirTema(TemaApp.ESCURO)

        coVerify { preferencias.definirTema(TemaApp.ESCURO) }
    }

    @Test
    fun `quando ativa o lembrete, entao grava no repositorio e agenda o trabalho`() = runTest {
        viewModel.definirLembreteAtivo(true)

        coVerifyOrder {
            preferencias.definirLembreteAtivo(true)
            agendador.agendar()
        }
        verify(exactly = 0) { agendador.cancelar() }
    }

    @Test
    fun `quando desativa o lembrete, entao grava no repositorio e cancela o trabalho`() = runTest {
        viewModel.definirLembreteAtivo(false)

        coVerifyOrder {
            preferencias.definirLembreteAtivo(false)
            agendador.cancelar()
        }
        verify(exactly = 0) { agendador.agendar() }
    }

    @Test
    fun `quando dispara o lembrete agora, entao pede a execucao imediata ao agendador`() {
        viewModel.dispararLembreteAgora()

        verify { agendador.dispararAgora() }
    }

    @Test
    fun `quando define o tema, entao nao mexe no agendamento`() = runTest {
        viewModel.definirTema(TemaApp.CLARO)

        verify(exactly = 0) { agendador.agendar() }
        verify(exactly = 0) { agendador.cancelar() }
    }
}
