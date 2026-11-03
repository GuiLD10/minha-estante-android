package com.minhaestante.core.network.di

import com.minhaestante.core.network.BuscaRemoteDataSource
import com.minhaestante.core.network.RetrofitBuscaRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Liga a interface pública [BuscaRemoteDataSource] à implementação com Retrofit. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class RemoteDataSourceModule {

    @Binds
    abstract fun bindBuscaRemoteDataSource(
        implementacao: RetrofitBuscaRemoteDataSource,
    ): BuscaRemoteDataSource
}
