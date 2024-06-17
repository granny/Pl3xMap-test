pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        //maven("https://maven.minecraftforge.net/")
    }
}

rootProject.name = "Pl3xMap"

include("core")
include("bukkit")
include("fabric")
//include ("forge")
include("webmap")
