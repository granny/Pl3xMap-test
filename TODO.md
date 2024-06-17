# TODO

## General

- [x] move versions to `gradle/libs.versions.toml` 
- [ ] Share logic in [`buildSrc`](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html#sec:using_buildsrc)

## Individual project jars

- [ ] bukkit: should dev jar include missing core dependency?
- [ ] fabric: devlibs jar is missing core dependency

## uber-jar shenanigans
- [x] fix missing `jars/` directory in `META-INF`
- [x] fix incorrect `MANIFEST.MF`
