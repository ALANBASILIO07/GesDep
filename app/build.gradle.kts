plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.uaemex.gesdep"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.uaemex.gesdep"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging") // Notificaciones Push
    implementation("com.google.firebase:firebase-analytics")

    // Material Design
    implementation("com.google.android.material:material:1.12.0")

    // Maps + Location
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // AppCompat
    implementation("androidx.appcompat:appcompat:1.7.0")

    // ConstraintLayout
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.activity)

    // GridLayout
    implementation("androidx.gridlayout:gridlayout:1.0.0")

    // WorkManager para sincronización en background
    implementation("androidx.work:work-runtime:2.9.0")

    // Glide para carga de imágenes
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // CameraX para funcionalidad de cámara
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")

    // Navigation Component para Fragments (Fase 10)
    val navVersion = "2.7.7"
    implementation("androidx.navigation:navigation-fragment:$navVersion")
    implementation("androidx.navigation:navigation-ui:$navVersion")

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
}