package com.minhaestante.core.data.di

import com.minhaestante.core.data.repository.BuscaRepository
import com.minhaestante.core.data.repository.DataStorePreferenciasRepository
import com.minhaestante.core.data.repository.EstanteRepository
import com.minhaestante.core.data.repository.OfflineFirstEstanteRepository
import com.minhaestante.core.data.repository.OpenLibraryBuscaRepository
import com.minhaestante.core.data.repository.PreferenciasRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Diz ao Hilt **qual implementação** usar quando alguém pede uma interface de Repository.
 *
 * `@Binds` é a forma mais enxuta para isso: a função é abstrata (sem corpo), porque o Hilt
 * já sabe construir cada implementação pelo seu construtor `@Inject`.
 * Só precisamos ligar "interface → implementação".
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindEstanteRepository(
        implementacao: OfflineFirstEstanteRepository,
    ): EstanteRepository

    @Binds
    @Singleton
    abstract fun bindBuscaRepository(
        implementacao: OpenLibraryBuscaRepository,
    ): BuscaRepository

    @Binds
    @Singleton
    abstract fun bindPreferenciasRepository(
        implementacao: DataStorePreferenciasRepository,
    ): PreferenciasRepository
}
