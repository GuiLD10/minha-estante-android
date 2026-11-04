package com.minhaestante

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.minhaestante.core.lembrete.criarCanalDeLembrete
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * `@HiltAndroidApp`: dispara a geração de código do Hilt e cria o container de
 * dependências raiz (`SingletonComponent`), que vive tanto quanto a Application.
 *
 * `Configuration.Provider`: por padrão o WorkManager se inicializa sozinho ao abrir o app e
 * cria os Workers por reflexão, só com `(Context, WorkerParameters)`. Assim ele não saberia
 * injetar os repositórios do `LembreteWorker`. Por isso desligamos a inicialização automática
 * no AndroidManifest e entregamos aqui uma configuração com a [HiltWorkerFactory].
 * O WorkManager é então inicializado sob demanda, na primeira vez que alguém o usa.
 */
@HiltAndroidApp
class MinhaEstanteApp : Application(), Configuration.Provider {

    /** Injeção em campo: a Application é criada pelo Android, então não há construtor para injetar. */
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        // super.onCreate() é onde o Hilt preenche os campos @Inject.
        super.onCreate()
        criarCanalDeLembrete(this)
    }
}
