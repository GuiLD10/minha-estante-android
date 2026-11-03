package com.minhaestante.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Um livro como vem da Open Library (DTO = Data Transfer Object).
 *
 * - `@SerialName` liga o nome do campo no JSON (snake_case, em inglês) ao nome em Kotlin.
 * - Os valores padrão (`= null`, `= emptyList()`) tornam o campo opcional: a API
 *   omite campos que não conhece para aquele livro, e sem padrão a desserialização falharia.
 *
 * `internal`: o DTO nunca sai deste módulo; fora daqui só se vê [com.minhaestante.core.model.ResultadoBusca].
 */
@Serializable
internal data class LivroDto(
    val key: String,
    val title: String,
    @SerialName("author_name") val autores: List<String> = emptyList(),
    @SerialName("first_publish_year") val ano: Int? = null,
    @SerialName("cover_i") val capaId: Long? = null,
    @SerialName("number_of_pages_median") val paginas: Int? = null,
)
