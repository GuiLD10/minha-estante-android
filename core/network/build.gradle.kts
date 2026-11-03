plugins {
    alias(libs.plugins.android.library)
    // Gera os serializers dos DTOs (@Serializable) em tempo de compilação.
    alias(libs.plugins.kotlin.serialization)
    // Hilt: este módulo fornece o NetworkModule (OkHttp, Retrofit e a API).
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.minhaestante.core.network"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Gera o campo BuildConfig.BASE_URL, disponível no código deste módulo.
        buildConfigField("String", "BASE_URL", "\"https://openlibrary.org/\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        // A partir do AGP 8, BuildConfig é desligado por padrão e precisa ser habilitado.
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.model)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
}
