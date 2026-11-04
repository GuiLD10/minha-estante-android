package com.minhaestante.core.data.repository

import com.minhaestante.core.datastore.PreferenciasDataSource
import com.minhaestante.core.model.TemaApp
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Implementação de [PreferenciasRepository] apoiada no DataStore. */
internal class DataStorePreferenciasRepository @Inject constructor(
    private val dataSource: PreferenciasDataSource,
) : PreferenciasRepository {

    override val tema: Flow<TemaApp> = dataSource.tema

    override val lembreteAtivo: Flow<Boolean> = dataSource.lembreteAtivo

    override val ultimaBusca: Flow<String> = dataSource.ultimaBusca

    override suspend fun definirTema(tema: TemaApp) {
        dataSource.definirTema(tema)
    }

    override suspend fun definirLembreteAtivo(ativo: Boolean) {
        dataSource.definirLembreteAtivo(ativo)
    }

    override suspend fun salvarUltimaBusca(termo: String) {
        dataSource.salvarUltimaBusca(termo)
    }
}
