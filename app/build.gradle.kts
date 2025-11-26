plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.gesdep"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gesdep"
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

    // Material
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

    // PARA ARREGLAR EL ERROR DE GridLayout
    implementation("androidx.gridlayout:gridlayout:1.0.0")
}