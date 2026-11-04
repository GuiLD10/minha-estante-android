package com.minhaestante.feature.configuracoes

import androidx.appcompat.app.AppCompatDelegate
import com.minhaestante.core.model.TemaApp

/**
 * Converte o tema do domínio no "night mode" do AppCompat, usado em `delegate.localNightMode`.
 *
 * Fica fora do `:core:model` porque depende de uma classe do Android (AppCompatDelegate);
 * o domínio continua sem saber nada de Android.
 */
internal fun TemaApp.paraModoNoturno(): Int = when (this) {
    TemaApp.SISTEMA -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    TemaApp.CLARO -> AppCompatDelegate.MODE_NIGHT_NO
    TemaApp.ESCURO -> AppCompatDelegate.MODE_NIGHT_YES
}
