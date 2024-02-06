description = "A Java client for KServe inference services."

plugins {
    `java-library`
    id("net.researchgate.release") version "3.0.2"
    id("com.bakdata.sonar") version "1.1.11"
    id("com.bakdata.sonatype") version "1.1.11"
    id("org.hildan.github.changelog") version "1.12.1"
    id("io.freefair.lombok") version "6.6.3"
    id("java-test-fixtures")
}

group = "com.bakdata.kserve"

tasks.withType<Test> {
    maxParallelForks = 4
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(group = "org.jsoup", name = "jsoup", version = "1.17.2")
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.16.1")
    val okHttpVersion: String by project
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = okHttpVersion)
    implementation(group = "org.json", name = "json", version = "20231013")
    implementation(group = "io.github.resilience4j", name = "resilience4j-retry", version = "1.7.1")
    implementation(group = "org.slf4j", name = "slf4j-api", version = "2.0.10")

    val junitVersion: String by project
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = junitVersion)
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = junitVersion)
    testImplementation(group = "org.assertj", name = "assertj-core", version = "3.25.1")
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

configure<org.hildan.github.changelog.plugin.GitHubChangelogExtension> {
    githubUser = "bakdata"
    githubRepository = "kserve-client"
    futureVersionTag = findProperty("changelog.releaseVersion")?.toString()
    sinceTag = findProperty("changelog.sinceTag")?.toString()
}
