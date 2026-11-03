package com.minhaestante.core.datastore

import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.PreferencesSerializer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.junit.Assert.assertEquals
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
    private fun TestScope.criarDataSource() = PreferenciasDataSource(
        // Storage com Okio: substitui o arquivo de forma atômica em qualquer sistema. O storage
        // padrão (baseado em `File.renameTo`) não consegue sobrescrever arquivos no Windows,
        // o que faria a segunda gravação falhar ao rodar os testes no PC. No Android tanto faz.
        PreferenceDataStoreFactory.create(
            storage = OkioStorage(FileSystem.SYSTEM, PreferencesSerializer) {
                File(pastaTemporaria.root, "teste.preferences_pb").toOkioPath()
            },
            scope = backgroundScope,
        ),
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
}
