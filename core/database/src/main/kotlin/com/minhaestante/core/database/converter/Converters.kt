package com.minhaestante.core.database.converter

import androidx.room.TypeConverter
import com.minhaestante.core.model.StatusLeitura
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Ensina o Room a guardar tipos que o SQLite não conhece.
 *
 * - `List<String>` vira texto JSON, ex.: `["Neil Gaiman","Terry Pratchett"]`.
 *   JSON foi escolhido (em vez de um separador como ";") porque funciona mesmo
 *   que um nome contenha o caractere separador.
 * - [StatusLeitura] vira o `name` do enum, ex.: `"LENDO"`. Por isso a query
 *   `WHERE status = 'LENDO'` do DAO funciona.
 *
 * `internal`: só o Room (dentro deste módulo) precisa usar.
 */
internal class Converters {

    private val serializadorAutores = ListSerializer(String.serializer())

    @TypeConverter
    fun autoresParaTexto(autores: List<String>): String =
        Json.encodeToString(serializadorAutores, autores)

    @TypeConverter
    fun textoParaAutores(texto: String): List<String> =
        Json.decodeFromString(serializadorAutores, texto)

    @TypeConverter
    fun statusParaTexto(status: StatusLeitura): String = status.name

    @TypeConverter
    fun textoParaStatus(texto: String): StatusLeitura = StatusLeitura.valueOf(texto)
}
