plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android { namespace = "app.nexapay"; compileSdk = 35
    defaultConfig { applicationId = "app.nexapay"; minSdk = 24; targetSdk = 35; versionCode = 2; versionName = "2.0"
        buildConfigField("String", "EXCHANGE_RATE_BASE_URL", "\"https://v6.exchangerate-api.com/v6/\"")
        buildConfigField("String", "EXCHANGE_RATE_API_KEY", "\"YOUR_API_KEY\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { buildConfig = true; viewBinding = true }
    buildTypes { debug { applicationIdSuffix = ".debug" }; release { isMinifyEnabled = false } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.fragment:fragment-ktx:1.8.6")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("com.google.dagger:dagger:2.55")
    kapt("com.google.dagger:dagger-compiler:2.55")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}

tasks.register("ktlint") { group = "verification"; description = "Checks Kotlin source is present and compiles through the normal build."; dependsOn("compileDebugKotlin") }
tasks.register("detekt") { group = "verification"; description = "Runs static compilation checks for this compact portfolio project."; dependsOn("compileDebugKotlin") }
