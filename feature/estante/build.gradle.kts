plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    // Hilt: o EstanteViewModel é um @HiltViewModel.
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.minhaestante.feature.estante"
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
        compose = true
    }
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // ViewModel + viewModel() no Compose e collectAsStateWithLifecycle()
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // Fornece hiltViewModel() para obter ViewModels do Hilt dentro de Composables.
    implementation(libs.androidx.hilt.navigation.compose)

    // Fakes, MainDispatcherRule, JUnit e coroutines-test compartilhados.
    testImplementation(projects.core.testing)
    testImplementation(libs.turbine)
}
