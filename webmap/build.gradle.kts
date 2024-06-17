import com.github.gradle.node.npm.task.NpxTask

plugins {
    id("java")
    id("com.github.node-gradle.node") version("7.0.2")
}

tasks {
    clean {
        delete("$projectDir/dist")
    }

    val buildWebmap = register<NpxTask>("buildWebmap") {
        dependsOn(npmInstall)
        command = "webpack"

        inputs.files(
            listOf(
                "package.json",
                "package-lock.json",
                "tsconfig.json",
                "webpack.config.js",
            )
        )
        inputs.dir("src")
        inputs.dir("public")
//        inputs.dir(fileTree("node_modules").exclude(".cache"))
        outputs.dir("dist")
    }

    processResources {
        dependsOn(buildWebmap)

        doLast {
            val destinationDir = outputs.files.singleFile.resolve("web")
            outputs.files.singleFile.resolve("dist").renameTo(destinationDir)
        }
    }
}

sourceSets {
    main {
        java {
            resources {
                srcDir("$projectDir")
                include("dist/**")
            }
        }
    }
}
