description = "A Java client for KServe inference services."

plugins {
    `java-library`
    id("net.researchgate.release") version "2.8.1"
    id("com.bakdata.sonar") version "1.1.7"
    id("com.bakdata.sonatype") version "1.1.7"
    id("org.hildan.github.changelog") version "0.8.0"
    id("io.freefair.lombok") version "5.3.3.3"
}

group = "com.bakdata.kserve"

tasks.withType<Test> {
    maxParallelForks = 4
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(group = "org.jsoup", name = "jsoup", version = "1.13.1")
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.12.5")
    val okHttpVersion: String by project
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = okHttpVersion)
    implementation(group = "org.json", name = "json", version = "20211205")
    implementation(group = "io.github.resilience4j", name = "resilience4j-retry", version = "1.7.1")
    implementation(group = "org.slf4j", name = "slf4j-api", version = "1.7.25")

    val junitVersion: String by project
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = junitVersion)
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = junitVersion)
    testImplementation(group = "com.squareup.okhttp3", name = "mockwebserver", version = okHttpVersion)
    testImplementation(group = "org.assertj", name = "assertj-core", version = "3.22.0")
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

configure<org.hildan.github.changelog.plugin.GitHubChangelogExtension> {
    githubUser = "bakdata"
    githubRepository = "kserve-client"
    futureVersionTag = findProperty("changelog.releaseVersion")?.toString()
    sinceTag = findProperty("changelog.sinceTag")?.toString()
}