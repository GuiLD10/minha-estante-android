package com.minhaestante.core.lembrete

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Implementação de [AgendadorLembrete] com WorkManager.
 *
 * Por que WorkManager? Ele guarda os trabalhos num banco próprio, então o agendamento
 * **sobrevive** ao app ser fechado e ao celular ser reiniciado, e respeita as regras de
 * economia de bateria do sistema (Doze). Por isso o horário é aproximado: o sistema pode
 * atrasar um pouco a execução, o que é aceitável para um lembrete.
 */
internal class WorkManagerAgendadorLembrete @Inject constructor(
    private val workManager: WorkManager,
) : AgendadorLembrete {

    override fun agendar() {
        // O intervalo mínimo de um trabalho periódico é 15 min; 24h é o nosso "todo dia".
        // Não usamos `Constraints` (ex.: exigir internet ou carregador): o lembrete só lê
        // dados locais e deve sair mesmo com bateria baixa ou offline.
        val pedido = PeriodicWorkRequestBuilder<LembreteWorker>(INTERVALO_HORAS, TimeUnit.HOURS)
            // O primeiro disparo espera até as próximas 20h; os seguintes vêm a cada 24h.
            .setInitialDelay(atrasoAte(HORARIO_LEMBRETE, ZonedDateTime.now()))
            .build()

        // "Unique work": no máximo um trabalho com este nome. `UPDATE` substitui a configuração
        // do trabalho existente em vez de criar um segundo (ativar o lembrete duas vezes não
        // gera duas notificações por dia).
        workManager.enqueueUniquePeriodicWork(NOME_TRABALHO, ExistingPeriodicWorkPolicy.UPDATE, pedido)
    }

    override fun cancelar() {
        workManager.cancelUniqueWork(NOME_TRABALHO)
    }

    override fun dispararAgora() {
        val pedido = OneTimeWorkRequestBuilder<LembreteWorker>().build()
        // Nome diferente do periódico: disparar o teste não pode cancelar o agendamento diário.
        workManager.enqueueUniqueWork(NOME_TRABALHO_TESTE, ExistingWorkPolicy.REPLACE, pedido)
    }

    private companion object {
        const val NOME_TRABALHO = "lembrete_leitura"
        const val NOME_TRABALHO_TESTE = "lembrete_leitura_teste"
        const val INTERVALO_HORAS = 24L
        val HORARIO_LEMBRETE: LocalTime = LocalTime.of(20, 0)
    }
}

/**
 * Quanto falta, a partir de [agora], até a próxima ocorrência de [horario].
 * Se já passou (ou é exatamente agora), conta até o mesmo horário de amanhã.
 *
 * Usa `ZonedDateTime` (com fuso) para a conta ficar certa em dias de mudança de horário.
 * Recebe `agora` como parâmetro (em vez de chamar `now()` aqui dentro) para ser testável.
 */
internal fun atrasoAte(horario: LocalTime, agora: ZonedDateTime): Duration {
    val hoje = agora.with(horario).withSecond(0).withNano(0)
    val proximo = if (hoje.isAfter(agora)) hoje else hoje.plusDays(1)
    return Duration.between(agora, proximo)
}
