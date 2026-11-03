package com.minhaestante.core.database.di

import android.content.Context
import com.minhaestante.core.database.BibliotecaDatabase
import com.minhaestante.core.database.dao.LivroDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Ensina ao Hilt **como criar** o banco e o DAO.
 *
 * Usamos `@Provides` (e não `@Binds`) porque não somos nós que chamamos o construtor:
 * o banco é criado pelo `Room.databaseBuilder` e o DAO pelo próprio banco.
 *
 * `@InstallIn(SingletonComponent::class)`: estas dependências vivem tanto quanto a Application.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    /**
     * `@Singleton`: o Hilt cria o banco uma única vez e reaproveita a mesma instância
     * em todo o app (substitui o `by lazy` do DataContainer da etapa 3).
     */
    @Provides
    @Singleton
    fun provideBibliotecaDatabase(
        @ApplicationContext context: Context,
    ): BibliotecaDatabase = BibliotecaDatabase.construir(context)

    /** Sem `@Singleton`: o DAO é barato e o Room já devolve sempre o mesmo objeto. */
    @Provides
    fun provideLivroDao(database: BibliotecaDatabase): LivroDao = database.livroDao()
}
