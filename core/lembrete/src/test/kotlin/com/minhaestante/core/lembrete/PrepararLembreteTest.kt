package com.minhaestante.core.lembrete

import com.minhaestante.core.model.StatusLeitura
import com.minhaestante.core.testing.FakeEstanteRepository
import com.minhaestante.core.testing.FakePreferenciasRepository
import com.minhaestante.core.testing.livroTeste
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Testa a decisão do [LembreteWorker], extraída para uma função sem dependência de Android. */
class PrepararLembreteTest {

    private val lendo = livroTeste("/works/1", StatusLeitura.LENDO)
    private val outroLendo = livroTeste("/works/2", StatusLeitura.LENDO)
    private val queroLer = livroTeste("/works/3", StatusLeitura.QUERO_LER)

    private val lembreteAtivo = FakePreferenciasRepository(lembreteAtivoInicial = true)

    @Test
    fun `quando o lembrete esta desativado, entao nao notifica`() = runTest {
        val estante = FakeEstanteRepository(listOf(lendo))
        val desativado = FakePreferenciasRepository(lembreteAtivoInicial = false)

        assertNull(prepararLembrete(estante, desativado))
    }

    @Test
    fun `quando nao ha livros sendo lidos, entao nao notifica`() = runTest {
        val estante = FakeEstanteRepository(listOf(queroLer))

        assertNull(prepararLembrete(estante, lembreteAtivo))
    }

    @Test
    fun `quando a estante esta vazia, entao nao notifica`() = runTest {
        assertNull(prepararLembrete(FakeEstanteRepository(emptyList()), lembreteAtivo))
    }

    @Test
    fun `quando ha um livro sendo lido, entao o lembrete cita o titulo`() = runTest {
        val estante = FakeEstanteRepository(listOf(lendo, queroLer))

        assertEquals(ConteudoLembrete.UmLivro(lendo.titulo), prepararLembrete(estante, lembreteAtivo))
    }

    @Test
    fun `quando ha varios livros sendo lidos, entao o lembrete informa a quantidade`() = runTest {
        val estante = FakeEstanteRepository(listOf(lendo, outroLendo, queroLer))

        assertEquals(ConteudoLembrete.VariosLivros(2), prepararLembrete(estante, lembreteAtivo))
    }
}
