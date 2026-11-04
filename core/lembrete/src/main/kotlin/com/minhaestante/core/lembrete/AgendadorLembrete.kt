package com.minhaestante.core.lembrete

/**
 * Agenda e cancela o lembrete diário. É uma interface para que o `ConfiguracoesViewModel`
 * possa ser testado com um mock, sem WorkManager de verdade.
 */
interface AgendadorLembrete {
    /** Agenda o lembrete para todos os dias às 20h. Chamar de novo só atualiza o agendamento. */
    fun agendar()

    fun cancelar()

    /** Executa o lembrete imediatamente, uma única vez. Uso apenas em builds debug, para testes. */
    fun dispararAgora()
}
