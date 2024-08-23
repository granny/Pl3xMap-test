plugins {
    alias(libs.plugins.neoforged.userdev)
    alias(libs.plugins.shadow)
}

val buildNum = System.getenv("NEXT_BUILD_NUMBER") ?: "SNAPSHOT"
project.version = "${libs.versions.minecraft.get()}-$buildNum"
project.group = "net.pl3x.map.neoforge"

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
    assemble {
        dependsOn(shadowJar)
    }

    // needed for below jank
    compileJava {
        dependsOn(":core:jar")
    }

    shadowJar {
        mergeServiceFiles()

        dependencies {
            include(project(":core"))
        }

        // this is janky, but it works
        manifest {
            from(project(":core").tasks.named<Jar>("shadowJar").get().manifest)
        }
    }

    build {
        dependsOn(jarJar)
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
