plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.fslab.pedidos"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.fslab.pedidos"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    buildFeatures {
        compose = true
    }
}

dependencies {
<<<<<<< Updated upstream
=======
    // 0. LOCATION
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // 1. FIREBASE (Gerenciamento de versões via BoM)
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-messaging")
>>>>>>> Stashed changes

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
<<<<<<< Updated upstream
=======
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.7.8")

    // 3. ANDROID CORE & LIFECYCLE
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // 4. PERSISTÊNCIA & SEGURANÇA
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // 5. REDE (Retrofit, OkHttp, Socket.IO)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("io.socket:socket.io-client:2.1.1") {
        exclude(group = "org.json", module = "json")
    }

    // 6. MULTIMÍDIA & IA (CameraX, ML Kit, Coil)
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-svg:2.5.0")

    // 7. COROUTINES
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // 8. TESTES
>>>>>>> Stashed changes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // UI - Jetpack Compose + Material 3
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended")

    // DataStore Preferences - persistência de tema e preferências
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // HTTP - Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines para operações assíncronas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // WebSocket - Socket.IO (notificações em tempo real)
    implementation("io.socket:socket.io-client:2.1.1") {
        exclude(group = "org.json", module = "json")
    }

    // CameraX - acesso completo à câmera
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // ML Kit Barcode Scanning - leitura de QR Codes
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // Google Fonts - fontes do Google Fonts via Compose
    implementation("androidx.compose.ui:ui-text-google-fonts:1.7.8")

    // Coil - carregamento assíncrono de imagens (avatares, etc.)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Armazenamento seguro de credenciais (AES-256 + Android Keystore)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Autenticação biométrica (impressão digital / reconhecimento facial)
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Firebase Cloud Messaging (notificações push)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
}