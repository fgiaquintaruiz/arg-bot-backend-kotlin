import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.spring") version "2.0.20"
    jacoco
}

group = "com.argbot"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Observability
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Resilience (Circuit Breaker — patrón Decorator vía AOP)
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

springBoot {
    buildInfo()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}
