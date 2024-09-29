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

//    implementation(libs.cloudCore)
//    implementation(libs.cloudProcessorsConfirmation)
//    implementation(libs.cloudMinecraftExtras)
//
//    implementation(libs.bundles.adventure)

    compileOnly(libs.cloudBrigadier)
    compileOnly(libs.cloudPaper)

    compileOnly(libs.adventurePlatformBukkit)

    paperweight.paperDevBundle(libs.versions.bukkit)
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

//        arrayOf(
//            "org.incendo",
//            "io.leangen.geantyref",
//            "net.kyori",
//        ).forEach { it -> relocate(it, "libs.$it") }

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
            "website" to project.properties["website"],
            "cloud" to libs.versions.cloud.asProvider().get(),
            "cloudProcessors" to libs.versions.cloud.processors.get(),
            "cloudMinecraft" to libs.versions.cloud.minecraft.asProvider().get(),
            "adventure" to libs.versions.adventure.get(),
            "adventureBukkit" to libs.versions.adventureBukkit.get()
        ))

        filesMatching("plugin.yml") {
            expand(inputs.properties)
        }
    }
}
