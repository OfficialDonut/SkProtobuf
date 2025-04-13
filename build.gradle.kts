plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "8.3.6"
}

group = "com.github.officialdonut"
version = "0.0.3"

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public") }
    maven { url = uri("https://repo.skriptlang.org/releases") }
}

dependencies {
    api("com.google.protobuf:protobuf-java:4.30.0")
    api("com.google.protobuf:protobuf-java-util:4.30.0")

    compileOnly("com.github.SkriptLang:Skript:2.10.2")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    expand("version" to project.version)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
