package com.minhaestante.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.minhaestante.core.database.converter.Converters
import com.minhaestante.core.database.dao.LivroDao
import com.minhaestante.core.database.entity.LivroEntity

/**
 * Banco de dados do app.
 *
 * Qualquer mudança de esquema (nova coluna, nova tabela...) exige incrementar
 * [version] e escrever uma `Migration`; senão o app quebra para quem já tem o banco instalado.
 */
@Database(
    entities = [LivroEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class BibliotecaDatabase : RoomDatabase() {

    abstract fun livroDao(): LivroDao

    companion object {
        private const val NOME_ARQUIVO = "biblioteca.db"

        /**
         * Cria o banco. Deve existir **uma única instância** no app inteiro
         * (criar o banco é caro, e várias instâncias não se enxergam nos Flows).
         */
        fun construir(context: Context): BibliotecaDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                BibliotecaDatabase::class.java,
                NOME_ARQUIVO,
            ).build()
    }
}
