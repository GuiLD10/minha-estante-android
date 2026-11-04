package com.minhaestante.core.lembrete

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class AtrasoAteTest {

    private val vinteHoras = LocalTime.of(20, 0)
    private val fuso = ZoneId.of("America/Sao_Paulo")

    private fun em(hora: Int, minuto: Int = 0) =
        ZonedDateTime.of(LocalDateTime.of(2026, 11, 3, hora, minuto), fuso)

    @Test
    fun `quando ainda nao deu o horario, entao espera ate hoje`() {
        assertEquals(Duration.ofHours(6), atrasoAte(vinteHoras, em(14)))
    }

    @Test
    fun `quando ja passou do horario, entao espera ate amanha`() {
        assertEquals(Duration.ofHours(23).plusMinutes(30), atrasoAte(vinteHoras, em(20, 30)))
    }

    @Test
    fun `quando e exatamente o horario, entao espera ate amanha`() {
        assertEquals(Duration.ofHours(24), atrasoAte(vinteHoras, em(20)))
    }

    @Test
    fun `quando agora tem segundos, entao o alvo continua sendo a hora cheia`() {
        val agora = em(19, 59).withSecond(30)

        assertEquals(Duration.ofSeconds(30), atrasoAte(vinteHoras, agora))
    }

    @Test
    fun `quando o proximo dia muda o horario de verao, entao a conta respeita o fuso`() {
        // Nova York adianta o relógio em 8/3/2026: aquele dia tem só 23 horas.
        val novaYork = ZoneId.of("America/New_York")
        val agora = ZonedDateTime.of(LocalDateTime.of(2026, 3, 7, 21, 0), novaYork)

        assertEquals(Duration.ofHours(22), atrasoAte(vinteHoras, agora))
    }
}
