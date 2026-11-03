package com.minhaestante.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/**
 * Lê e grava as preferências do usuário com o DataStore (Preferences).
 *
 * Por que DataStore e não Room? Preferências são poucos pares chave → valor, sem consultas
 * nem relações. O DataStore é feito para isso: assíncrono (coroutines/Flow), seguro entre
 * threads e substituto moderno do `SharedPreferences`.
 *
 * Nesta etapa só existe `ultima_busca`; tema e lembrete entram na etapa 7.
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

    val ultimaBusca: Flow<String> = preferencias.map { it[Chaves.ULTIMA_BUSCA].orEmpty() }

    /** `edit` grava de forma atômica: ou tudo é salvo, ou nada. */
    suspend fun salvarUltimaBusca(termo: String) {
        dataStore.edit { it[Chaves.ULTIMA_BUSCA] = termo }
    }

    private object Chaves {
        val ULTIMA_BUSCA = stringPreferencesKey("ultima_busca")
    }
}
