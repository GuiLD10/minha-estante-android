plugins {
    alias(libs.plugins.android.library)
    // Hilt: a Activity e o Fragment são @AndroidEntryPoint e o ViewModel é @HiltViewModel.
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
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
        // BuildConfig.DEBUG: liga os logs de ciclo de vida só em builds de debug.
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.lembrete)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    // fragment-ktx traz os delegates `by viewModels()` / `by activityViewModels()`.
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)

    // viewModelScope, lifecycleScope e repeatOnLifecycle.
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Fakes, MainDispatcherRule, JUnit e coroutines-test compartilhados.
    testImplementation(projects.core.testing)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
}
