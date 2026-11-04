package com.minhaestante.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.minhaestante.core.model.TemaApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/**
 * Lê e grava as preferências do usuário com o DataStore (Preferences).
 *
 * Por que DataStore e não Room? Preferências são poucos pares chave → valor, sem consultas
 * nem relações. O DataStore é feito para isso: assíncrono (coroutines/Flow), seguro entre
 * threads e substituto moderno do `SharedPreferences`.
 */
class PreferenciasDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    /**
     * `dataStore.data` é um Flow que emite de novo a cada gravação.
     * Se a leitura do arquivo falhar (`IOException`), seguimos com preferências vazias
     * (ou seja, os valores padrão) em vez de derrubar o app.
     */
    private val preferencias: Flow<Preferences> = dataStore.data.catch { erro ->
        if (erro is IOException) emit(emptyPreferences()) else throw erro
    }

    /*
     * Todas as chaves vivem no mesmo arquivo, então `dataStore.data` emite quando *qualquer*
     * uma muda. `distinctUntilChanged()` evita que, por exemplo, salvar a última busca faça
     * o tema "emitir de novo" o mesmo valor (o que recalcularia telas à toa).
     */

    val ultimaBusca: Flow<String> = preferencias
        .map { it[Chaves.ULTIMA_BUSCA].orEmpty() }
        .distinctUntilChanged()

    /**
     * O tema é gravado como texto (`TemaApp.name`). Se o valor salvo for desconhecido
     * (ex.: um tema removido numa versão futura), voltamos ao padrão em vez de lançar exceção,
     * como faria `TemaApp.valueOf`.
     */
    val tema: Flow<TemaApp> = preferencias
        .map { prefs -> prefs[Chaves.TEMA].paraTemaApp() }
        .distinctUntilChanged()

    val lembreteAtivo: Flow<Boolean> = preferencias
        .map { it[Chaves.LEMBRETE_ATIVO] ?: LEMBRETE_ATIVO_PADRAO }
        .distinctUntilChanged()

    /** `edit` grava de forma atômica: ou tudo é salvo, ou nada. */
    suspend fun salvarUltimaBusca(termo: String) {
        dataStore.edit { it[Chaves.ULTIMA_BUSCA] = termo }
    }

    suspend fun definirTema(tema: TemaApp) {
        dataStore.edit { it[Chaves.TEMA] = tema.name }
    }

    suspend fun definirLembreteAtivo(ativo: Boolean) {
        dataStore.edit { it[Chaves.LEMBRETE_ATIVO] = ativo }
    }

    private fun String?.paraTemaApp(): TemaApp =
        TemaApp.entries.find { it.name == this } ?: TEMA_PADRAO

    private object Chaves {
        val ULTIMA_BUSCA = stringPreferencesKey("ultima_busca")
        val TEMA = stringPreferencesKey("tema")
        val LEMBRETE_ATIVO = booleanPreferencesKey("lembrete_ativo")
    }

    private companion object {
        val TEMA_PADRAO = TemaApp.SISTEMA
        const val LEMBRETE_ATIVO_PADRAO = false
    }
}
