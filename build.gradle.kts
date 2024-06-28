import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.thesheep"
version = "2.5"

val versions = listOf("1_16" to "1.16.5", "1_20" to "1.20.4")

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

configurations {
    versions.forEach { (suffix, _) ->
        create("v$suffix")
    }
}

dependencies {
    // General dependencies
    implementation("org.bstats:bstats-bukkit:3.0.2")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    // Spigot API dependency (based on version)
    versions.forEach { (suffix, version) ->
        compileOnly("org.spigotmc:spigot-api:$version-R0.1-SNAPSHOT") {
            configurations["v$suffix"]
        }
    }
}

versions.forEach { (suffix, _) ->
    tasks.register<ShadowJar>("v${suffix}Jar") {
        filteringCharset = "UTF-8"

        // Add the version suffix to the jar file name
        archiveClassifier.set("v$suffix")

        // Shade the dependencies
        configurations = listOf(project.configurations["v$suffix"], project.configurations["runtimeClasspath"])
        from(sourceSets.main.get().output)
        relocate("org.bstats", "dev.thesheep")

        // Get API version from suffix
        val apiVersion = suffix.replace("_", ".")

        // Add the version and API version to the plugin.yml
        filesMatching("**/plugin.yml") {
            expand(
                "version" to project.version,
                "apiVersion" to apiVersion
            )
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(8)
}

// Default build task depends on all versioned jar tasks
tasks.named("build") {
    dependsOn(versions.map { (suffix, _) -> "v${suffix}Jar" })
}

// Disable the default jar task
tasks.jar {
    enabled = false
}
