package com.minhaestante.core.network.di

import com.minhaestante.core.network.BuildConfig
import com.minhaestante.core.network.OpenLibraryApi
import com.minhaestante.core.network.openLibraryJson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import javax.inject.Singleton

/**
 * Ensina ao Hilt como montar a pilha de rede: OkHttp (cliente HTTP) → Retrofit → API.
 *
 * Tudo `@Singleton`: um único OkHttpClient reaproveita conexões e threads em todo o app.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    private const val TIPO_JSON = "application/json"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .apply {
            // O log com o corpo completo ajuda a estudar e depurar, mas não deve ir para produção
            // (polui o Logcat e pode expor dados). Por isso só em builds debug.
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY },
                )
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        // Converte o JSON da resposta nos DTOs @Serializable.
        .addConverterFactory(openLibraryJson.asConverterFactory(TIPO_JSON.toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideOpenLibraryApi(retrofit: Retrofit): OpenLibraryApi = retrofit.create()
}
