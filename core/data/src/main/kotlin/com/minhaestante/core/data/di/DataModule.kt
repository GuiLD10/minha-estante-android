package com.minhaestante.core.data.di

import com.minhaestante.core.data.repository.EstanteRepository
import com.minhaestante.core.data.repository.OfflineFirstEstanteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Diz ao Hilt **qual implementação** usar quando alguém pede uma interface de Repository.
 *
 * `@Binds` é a forma mais enxuta para isso: a função é abstrata (sem corpo), porque o Hilt
 * já sabe construir [OfflineFirstEstanteRepository] pelo seu construtor `@Inject`.
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
}
