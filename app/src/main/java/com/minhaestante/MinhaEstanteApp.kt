package com.minhaestante

import android.app.Application
import com.minhaestante.core.data.DataContainer
import com.minhaestante.core.data.exemplo.inserirLivrosDeExemplo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application: criada uma única vez, antes de qualquer Activity, e vive enquanto o
 * processo do app existir. Por isso é o lugar certo para guardar dependências
 * compartilhadas (singletons), como o banco de dados.
 */
class MinhaEstanteApp : Application() {

    /** DI manual (etapa 3). `by lazy` cria o container só no primeiro uso. */
    val dataContainer: DataContainer by lazy { DataContainer(this) }

    /** Escopo de coroutines que vive tanto quanto o app. */
    private val escopoApp = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        // TEMPORÁRIO (etapa 3): livros de exemplo apenas em debug; removido na etapa 5.
        if (BuildConfig.DEBUG) {
            escopoApp.launch { inserirLivrosDeExemplo(dataContainer.estanteRepository) }
        }
    }
}
