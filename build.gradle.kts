plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.nandi0813"
version = "3.1-SNAPSHOT"
description = "ZonePractice Lite"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()

    maven {
        name = "hpfxd-repo"
        url = uri("https://repo.hpfxd.com/releases/")
    }
    maven {
        name = "dmulloy2-repo"
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }
    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        name = "codemc-releases"
        url = uri("https://repo.codemc.io/repository/maven-releases/")
    }
    maven {
        name = "codemc-snapshots"
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
    }

    dependencies {
        compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
        compileOnly("org.projectlombok:lombok:1.18.34")
        annotationProcessor("org.projectlombok:lombok:1.18.34")
        compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
        compileOnly("me.clip:placeholderapi:2.11.6")
        compileOnly("com.github.retrooper:packetevents-spigot:2.11.0")

        implementation("org.bstats:bstats-bukkit:3.0.2")
    }

    tasks.processResources {
        filteringCharset = "UTF-8"
    }

    tasks.shadowJar {
        archiveBaseName.set("ZonePractice Lite")
        archiveClassifier.set("")

        relocate("org.bstats", "dev.nandi0813.practice.dependencies")
    }
}
