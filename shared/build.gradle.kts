import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties

val buildConfigs = mapOf(
    "nordigenSecret" to "nordigenSecret",
    "nordigenClientId" to "nordigenClientId",
    "chatGptSecret" to "chatGptSecret",
    "currencyApiSecret" to "currencyApiSecret",
    "bingSecret" to "bingSecret"

)
lateinit var _secrets: Properties
val secrets: Properties
    get() {
        if(!::_secrets.isInitialized) {
            _secrets = Properties()
            val localPropertiesFile = File("/Users/sanderhoyvik/environment-files/sandeerbank/sandeerapp.secrets.properties")
            println(localPropertiesFile.absolutePath.toString())
            println(localPropertiesFile.exists())
            if (localPropertiesFile.isFile) {
                InputStreamReader(FileInputStream(localPropertiesFile), Charsets.UTF_8).use { reader ->
                    secrets.load(reader)
                }
            }
        }
        return _secrets
    }

val buildConfigGenerator by tasks.registering(Sync::class) {
    from(
        resources.text.fromString(
            """
        |package generated
        |
        |object BuildConfig {
        |  ${
                buildConfigs.entries.joinToString("\n") {
                    "val ${it.key}: String = \"${secrets.getProperty(it.value).replace("\"", "\\\"")}\""
                }
            }
        |}
        |
      """.trimMargin()
        )
    ) {
        rename { "BuildConfig.kt" } // set the file name
        into("my/project/") // change the directory to match the package
    }

    into(layout.buildDirectory.dir("generated-src/kotlin/"))
}

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    kotlin("plugin.serialization") version "1.8.10"
    id("com.android.library")
    id("org.jetbrains.compose")
    id("io.realm.kotlin")
}

kotlin {
    android()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0.0"
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
        }
        extraSpecAttributes["resources"] = "['src/commonMain/resources/**', 'src/iosMain/resources/**']"
    }

    sourceSets {
        val ktorVersion = "2.3.0"
        val commonMain by getting {
            kotlin.srcDirs(buildConfigGenerator.map { it.destinationDir })

            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

                api("io.insert-koin:koin-core:3.2.0")


                implementation("com.russhwolf:multiplatform-settings:1.0.0")
                implementation("com.russhwolf:multiplatform-settings-no-arg:1.0.0")

                implementation("io.realm.kotlin:library-base:1.7.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.6.1")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.9.0")

                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.myapplication.common"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
    }
}