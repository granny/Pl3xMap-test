plugins {
    id("java")
    alias(libs.plugins.paperweight.userdev)
    alias(libs.plugins.shadow)
}

val buildNum = System.getenv("NEXT_BUILD_NUMBER") ?: "SNAPSHOT"
project.version = "${libs.versions.minecraft.get()}-$buildNum"
project.group = "net.pl3x.map.bukkit"

base {
    archivesName = "${rootProject.name}-${project.name}"
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/") {
        name = "oss-sonatype-snapshots"
        mavenContent {
            snapshotsOnly()
        }
    }
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "s01-sonatype-snapshots"
        mavenContent {
            snapshotsOnly()
        }
    }
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":core", configuration = "shadow"))

    implementation(libs.cloudBrigadier)
    implementation(libs.cloudPaper)

    implementation(libs.adventurePlatformBukkit)

    paperweight.paperDevBundle(libs.versions.bukkit)
}

tasks {
    reobfJar {
        dependsOn(shadowJar)

        outputJar.set(jar.get().archiveFile)
    }

    // needed for below jank
    compileJava {
        dependsOn(":core:jar")
    }

    shadowJar {
        mergeServiceFiles()

        // this is janky, but it works
        manifest {
            from(project(":core").tasks.named<Jar>("shadowJar").get().manifest)
        }
    }

    build {
        dependsOn(reobfJar)
    }

    processResources {
        inputs.properties(mapOf(
            "name" to rootProject.name,
            "group" to project.group,
            "version" to project.version,
            "authors" to project.properties["authors"],
            "description" to project.properties["description"],
            "website" to project.properties["website"]
        ))

        filesMatching("plugin.yml") {
            expand(inputs.properties)
        }
    }
}
