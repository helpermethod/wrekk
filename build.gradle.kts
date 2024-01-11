plugins {
    kotlin("jvm") version "1.9.22"
    id("com.diffplug.spotless") version "6.23.3"
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.gitlab4j:gitlab4j-api:5.4.0")
    implementation("com.github.ajalt.clikt:clikt:4.2.1")
    testImplementation(platform("io.kotest:kotest-bom:5.8.0"))
    testImplementation("io.kotest:kotest-runner-junit5")
    testImplementation("io.kotest:kotest-framework-datatest")
    testImplementation("io.kotest.extensions:kotest-extensions-wiremock:2.0.1")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

spotless {
    kotlin {
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
}
