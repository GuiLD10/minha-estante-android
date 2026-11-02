plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.minhaestante.feature.configuracoes"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        // Gera classes de binding para os layouts XML (ex.: FragmentConfiguracoesBinding).
        viewBinding = true
    }
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.lembrete)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)

    testImplementation(libs.junit)
}
