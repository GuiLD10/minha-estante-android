package com.minhaestante.feature.configuracoes

import androidx.appcompat.app.AppCompatDelegate
import com.minhaestante.core.model.TemaApp
import org.junit.Assert.assertEquals
import org.junit.Test

class ModoNoturnoTest {

    @Test
    fun `quando o tema e SISTEMA, entao segue o modo do sistema`() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, TemaApp.SISTEMA.paraModoNoturno())
    }

    @Test
    fun `quando o tema e CLARO, entao desliga o modo noturno`() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, TemaApp.CLARO.paraModoNoturno())
    }

    @Test
    fun `quando o tema e ESCURO, entao liga o modo noturno`() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, TemaApp.ESCURO.paraModoNoturno())
    }
}
