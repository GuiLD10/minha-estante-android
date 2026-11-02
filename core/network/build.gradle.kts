plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
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

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}
