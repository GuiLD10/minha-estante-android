package com.minhaestante.core.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Preferências do usuário. Nesta etapa só existe a última busca;
 * tema e lembrete diário entram na etapa 7.
 */
interface PreferenciasRepository {
    val ultimaBusca: Flow<String>
    suspend fun salvarUltimaBusca(termo: String)
}
