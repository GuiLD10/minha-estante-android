package com.minhaestante.core.lembrete

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.minhaestante.core.data.repository.EstanteRepository
import com.minhaestante.core.data.repository.PreferenciasRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Trabalho em segundo plano que envia o lembrete diário de leitura.
 *
 * `CoroutineWorker`: o `doWork()` é uma função `suspend` e roda fora da main thread
 * (em `Dispatchers.Default`), então podemos chamar o Room e o DataStore direto.
 *
 * `@HiltWorker` + `@AssistedInject`: quem cria o Worker é o WorkManager, não nós. Ele fornece
 * `context` e `params` (os parâmetros "assistidos", `@Assisted`), e o Hilt completa o resto
 * (os repositórios). Isso só funciona porque o app registra a `HiltWorkerFactory`
 * no `MinhaEstanteApp`.
 */
@HiltWorker
class LembreteWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val estanteRepository: EstanteRepository,
    private val preferenciasRepository: PreferenciasRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Não ter o que lembrar (lembrete desligado ou nenhum livro "Lendo") não é erro:
        // `failure()` ou `retry()` fariam o WorkManager tratar como falha e tentar de novo à toa.
        val conteudo = prepararLembrete(estanteRepository, preferenciasRepository)
            ?: return Result.success()

        NotificadorLembrete(applicationContext).notificar(conteudo)
        return Result.success()
    }
}
