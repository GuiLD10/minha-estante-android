package com.minhaestante.core.lembrete

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

private const val ID_CANAL = "lembrete_leitura"
private const val ID_NOTIFICACAO = 1
private const val CODIGO_PENDING_INTENT = 0

/**
 * Cria o canal de notificação do lembrete. Desde o Android 8 (nosso minSdk), toda notificação
 * precisa de um canal; é por canal que o usuário silencia ou desativa notificações nas
 * configurações do sistema. Chamar de novo é seguro: se o canal já existe, nada muda.
 *
 * Chamado pelo `MinhaEstanteApp.onCreate()`, para o canal existir antes da primeira notificação.
 */
fun criarCanalDeLembrete(context: Context) {
    val canal = NotificationChannelCompat.Builder(ID_CANAL, NotificationManagerCompat.IMPORTANCE_DEFAULT)
        .setName(context.getString(R.string.lembrete_canal_nome))
        .setDescription(context.getString(R.string.lembrete_canal_descricao))
        .build()
    NotificationManagerCompat.from(context).createNotificationChannel(canal)
}

/** Monta e exibe a notificação do lembrete. */
internal class NotificadorLembrete(private val context: Context) {

    fun notificar(conteudo: ConteudoLembrete) {
        // Android 13+: sem a permissão, `notify` seria ignorado (ou lançaria SecurityException).
        // O usuário pode revogá-la a qualquer momento nas configurações do sistema.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val texto = texto(conteudo)
        val notificacao = NotificationCompat.Builder(context, ID_CANAL)
            .setSmallIcon(R.drawable.ic_notificacao_lembrete)
            .setContentTitle(context.getString(R.string.lembrete_titulo))
            .setContentText(texto)
            // Mostra o texto inteiro quando a notificação é expandida (títulos longos).
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(intentParaAbrirApp())
            .setAutoCancel(true) // some ao ser tocada
            .build()

        NotificationManagerCompat.from(context).notify(ID_NOTIFICACAO, notificacao)
    }

    private fun texto(conteudo: ConteudoLembrete): String = when (conteudo) {
        is ConteudoLembrete.UmLivro -> context.getString(R.string.lembrete_texto_um_livro, conteudo.titulo)
        is ConteudoLembrete.VariosLivros -> context.resources.getQuantityString(
            R.plurals.lembrete_texto_varios_livros,
            conteudo.quantidade, // escolhe a forma (singular/plural)...
            conteudo.quantidade, // ...e preenche o %1$d
        )
    }

    /**
     * `PendingIntent`: um Intent "embrulhado" que outro processo (a barra de notificações do
     * sistema) pode disparar em nome do app. `FLAG_IMMUTABLE` é obrigatório desde o Android 12.
     *
     * Usamos o Intent de abertura do app (a Activity com `LAUNCHER`, a `MainActivity`) em vez de
     * referenciar `MainActivity::class`: um módulo `core` não pode depender do `:app`.
     */
    private fun intentParaAbrirApp(): PendingIntent? {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            ?: return null
        return PendingIntent.getActivity(context, CODIGO_PENDING_INTENT, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}
