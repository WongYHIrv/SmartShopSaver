plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.smartshopsaver"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.smartshopsaver"
        minSdk = 22
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0_b11"

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

    packagingOptions {
        exclude ("META-INF/DEPENDENCIES")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.google.firebase:firebase-analytics:21.3.0")
    implementation("com.google.firebase:firebase-auth:22.1.1")
    implementation("com.google.firebase:firebase-database:20.2.2")
    implementation("com.google.firebase:firebase-storage:20.2.1")
    implementation("com.google.firebase:firebase-crashlytics:18.4.1")
    implementation("com.google.firebase:firebase-messaging:23.2.1")

    implementation ("com.squareup.picasso:picasso:2.8")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")


    //Location Updated: https://developers.google.com/android/guides/setup
    implementation("com.google.android.gms:play-services-location:21.0.1")
    //Barcode Scanner: https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner#java
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
    //Volley: https://google.github.io/volley/
    implementation("com.android.volley:volley:1.2.1")
    //Image Processing: https://github.com/bumptech/glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    //Graphs: https://github.com/AnyChart/AnyChart-Android
    implementation("com.github.AnyChart:AnyChart-Android:1.1.5")

    //Google Management Services (GMS) for Locations
    implementation ("com.google.android.gms:play-services-maps:17.0.1")
    implementation ("com.google.maps:google-maps-services:0.18.0")
    implementation ("com.google.maps.android:android-maps-utils:2.2.0")
    implementation ("com.google.api-client:google-api-client:1.32.1")

    //ML Kit for Optical Character Recognition - OCR
    implementation ("com.google.android.gms:play-services-base:18.2.0")
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition:18.0.0")
    implementation ("com.google.mlkit:vision-common:17.1.0")
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition-common:19.0.0")
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}