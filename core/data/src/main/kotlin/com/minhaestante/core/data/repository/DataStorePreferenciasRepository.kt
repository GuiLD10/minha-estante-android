package com.minhaestante.core.data.repository

import com.minhaestante.core.datastore.PreferenciasDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Implementação de [PreferenciasRepository] apoiada no DataStore. */
internal class DataStorePreferenciasRepository @Inject constructor(
    private val dataSource: PreferenciasDataSource,
) : PreferenciasRepository {

    override val ultimaBusca: Flow<String> = dataSource.ultimaBusca

    override suspend fun salvarUltimaBusca(termo: String) {
        dataSource.salvarUltimaBusca(termo)
    }
}
