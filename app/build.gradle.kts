plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // used for room annotation processing
}

android {
    namespace = "com.example.homework3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.homework3"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // these 4 are for view models and livedata
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0") // enables LiveData
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") // enables viewModelScope
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0") // enables ViewModel
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.preference:preference:1.2.1") // enables (by) viewModels()

    implementation("com.github.bumptech.glide:glide:4.15.1") //for loading images, cashing images

    // Android room dependencies, Entity, DAO, Database
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Workmanager dependency
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}