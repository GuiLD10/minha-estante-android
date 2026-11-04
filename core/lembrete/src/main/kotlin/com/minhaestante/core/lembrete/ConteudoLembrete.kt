package com.minhaestante.core.lembrete

import com.minhaestante.core.data.repository.EstanteRepository
import com.minhaestante.core.data.repository.PreferenciasRepository
import kotlinx.coroutines.flow.first

/** O que a notificação do lembrete vai dizer. */
internal sealed interface ConteudoLembrete {
    /** "Você está lendo {título}. Que tal 20 minutos hoje?" */
    data class UmLivro(val titulo: String) : ConteudoLembrete

    /** "Você tem {n} livros em andamento" */
    data class VariosLivros(val quantidade: Int) : ConteudoLembrete
}

/**
 * Decide **se** o lembrete deve ser enviado e **com qual conteúdo**. `null` = não notificar.
 *
 * Fica fora do [LembreteWorker] de propósito: testar um Worker exige o framework do
 * WorkManager (e um `Context`), enquanto esta função roda num teste unitário comum,
 * só com os repositórios fake.
 */
internal suspend fun prepararLembrete(
    estante: EstanteRepository,
    preferencias: PreferenciasRepository,
): ConteudoLembrete? {
    // `first()`: lê o valor atual do Flow e para de observar. O Worker roda uma vez e termina.
    if (!preferencias.lembreteAtivo.first()) return null

    val lendo = estante.livrosSendoLidos()
    return when (lendo.size) {
        0 -> null
        1 -> ConteudoLembrete.UmLivro(lendo.single().titulo)
        else -> ConteudoLembrete.VariosLivros(lendo.size)
    }
}
