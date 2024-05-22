plugins {
    id("java")
    alias(libs.plugins.paperweight.userdev)
    alias(libs.plugins.shadow)
}

val buildNum = System.getenv("NEXT_BUILD_NUMBER") ?: "TEMP" // TODO: Temp
project.version = "${rootProject.properties["minecraftVersion"]}-$buildNum"
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

    implementation("org.incendo:cloud-brigadier:${rootProject.properties["cloudVersion"]}")
    implementation("org.incendo:cloud-paper:${rootProject.properties["cloudVersion"]}")

    implementation("net.kyori:adventure-platform-bukkit:${rootProject.properties["adventureBukkitVersion"]}") // TODO: temp

    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:${rootProject.properties["bukkitVersion"]}")
}

tasks {
    reobfJar {
        dependsOn(shadowJar)
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
