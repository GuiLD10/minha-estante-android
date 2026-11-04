package com.minhaestante.core.datastore

import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.minhaestante.core.model.TemaApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Testa com um DataStore **de verdade**, gravando num arquivo temporário.
 * Não precisa de emulador: a criação via `PreferenceDataStoreFactory` não usa `Context`.
 */
class PreferenciasDataSourceTest {

    @get:Rule
    val pastaTemporaria = TemporaryFolder()

    /** `backgroundScope` é cancelado no fim do teste, encerrando o DataStore junto. */
    private fun TestScope.criarDataSource() = PreferenciasDataSource(criarDataStore())

    // Storage com Okio: substitui o arquivo de forma atômica em qualquer sistema. O storage
    // padrão (baseado em `File.renameTo`) não consegue sobrescrever arquivos no Windows,
    // o que faria a segunda gravação falhar ao rodar os testes no PC. No Android tanto faz.
    private fun TestScope.criarDataStore() = PreferenceDataStoreFactory.create(
        storage = OkioStorage(FileSystem.SYSTEM, PreferencesSerializer) {
            File(pastaTemporaria.root, "teste.preferences_pb").toOkioPath()
        },
        scope = backgroundScope,
    )

    @Test
    fun `quando nada foi salvo, entao a ultima busca e vazia`() = runTest {
        assertEquals("", criarDataSource().ultimaBusca.first())
    }

    @Test
    fun `quando salva um termo, entao a ultima busca passa a ser esse termo`() = runTest {
        val dataSource = criarDataSource()

        dataSource.salvarUltimaBusca("tolkien")

        assertEquals("tolkien", dataSource.ultimaBusca.first())
    }

    @Test
    fun `quando salva duas vezes, entao vale o termo mais recente`() = runTest {
        val dataSource = criarDataSource()

        dataSource.salvarUltimaBusca("tolkien")
        dataSource.salvarUltimaBusca("machado")

        assertEquals("machado", dataSource.ultimaBusca.first())
    }

    @Test
    fun `quando nada foi salvo, entao o tema e SISTEMA`() = runTest {
        assertEquals(TemaApp.SISTEMA, criarDataSource().tema.first())
    }

    @Test
    fun `quando define o tema, entao o tema passa a ser o escolhido`() = runTest {
        val dataSource = criarDataSource()

        dataSource.definirTema(TemaApp.ESCURO)

        assertEquals(TemaApp.ESCURO, dataSource.tema.first())
    }

    @Test
    fun `quando o tema salvo e desconhecido, entao volta para SISTEMA`() = runTest {
        val dataStore = criarDataStore()
        dataStore.edit { it[stringPreferencesKey("tema")] = "ROXO" }

        assertEquals(TemaApp.SISTEMA, PreferenciasDataSource(dataStore).tema.first())
    }

    @Test
    fun `quando nada foi salvo, entao o lembrete esta desativado`() = runTest {
        assertFalse(criarDataSource().lembreteAtivo.first())
    }

    @Test
    fun `quando ativa o lembrete, entao o lembrete passa a estar ativo`() = runTest {
        val dataSource = criarDataSource()

        dataSource.definirLembreteAtivo(true)

        assertTrue(dataSource.lembreteAtivo.first())
    }

    @Test
    fun `quando ativa e depois desativa o lembrete, entao o lembrete fica desativado`() = runTest {
        val dataSource = criarDataSource()

        dataSource.definirLembreteAtivo(true)
        dataSource.definirLembreteAtivo(false)

        assertFalse(dataSource.lembreteAtivo.first())
    }
}
