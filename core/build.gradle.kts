plugins {
    id("java")
    alias(libs.plugins.shadow)
    alias(libs.plugins.indra.git)
}

val buildNum = System.getenv("NEXT_BUILD_NUMBER") ?: "SNAPSHOT"
project.group = "net.pl3x.map.core"
project.version = "${libs.versions.minecraft.get()}-$buildNum"

java {
    withJavadocJar()
    withSourcesJar()
}

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
    implementation(project(":webmap"))

    compileOnly(libs.log4j)

    implementation(libs.jetbrainsAnnotations)

    implementation(libs.undertow)

    compileOnly(libs.cloudCore)
    compileOnly(libs.cloudProcessorsConfirmation)
    compileOnly(libs.cloudMinecraftExtras)

    compileOnly(libs.bundles.adventure)
    compileOnly(libs.adventurePlatformFacet)

    implementation(libs.caffeine)
    implementation(libs.querzNbt)
    implementation(libs.lz4Java)
    implementation(libs.simpleYaml) {
        exclude("org.yaml", "snakeyaml")
    }

    // provided by mojang
    compileOnly(libs.gson)
    compileOnly(libs.guava)
}

tasks {
    shadowJar {
        archiveClassifier = ""

        mergeServiceFiles()
        exclude(
            "META-INF/LICENSE",
            "META-INF/LICENSE.txt"
        )

        arrayOf(
            "com.github.benmanes.caffeine.cache",
            "com.github.Carleslc.Simple-YAML",
            "com.google.errorprone.annotations",
            "com.luciad",
            "io.undertow",
            "net.querz",
            "net.jpountz",
            "org.checkerframework",
            "org.jboss",
            "org.simpleyaml",
            "org.wildfly",
            "org.xnio",
            "org.yaml.snakeyaml",
        ).forEach { it -> relocate(it, "libs.$it") }

        manifest {
            attributes["Main-Class"] = "${project.group}.Pl3xMap"
            attributes["Git-Commit"] = (if (indraGit.isPresent) indraGit.commit()?.name() ?: "" else "").substring(0, 7)
        }
    }

    build {
        dependsOn(shadowJar)
    }

    javadoc {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        title = "${rootProject.name}-${project.version} API"
    }
}
