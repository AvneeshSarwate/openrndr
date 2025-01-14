plugins {
    kotlin("multiplatform")
}

val kotlinxSerializationVersion: String by rootProject.extra
val kotestVersion: String by rootProject.extra
val junitJupiterVersion: String by rootProject.extra
val kotlinLoggingVersion: String by rootProject.extra
val kotlinApiVersion: String by rootProject.extra
val kotlinJvmTarget: String by rootProject.extra

val embedShaders = tasks.register<EmbedShadersTask>("embedShaders") {
    inputDir.set(file("$projectDir/src/shaders/glsl"))
    outputDir.set(file("$buildDir/generated/shaderKotlin"))

    defaultPackage.set("org.openrndr.filter")
    defaultVisibility.set("")
    namePrefix.set("filter_")
}.get()

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = kotlinJvmTarget
            kotlinOptions.apiVersion = kotlinApiVersion
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        browser()
        nodejs()
    }

    sourceSets {
        val shaderKotlin by creating {
            this.kotlin.srcDir(embedShaders.outputDir)
        }

        @Suppress("UNUSED_VARIABLE")
        val commonMain by getting {
            dependencies {
                implementation(project(":openrndr-draw"))
                implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
                api(shaderKotlin.kotlin)
            }
            dependsOn(shaderKotlin)
        }

        @Suppress("UNUSED_VARIABLE")
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("io.kotest:kotest-assertions-core:$kotestVersion")
            }
        }
    }
}