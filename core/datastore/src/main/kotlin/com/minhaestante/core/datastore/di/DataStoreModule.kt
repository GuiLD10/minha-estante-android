package com.minhaestante.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Cria o DataStore de preferências.
 *
 * `@Singleton` aqui não é só otimização: o DataStore **exige** uma única instância por arquivo.
 * Duas instâncias abrindo o mesmo arquivo lançam `IllegalStateException`.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object DataStoreModule {

    private const val NOME_ARQUIVO = "preferencias"

    @Provides
    @Singleton
    fun providePreferenciasDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(NOME_ARQUIVO) },
    )
}
