plugins {
    kotlin("jvm") version "2.2.0"
    application
    id("org.graalvm.buildtools.native") version "0.10.6"
    id("com.diffplug.spotless") version "7.0.4"
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.gitlab4j:gitlab4j-api:5.8.0")
    implementation("com.github.ajalt.clikt:clikt:5.0.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.2")
    testImplementation("org.wiremock:wiremock:3.13.1")
}

application {
    mainClass = "io.github.helpermethod.plumber.PlumberKt"
}

graalvmNative {
    metadataRepository {
        enabled = true
    }
    agent {
        metadataCopy {
            mergeWithExisting = true
            inputTaskNames.add("test")
            outputDirectories.add("src/main/resources/META-INF/native-image")
        }
    }
    binaries {
        named("main") {
            buildArgs("--enable-http", "--enable-https")
        }
        named("test") {
            buildArgs(
                "--initialize-at-build-time=kotlin.annotation.AnnotationRetention,kotlin.annotation.AnnotationTarget",
                "--enable-http",
                "--enable-https"
            )
        }
    }
}

spotless {
    kotlin {
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
