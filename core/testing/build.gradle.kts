// Módulo com utilitários de teste compartilhados (fakes, regras do JUnit).
// Os outros módulos o usam apenas como `testImplementation`, então nada daqui vai para o APK.
plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.minhaestante.core.testing"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // `api`: quem usa os fakes também precisa enxergar as interfaces e modelos que eles implementam.
    api(projects.core.data)
    api(projects.core.model)

    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
}
