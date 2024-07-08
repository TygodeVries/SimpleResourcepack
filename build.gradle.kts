plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.thesheep"
version = "2.6"

val earliestSupportedAPIVersion = "1.16"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    implementation("org.bstats:bstats-bukkit:3.0.2")
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(8)
}

// Default build task depends on all versioned jar tasks
tasks.named("build") {
    dependsOn(tasks.shadowJar)
}

// Disable the default jar task
tasks.jar {
    enabled = false
}

tasks.shadowJar {
    filteringCharset = "UTF-8"

    // Remove the suffix from the jar file
    archiveClassifier.set("")

    // Shade the dependencies
    configurations = listOf(project.configurations["runtimeClasspath"])
    from(sourceSets.main.get().output)
    relocate("org.bstats", "dev.thesheep")

    // Add the version to the plugin.yml
    filesMatching("**/plugin.yml") {
        expand(
            "version" to project.version,
            "apiVersion" to earliestSupportedAPIVersion
        )
    }
}
