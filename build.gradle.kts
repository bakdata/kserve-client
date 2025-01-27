buildscript {
    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
        }
    }
    dependencies {
        classpath("com.bakdata.gradle:sonatype:1.7.1-SNAPSHOT")
    }
}

description = "A Java client for KServe inference services."

plugins {
    `java-library`
    id("com.bakdata.release") version "1.6.1"
    id("com.bakdata.sonar") version "1.6.1"
//    id("com.bakdata.sonatype") version "1.6.1"
    id("io.freefair.lombok") version "8.11"
    id("java-test-fixtures")
}
apply(plugin = "com.bakdata.sonatype")

group = "com.bakdata.kserve"

tasks.withType<Test> {
    maxParallelForks = 4
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

dependencies {
    implementation(group = "org.jsoup", name = "jsoup", version = "1.18.3")
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.18.2")
    val okHttpVersion: String by project
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = okHttpVersion)
    implementation(group = "org.json", name = "json", version = "20250107")
    implementation(group = "io.github.resilience4j", name = "resilience4j-retry", version = "1.7.1")
    implementation(group = "org.slf4j", name = "slf4j-api", version = "2.0.16")

    val junitVersion: String by project
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = junitVersion)
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-params", version = junitVersion)
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = junitVersion)
    testImplementation(group = "org.assertj", name = "assertj-core", version = "3.27.2")
    testImplementation(group = "com.squareup.okhttp3", name = "mockwebserver", version = okHttpVersion)

    testFixturesImplementation(group = "com.squareup.okhttp3", name = "mockwebserver", version = okHttpVersion)
}

configure<com.bakdata.gradle.SonatypeSettings> {
    developers {
        developer {
            name.set("Victor Künstler")
            id.set("VictorKuenstler")
        }
        developer {
            name.set("Alejandro Jaramillo")
            id.set("irux")
        }
        developer {
            name.set("Jakob Edding")
            id.set("jakob-ed")
        }
    }
}
