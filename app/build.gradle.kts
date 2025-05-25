plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "br.uscs.android.cidapp2"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.uscs.android.cidapp2"
        minSdk = 21 // Material 3 funciona bem com minSdk 21+
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Para usar Vector Drawables em APIs < 21 (embora seu minSdk seja 21, é uma boa prática)
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Considere habilitar para builds de produção (isMinifyEnabled = true)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    // Habilitar ViewBinding pode ser útil para acessar views nos seus Activities/Fragments/Adapters
    // de forma mais segura e concisa, embora não seja estritamente necessário para o Material 3.
    // Se você não for usar, pode remover este bloco.
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Dependências principais do AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat) // Necessário para AppCompatActivity
    implementation(libs.androidx.activity) // Para ComponentActivity, AppCompatActivity

    // Material Design Components (VOCÊ JÁ TEM ESTA, O QUE É ÓTIMO!)
    // Verifique se libs.material aponta para uma versão recente, ex: 'com.google.android.material:material:1.12.0'
    implementation(libs.material)

    // Para RecyclerView
    // Se você não tiver uma dependência explícita para RecyclerView, adicione-a.
    // Muitas vezes, ela é transitiva via appcompat, mas é bom ser explícito.
    // Verifique se você tem algo como libs.androidx.recyclerview ou adicione:
    implementation("androidx.recyclerview:recyclerview:1.3.2") // Ou use libs.androidx.recyclerview se definido

    // Para ConstraintLayout (usado no item_cid.xml atualizado)
    implementation(libs.androidx.constraintlayout)

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}