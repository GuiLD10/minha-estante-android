package com.minhaestante.core.lembrete.di

import android.content.Context
import androidx.work.WorkManager
import com.minhaestante.core.lembrete.AgendadorLembrete
import com.minhaestante.core.lembrete.WorkManagerAgendadorLembrete
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * `@Binds` liga a interface à implementação (o Hilt sabe construí-la pelo `@Inject constructor`).
 * `@Provides` é necessário para o [WorkManager], que não tem construtor `@Inject`: é obtido
 * por `getInstance`. Por isso a classe é abstrata e os `@Provides` ficam no `companion object`.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class LembreteModule {

    @Binds
    abstract fun bindAgendadorLembrete(implementacao: WorkManagerAgendadorLembrete): AgendadorLembrete

    companion object {
        @Provides
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
            WorkManager.getInstance(context)
    }
}
