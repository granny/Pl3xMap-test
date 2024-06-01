plugins {
    id("java-library")
    alias(libs.plugins.minotaur)
}

val buildNum = System.getenv("NEXT_BUILD_NUMBER") ?: "TEMP" // TODO: Temp
project.version = "${libs.versions.minecraft.get()}-$buildNum"

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        subprojects {
            dependsOn(project.tasks.build)
        }

        archiveClassifier = ""

        // this is janky, but it works
        val manifestFiles = subprojects.filter({ it.name != "webmap" && it.name != "core" }).map {
            val regularFile = it.layout.buildDirectory.file("libs/${project.name}-${it.name}-${it.version}.jar")
            if (regularFile.isPresent) {
                zipTree(regularFile)
            } else {
                null
            }
        }.filterNotNull()

        from(manifestFiles) {
            exclude("META-INF/MANIFEST.MF")
        }

        // this is janky, but it works
        doFirst {
            manifestFiles.forEach {
                it.matching { include("META-INF/MANIFEST.MF") }.files.forEach {
                    manifest.from(it)
                }
            }
        }
    }
}

modrinth {
    autoAddDependsOn = false
    token = System.getenv("MODRINTH_TOKEN")
    projectId = "pl3xmap"
    versionName = "${project.version}"
    versionNumber = "${project.version}"
    versionType = "beta"
    uploadFile = rootProject.layout.buildDirectory.file("libs/${rootProject.name}-${project.version}.jar").get()
    //additionalFiles.addAll([
    //        rootProject.layout.buildDirectory.file("libs/${rootProject.name}-${project.version}-javadoc.jar").get(),
    //        rootProject.layout.buildDirectory.file("libs/${rootProject.name}-${project.version}-sources.jar").get()
    //])
    gameVersions.addAll(listOf(libs.versions.minecraft.get()))
    loaders.addAll(listOf("bukkit", "fabric", /*"forge",*/ "paper", "purpur", "quilt", "spigot", "folia"))
    changelog = System.getenv("COMMIT_MESSAGE")
    dependencies {
        required.project("fabric-api")
         //optional.project(
         //    "pl3xmap-banners",
         //    "pl3xmap-claims",
         //    "pl3xmap-mobs",
         //    "pl3xmap-signs",
         //    "pl3xmap-warps",
         //    "deathspots",
         //)
    }
}
