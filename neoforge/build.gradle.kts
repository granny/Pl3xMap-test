import net.neoforged.gradle.common.tasks.JarJar

plugins {
    alias(libs.plugins.neoforged.userdev)
}

val buildNum = System.getenv("NEXT_BUILD_NUMBER") ?: "SNAPSHOT"
project.version = "${libs.versions.minecraft.get()}-$buildNum"
project.group = "net.pl3x.map.neoforge"

val shade: Configuration by configurations.creating

base {
    archivesName = "${rootProject.name}-${project.name}"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-snapshots"
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
    maven("https://maven.fabricmc.net/")
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.neoforge.loader)
    implementation(project(path = ":core", configuration = "shadow"))
    shade(project(path = ":core", configuration = "shadow"))
    implementation("net.kyori:adventure-text-serializer-gson:4.17.0")

    implementation(libs.cloudNeoforge)
    jarJar(libs.cloudNeoforge)
}

jarJar.enable()

minecraft {
    accessTransformers {
        files("src/main/resources/META-INF/accesstransformer.cfg")
    }
}

tasks {
    val jarJarTask = named<JarJar>("jarJar") {
        archiveClassifier = ""

        val fileTreeProvider = shade.elements.map { zipTree(it.single()) }
        from(fileTreeProvider)

        // this is jank but it works
        doFirst {
            manifest {
                from(fileTreeProvider.get().matching { include("META-INF/MANIFEST.MF") }.files)
            }
        }
    }

    jar {
        archiveClassifier = "original"
    }

    // needed for above jank
    compileJava {
        dependsOn(":core:jar")
    }

    build {
        dependsOn(jarJarTask)
    }

    sourceSets {
        main {
            resources {
                srcDirs("src/generated/resources")
            }
        }
    }

    processResources {
        inputs.properties(mapOf(
            "name" to rootProject.name,
            "version" to project.version,
            "authors" to project.properties["authors"],
            "description" to rootProject.properties["description"],
            "website" to rootProject.properties["website"],
            "issues" to rootProject.properties["issues"],
            "minecraftVersion" to libs.versions.minecraft.get(),
            "neoforgeLoaderVersion" to libs.versions.neoforgeLoader.get(),
        ))

        filesMatching("META-INF/neoforge.mods.toml") {
            expand(inputs.properties)
        }
    }
}
