package com.minhaestante.core.data.repository

import com.minhaestante.core.model.TemaApp
import kotlinx.coroutines.flow.Flow

/** Preferências do usuário: tema, lembrete diário e última busca. */
interface PreferenciasRepository {
    val tema: Flow<TemaApp>
    val lembreteAtivo: Flow<Boolean>
    val ultimaBusca: Flow<String>
    suspend fun definirTema(tema: TemaApp)
    suspend fun definirLembreteAtivo(ativo: Boolean)
    suspend fun salvarUltimaBusca(termo: String)
}
