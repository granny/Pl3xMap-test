<div align="center">

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/granny/Pl3xMap/v3/webmap/public/images/og.png">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/granny/Pl3xMap/v3/webmap/public/images/og.png">
  <img src="https://raw.githubusercontent.com/granny/Pl3xMap/v3/webmap/public/images/og.png" alt="Pl3xMap">
</picture>

# Pl3xMap

[![Build Status](https://img.shields.io/github/actions/workflow/status/granny/Pl3xMap/build.yml?event=push&logo=travis)](https://github.com/granny/Pl3xMap/actions)
[![Downloads](https://img.shields.io/modrinth/dt/pl3xmap?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABhWlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AcxV9bxSIVh1YQcYhQnSyIiuimVShChVArtOpgcukXNGlIUlwcBdeCgx+LVQcXZ10dXAVB8APE0clJ0UVK/F9SaBHjwXE/3t173L0D/PUyU82OMUDVLCOViAuZ7KrQ9YogwujDEGYkZupzopiE5/i6h4+vdzGe5X3uz9Gj5EwG+ATiWaYbFvEG8dSmpXPeJ46woqQQnxOPGnRB4keuyy6/cS447OeZESOdmieOEAuFNpbbmBUNlXiSOKqoGuX7My4rnLc4q+Uqa96TvzCU01aWuU5zEAksYgkiBMioooQyLMRo1UgxkaL9uId/wPGL5JLJVQIjxwIqUCE5fvA/+N2tmZ8Yd5NCcaDzxbY/hoGuXaBRs+3vY9tunACBZ+BKa/krdWD6k/RaS4seAb3bwMV1S5P3gMsdoP9JlwzJkQI0/fk88H5G35QFwrdA95rbW3Mfpw9AmrpK3gAHh8BIgbLXPd4dbO/t3zPN/n4Ax9dyyerighsAAAAGYktHRAAAAAAAAPlDu38AAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfmCBMVIw496dpyAAACm0lEQVQ4y2XTTYjVdRTG8c/vd//33sQJZyEVrYZeNJ1BSgKTsVbekRRbZVSrIqHwNUxtFVkboXyBQnORK6kICXQxSaOG0oSQiJAz4xuaUWvvCFdr7p37Py0Uszyrs3j4HnjO8yT/n4mhATlWS5YKfSC5JhxTpi/NHxm7V57ubuOrairN3aR30BRpWI4rIDyB5eiVfGG69z39B9v/AsZX1VQmj+AF4UM9rd2m63XtYsBUfdyC4aY/Fs9wc+Ym0jac1O1drv9gu4A7l5+nXInTWg/uk+I1VNWnHnexscUtj5rOm1TLs8JhxY0d2JBNDA2Q3ha2mS5+kfKoFK8I26W8yJxjv+Gq8KqiHDVVO0X6SMQa48v6sxyrMamntVu13IU+kZaoxS5RLnRp6T5FHBRpEI+ptXeK+Ik0pVJ+loUGvjddrwuvC58oKmM66RD2CrM0m7fMGzkj4lvhTckJoi48k1xovKRMV+WYhVEpL6KcLQyTVntqZP/tT6yqOD+5TDZMfpfubGzM9+UgpwqpDzeleNr5xpALjbMuTu4krt+GxWkpdSAL2+V4XzXGiI6y+52IPZJzwjrJD5IeRdopW4G2ojKmdBOXs+QoVujkvaSqiEfktNHco4tFvKxMQ2LGQtPxMGkLDmj3/K3s/Vyr+Vwy1hhUGJXiL/LHyvINKfURnyoNKyuXFOU8HBeueKAzqF3dIKzTrs3NBo7+jBGRqsSvOvXF+Jq0VU6nFOVWZfdP4iud2hKdYlD4AN9YMNy8bWK3dyVOCofVOmvNbK1VjYewRDcfMP/H37VmrFdvr1emQyKd0Lq++f4yFTd2iFiDSRyRXL7j+hzSi5iFPVrXN3v2TOe/gLugZf1y9y0pNbhTZ64JI4pyvyePT9wr/wdL6hgY+Xe2UgAAAABJRU5ErkJggg==)](https://modrinth.com/mod/pl3xmap/)
[![Join us on Discord](https://img.shields.io/discord/1166280318349758464.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://granny.dev/discord)

[![MIT License](https://img.shields.io/github/license/granny/Pl3xMap?&logo=github)](LICENSE)
[![CodeFactor Grade](https://img.shields.io/codefactor/grade/github/granny/Pl3xMap?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8%2F9hAAABhWlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AcxV9bxSIVh1YQcYhQnSyIiuimVShChVArtOpgcukXNGlIUlwcBdeCgx%2BLVQcXZ10dXAVB8APE0clJ0UVK%2FF9SaBHjwXE%2F3t173L0D%2FPUyU82OMUDVLCOViAuZ7KrQ9YogwujDEGYkZupzopiE5%2Fi6h4%2BvdzGe5X3uz9Gj5EwG%2BATiWaYbFvEG8dSmpXPeJ46woqQQnxOPGnRB4keuyy6%2FcS447OeZESOdmieOEAuFNpbbmBUNlXiSOKqoGuX7My4rnLc4q%2BUqa96TvzCU01aWuU5zEAksYgkiBMioooQyLMRo1UgxkaL9uId%2FwPGL5JLJVQIjxwIqUCE5fvA%2F%2BN2tmZ8Yd5NCcaDzxbY%2FhoGuXaBRs%2B3vY9tunACBZ%2BBKa%2FkrdWD6k%2FRaS4seAb3bwMV1S5P3gMsdoP9JlwzJkQI0%2Ffk88H5G35QFwrdA95rbW3Mfpw9AmrpK3gAHh8BIgbLXPd4dbO%2Ft3zPN%2Fn4Ax9dyyerighsAAAAGYktHRAAAAAAAAPlDu38AAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfmCBMVKAA5pS6%2BAAABlElEQVQ4y82PP2gVQRDGf7N3t%2Bvdixpi0N5OELFKJ1iohBciKlgYJLX6YkBbC0sVooVFBAvBPw%2BFZzrJs7DR2iYHRhBsxNI8VLwUx92MRXJGxKCp9AfL7DfDfPutFO3z5wy5DuRlWU2OvLj7hduLYXh0ZSEkOh4SjUKiBK%2BEZP34Gu%2FtbebLE86Qa8BO4FDwyWmAbPjzMWACiNgEMdun6macwfJ6z2qxZYBI6ndAxR%2BRN%2FL1ZGeXlDqFkm%2Fv33nZjHZ0u2OZrw%2F7pBYf16Re8UEJ8VpNE33fP3BxgX%2BOFOOdtjmuGpoPtT51pNcrMZORx4%2FmslQnslAlWahItymZrz%2Bmqc4%2B2z%2B71BjE5uwesEeQsaLY%2FQp42LrfPUqwy2DNO03ZK9hN4Ehj4IDBjzjKCoC5aMDG9q%2BhBz%2BrWCN3KqptBtG89Xx%2BEWB1%2Bszr8OTBFMgkSLKWQAA%2BVCU3%2BK%2BQb%2B0LB4FLGHmrP39LNv3773Ei9IBphLnVduf4VhM4M9JGqGzc%2F5bYnDsrqlcQloaK0adbNfgOUn6NRlZZ46YAAAAASUVORK5CYII%3D)](https://www.codefactor.io/repository/github/granny/Pl3xMap)

[![bStats Servers](https://img.shields.io/bstats/servers/10133)](https://bstats.org/plugin/bukkit/Pl3xMap/10133)
[![Stargazers](https://img.shields.io/github/stars/granny/Pl3xMap?label=stars&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABhWlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AcxV9bxSIVh1YQcYhQnSyIiuimVShChVArtOpgcukXNGlIUlwcBdeCgx+LVQcXZ10dXAVB8APE0clJ0UVK/F9SaBHjwXE/3t173L0D/PUyU82OMUDVLCOViAuZ7KrQ9YogwujDEGYkZupzopiE5/i6h4+vdzGe5X3uz9Gj5EwG+ATiWaYbFvEG8dSmpXPeJ46woqQQnxOPGnRB4keuyy6/cS447OeZESOdmieOEAuFNpbbmBUNlXiSOKqoGuX7My4rnLc4q+Uqa96TvzCU01aWuU5zEAksYgkiBMioooQyLMRo1UgxkaL9uId/wPGL5JLJVQIjxwIqUCE5fvA/+N2tmZ8Yd5NCcaDzxbY/hoGuXaBRs+3vY9tunACBZ+BKa/krdWD6k/RaS4seAb3bwMV1S5P3gMsdoP9JlwzJkQI0/fk88H5G35QFwrdA95rbW3Mfpw9AmrpK3gAHh8BIgbLXPd4dbO/t3zPN/n4Ax9dyyerighsAAAAGYktHRAAAAAAAAPlDu38AAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfmCBMVNjtc7/hFAAABIElEQVQ4y62SzS5DURSFv6smXkAUCRU0UdKYGNTPyCsYYOYFGGi8Ao9QM0PxCh6CgQ4qfiLpBFEjdKCfySaXtDch1uScs9Ze62TvcyAD6o66zV+gjqpvalsd61XXl5GxBySx3/3t7UPqi1pTD9VXdaRbbZIyDQLTwBSwBqzGGaABnAInwCXQSJLk/tO4orb8jra6nwo/CC6NlrqMOq421Y5aVSfUXJe2cqFVo7b5NdwIuVaf1IWM2cyrD+qdOvlTLERIS53pYi6FdqMWet2wGP1tdNE2Q1vK+gfDsdbDlFfzwV3Ems8KmAXegcd4hSvgVq0Bz6GV0ob+HgF1YAA4Cn4LWA9tLusHnscTHavFFF8MrqOeZQVU1HKGXlYr/Cc+AKuOI2h/Jrf7AAAAAElFTkSuQmCC)](https://github.com/granny/Pl3xMap/stargazers)
[![Forks](https://img.shields.io/github/forks/granny/Pl3xMap?label=forks&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABhWlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AcxV9bxSIVh1YQcYhQnSyIiuimVShChVArtOpgcukXNGlIUlwcBdeCgx+LVQcXZ10dXAVB8APE0clJ0UVK/F9SaBHjwXE/3t173L0D/PUyU82OMUDVLCOViAuZ7KrQ9YogwujDEGYkZupzopiE5/i6h4+vdzGe5X3uz9Gj5EwG+ATiWaYbFvEG8dSmpXPeJ46woqQQnxOPGnRB4keuyy6/cS447OeZESOdmieOEAuFNpbbmBUNlXiSOKqoGuX7My4rnLc4q+Uqa96TvzCU01aWuU5zEAksYgkiBMioooQyLMRo1UgxkaL9uId/wPGL5JLJVQIjxwIqUCE5fvA/+N2tmZ8Yd5NCcaDzxbY/hoGuXaBRs+3vY9tunACBZ+BKa/krdWD6k/RaS4seAb3bwMV1S5P3gMsdoP9JlwzJkQI0/fk88H5G35QFwrdA95rbW3Mfpw9AmrpK3gAHh8BIgbLXPd4dbO/t3zPN/n4Ax9dyyerighsAAAAGYktHRAAAAAAAAPlDu38AAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfmCBMVNCYN3/YeAAAA/UlEQVQ4y7WTQUoDQRBFf01czlJcxUyOINGjjAvFHMFzZGdygOwDwTtk6UZcqLlAxCAuMigug89FamIzdAIN+qGhq/6v6qrqbumvAJwBj8AHMAQs4DJgBHy65jSW4Bl4AaZsUAbcufumrnmquSzIcSzpTtLA7XbA1fuBa9qxCob8YgUUAdcFqoC/iSXIgLELOhG+49w4nM+2BTP7ljR3M4/MufbNzYxdN1E0Sm2ialZnsVIllZKOJF24eyLpXdKtmS1S3sYMmO3THOwJziUdbrbkZvaVcnILeAh6vweylAQ9D7z2BXCS0sJS0lrSpdtrSW+pn6sPLIFX4Er/hR9C0wl1FTBzNwAAAABJRU5ErkJggg==)](https://github.com/granny/Pl3xMap/network/members)
[![Watchers](https://img.shields.io/github/watchers/granny/Pl3xMap?label=watchers&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABhWlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AcxV9bxSIVh1YQcYhQnSyIiuimVShChVArtOpgcukXNGlIUlwcBdeCgx+LVQcXZ10dXAVB8APE0clJ0UVK/F9SaBHjwXE/3t173L0D/PUyU82OMUDVLCOViAuZ7KrQ9YogwujDEGYkZupzopiE5/i6h4+vdzGe5X3uz9Gj5EwG+ATiWaYbFvEG8dSmpXPeJ46woqQQnxOPGnRB4keuyy6/cS447OeZESOdmieOEAuFNpbbmBUNlXiSOKqoGuX7My4rnLc4q+Uqa96TvzCU01aWuU5zEAksYgkiBMioooQyLMRo1UgxkaL9uId/wPGL5JLJVQIjxwIqUCE5fvA/+N2tmZ8Yd5NCcaDzxbY/hoGuXaBRs+3vY9tunACBZ+BKa/krdWD6k/RaS4seAb3bwMV1S5P3gMsdoP9JlwzJkQI0/fk88H5G35QFwrdA95rbW3Mfpw9AmrpK3gAHh8BIgbLXPd4dbO/t3zPN/n4Ax9dyyerighsAAAAGYktHRAAAAAAAAPlDu38AAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfmCBMVNw4TRw0nAAAA3UlEQVQ4y83SP04CURAG8I0lewHOwAFUaiwkdmAlp8CL4FHopfIvtOIJWE3opIBK489mQPKy6xYWOskkL9/MN/PNzMuyf2fIcYkZVuGzwPI68gle8Yl7jMIfAntBp4o8wAeecFgSP8I8cgZp8DwC12j8oLCBCd7R34ItbHCzT8ZZSC7QTYrcYo1WhjGWaCbdCt+2SGLN4IwPfnu07QjrkhG6oWKB0+TMd7sRAuzHYuqWmO8tsVd1xjmOS8htPEfORVWHTmweHnEVPg2sqPxIicxhFFjhLd7D2q/8J/YFHSJt9VSqQ08AAAAASUVORK5CYII=)](https://github.com/granny/Pl3xMap/watchers)

<big><b>Pl3xMap is the <u>original</u> minimalistic and lightweight world map viewer  
for Minecraft servers using the vanilla Minecraft rendering style.</b></big>

</div>

## Features

* Ultra-fast render times. Get your map rendered in minutes, not next week.
* Simple vanilla-like top down 2D view, designed for navigation.
* Player markers showing yaw rotation, health, and armor.
* [Addons and integrations](https://github.com/granny/Pl3xMap/wiki/pl3xmap-integrations) for many popular plugins.
* Multiple render types including built-in basic, night, biomes, inhabited, and flowermap.
* Easy to [set up](https://github.com/granny/Pl3xMap/wiki/Installation) and [configure](https://github.com/granny/Pl3xMap/wiki/Configuration).
* Up-to-date Leaflet front-end for a better browser experience.
* [Robust API](https://github.com/granny/Pl3xMap/wiki/Pl3xMap-API) to create your own addons and/or integrations with ease.

## Supported Platforms

* [CraftBukkit](https://dev.bukkit.org/)
* [Spigot](https://spigotmc.org/)
* [Paper](https://papermc.io/)
* [Purpur](https://purpurmc.org/)
* [Fabric](https://fabricmc.net/) (requires [Fabric API](https://modrinth.com/mod/fabric-api))
<!-- * [Forge](https://minecraftforge.net/) -->

## Downloads

Downloads are available on Modrinth.

[![Download on Modrinth](https://i.imgur.com/5C4fVJC.png)](https://modrinth.com/mod/pl3xmap)

## Demo

You can demo Pl3xMap (and it's top competitors) at:

https://map.roanv.nl/

## Support the Developers

granny on:
* [GitHub Sponsors](https://github.com/sponsors/granny)

BillyGalbreath on:
* [Ko-Fi](https://ko-fi.com/billygalbreath)

JLyne on:
* [Ko-Fi](https://ko-fi.com/jlyne)

## bStats

[![bStats Graph Data](https://bstats.org/signatures/bukkit/Pl3xMap.svg)](https://bstats.org/plugin/bukkit/Pl3xMap/10133)

## Developers

Maven

```xml
<repository>
  <id>modrinth-repo</id>
  <url>https://api.modrinth.com/maven/</url>
</repository>

<dependency>
  <groupId>maven.modrinth</groupId>
  <artifactId>pl3xmap</artifactId>
  <version>1.21.3-509</version>
  <scope>provided</scope>
</dependency>
```

Gradle

```groovy
repositories {
    exclusiveContent {
        forRepository { maven { url = "https://api.modrinth.com/maven" } }
        filter { includeGroup "maven.modrinth" }
    }
}

dependencies {
    compileOnly 'maven.modrinth:pl3xmap:1.21.3-509'
}
```

## Building from source

Then just run the following command:

```
./gradlew build
```

The compiled jars will be in `build/libs/`
