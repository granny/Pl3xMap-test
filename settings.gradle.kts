pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
    }
}

rootProject.name = "Pl3xMap"

include("core")
include("bukkit")
include("fabric")
include ("neoforge")
include("webmap")
