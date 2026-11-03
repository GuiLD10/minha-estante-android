package com.minhaestante

import android.app.Application
import com.minhaestante.core.data.exemplo.inserirLivrosDeExemplo
import com.minhaestante.core.data.repository.EstanteRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * `@HiltAndroidApp`: dispara a geração de código do Hilt e cria o container de
 * dependências raiz (`SingletonComponent`), que vive tanto quanto a Application.
 */
@HiltAndroidApp
class MinhaEstanteApp : Application() {

    /**
     * Injeção em campo: o Android cria a Application (não dá para usar construtor),
     * então o Hilt preenche este `lateinit` dentro de `super.onCreate()`.
     */
    @Inject
    lateinit var estanteRepository: EstanteRepository

    /** Escopo de coroutines que vive tanto quanto o app. */
    private val escopoApp = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate() // ← aqui o Hilt injeta os campos @Inject
        // TEMPORÁRIO (etapa 3): livros de exemplo apenas em debug; removido na etapa 5.
        if (BuildConfig.DEBUG) {
            escopoApp.launch { inserirLivrosDeExemplo(estanteRepository) }
        }
    }
}
