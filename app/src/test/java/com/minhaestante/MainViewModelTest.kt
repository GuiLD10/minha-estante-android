package com.minhaestante

import app.cash.turbine.test
import com.minhaestante.core.model.TemaApp
import com.minhaestante.core.testing.FakePreferenciasRepository
import com.minhaestante.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `quando o tema salvo e ESCURO, entao expoe ESCURO`() = runTest {
        val viewModel = MainViewModel(FakePreferenciasRepository(temaInicial = TemaApp.ESCURO))

        viewModel.tema.test {
            // Se o `initialValue` (SISTEMA) chega antes ou não, depende do dispatcher;
            // o que importa é que o valor mais recente seja o salvo.
            assertEquals(TemaApp.ESCURO, expectMostRecentItem())
        }
    }

    @Test
    fun `quando o tema muda nas preferencias, entao o novo tema e emitido`() = runTest {
        val preferencias = FakePreferenciasRepository()
        val viewModel = MainViewModel(preferencias)

        viewModel.tema.test {
            assertEquals(TemaApp.SISTEMA, awaitItem())

            preferencias.definirTema(TemaApp.CLARO)

            assertEquals(TemaApp.CLARO, awaitItem())
        }
    }

    @Test
    fun `quando o tema e SISTEMA, entao segue o modo do sistema`() {
        assertTrue(TemaApp.SISTEMA.usaTemaEscuro(sistemaEscuro = true))
        assertFalse(TemaApp.SISTEMA.usaTemaEscuro(sistemaEscuro = false))
    }

    @Test
    fun `quando o tema e CLARO, entao nunca usa tema escuro`() {
        assertFalse(TemaApp.CLARO.usaTemaEscuro(sistemaEscuro = true))
    }

    @Test
    fun `quando o tema e ESCURO, entao sempre usa tema escuro`() {
        assertTrue(TemaApp.ESCURO.usaTemaEscuro(sistemaEscuro = false))
    }
}
