import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

plugins {
    scala
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("com.modrinth.minotaur") version "2.2.0"
    id("net.minecraftforge.gradle") version "5.1.64"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

version = project.property("mod.version") as String
if (version.toString().endsWith("-snapshot")) {
    version = version.toString() + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
}

group = project.properties["mod.group"] as String
//application.mainClass.set(project.property("mod.group") as String)

fun getGitRef(): String {
    return try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim()
    } catch (e: Throwable) {
        "unknown"
    }
}


version = version.toString() + "+" + getGitRef()

version = "MC${project.property("minecraft.version")}-${project.version}"

val mappingsChannel = project.provider { project.properties["minecraft.mappings_channel"] as String }
val mappingsVersion = project.provider { project.properties["minecraft.mappings_version"] as String }
minecraft {

    mappings(mappingsChannel.get(), mappingsVersion.get())

    runs {
        create("client") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "info")

            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")

            mods {
                create("opencomputers") {
                    source(sourceSets.main.get())
                }
            }
        }

        create("server") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "info")

            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")

            mods {
                create("opencomputers") {
                    source(sourceSets.main.get())
                }
            }
        }
    }
}

/* Do not pull this in on IDEA, as it changes the compiler"s source path, making navigating to errors harder */
//if (!System.getProperty("idea.active")) {
//    compileScala {
//        source = replaceSourceTokensScala.outputs
//    }
//}

//compileScala {
//    configure(scalaCompileOptions.forkOptions) {
//        memoryMaximumSize = "1g"
//    }
//}

repositories {
    maven {
        name = "MightyPirates"
        url = uri("https://maven.cil.li/")
        metadataSources {
            mavenPom()
            artifact()
        }
    }
    maven {
        name = "SquidDev" /* CC: Tweaked */
        url = uri("https://squiddev.cc/maven/")
    }
    ivy {
        name = "asie dependency mirror"
        artifactPattern("https://asie.pl/javadeps/[module]-revision.[ext]")
        content {
            includeModule("", "OC-LuaJ")
            includeModule("", "OC-JNLua")
            includeModule("", "OC-JNLua-Natives")
        }
        metadataSources {
            artifact()
        }
    }
    maven {
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
    maven {
        url = uri("https://modmaven.dev")
        content {
            includeGroup("mezz.jei")
        }
    }
    maven {
        url = uri("https://chickenbones.net/maven/")
        content {
            includeGroup("codechicken")
            includeGroup("mrtjp")
        }
    }
    maven {
        url = uri("https://modmaven.dev/")
        content {
            includeGroup("appeng")
            includeGroup("mekanism")
        }
    }
    maven {
        url = uri("https://proxy-maven.covers1624.net/")
        content {
            includeModule("net.minecraftforge", "Scorge")
        }
    }
    mavenCentral()
}

//configurations {
//    create("embedded")
//    getByName("compileOnly").extendsFrom(getByName("provided"))
//    getByName("implementation").extendsFrom(getByName("embedded"))
//}


dependencies {
    val jeiSlug = "jei-${project.property("minecraft.version")}"
    minecraft("net.minecraftforge:forge:${project.property("minecraft.version")}-${project.property("forge.version")}")

    // required for tests but cannot use implementation as that would clash with scorge at runtime
    compileOnly("org.scala-lang:scala-library:2.13.4")
    implementation("net.minecraftforge:Scorge:${project.property("scorge.version")}")
    shadow("com.typesafe:config:1.2.1")

    compileOnly(fg.deobf("li.cil.tis3d:tis3d-1.18.2-forge:${project.property("tis3d.version")}"))
    compileOnly(fg.deobf("curse.maven:hwyla-${project.property("hwyla.projectId")}:${project.property("hwyla.fileId")}"))
    compileOnly(fg.deobf("org.squiddev:cc-tweaked-${project.property("minecraft.version")}:${project.property("cct.version")}"))
    compileOnly(fg.deobf("curse.maven:cofh-core-69162:4022663")) /* CoFHCore */
    compileOnly(fg.deobf("curse.maven:thermal-foundation-222880:4022666")) /* Thermal Foundation */

    runtimeOnly("curse.maven:modernfix-790626:4728410")

    compileOnly(fg.deobf("appeng:appliedenergistics2:${project.property("ae2.version")}:api"))

    compileOnly(fg.deobf("mekanism:Mekanism:${project.property("mekanism.version")}:api"))

    compileOnly(fg.deobf("codechicken:CBMultipart:${project.property("cbmultipart.version")}:universal"))

    compileOnly(fg.deobf("codechicken:ChickenASM:${project.property("casm.version")}"))
    compileOnly(fg.deobf("codechicken:CodeChickenLib:${project.property("ccl.version")}:universal"))
    compileOnly(fg.deobf("codechicken:EnderStorage:${project.property("enderstorage.version")}:universal"))

    compileOnly(fg.deobf("mezz.jei:${jeiSlug}:${project.property("jei.version")}"))

    compileOnly(fg.deobf("mrtjp:ProjectRed:${project.property("projred.version")}:core"))

    compileOnly(fg.deobf("mrtjp:ProjectRed:${project.property("projred.version")}:integration"))

    shadow(mapOf("name" to "OC-LuaJ", "version" to "20220907.1", "ext" to "jar"))
    shadow(mapOf("name" to "OC-JNLua", "version" to "20230530.0", "ext" to "jar"))
    shadow(mapOf("name" to "OC-JNLua-Natives", "version" to "20220928.1", "ext" to "jar"))

    testImplementation("org.scala-lang:scala-library:2.13.4")
    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:3.4.0")
    testImplementation("org.scalactic:scalactic_2.13:3.2.6")
    testImplementation("org.scalatest:scalatest_2.13:3.2.6")
    testImplementation("org.scalatestplus:junit-4-13_2.13:3.2.6.+")
    testImplementation("org.scalatestplus:mockito-3-4_2.13:3.2.6.+")

}

tasks {
    register<Sync>("replaceSourceTokensScala") {
        from(sourceSets.main.get().scala.srcDirs)
        into("$buildDir/srcReplaced/scala")
        expand("VERSION" to project.property("mod.version"),
                "MCVERSION" to project.property("minecraft.version"))
    }

    named<Copy>("processResources") {
        val reducedScorgeVer = project.property("scorge.version").toString().replace(Regex("(\\d+\\.\\d+)(\\.\\d+)"), "\$1")

        inputs.property("version", "${project.property("mod.version")}")
        inputs.property("mcversion", project.property("minecraft.version"))
        inputs.property("sversion", reducedScorgeVer)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        filesMatching(listOf("META-INF/mods.toml")) {
            expand("version" to "${project.property("mod.version")}", "mcversion" to project.property("minecraft.version"), "sversion" to reducedScorgeVer)
        }
    }

    named<Jar>("jar") {
        val reducedScorgeVer = project.property("scorge.version").toString().replace(Regex("(\\d+\\.\\d+)(\\.\\d+)"), "\$1")

        inputs.property("version", "${project.property("mod.version")}")
        inputs.property("mcversion", project.property("minecraft.version"))
        inputs.property("sversion", reducedScorgeVer)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        filesMatching(listOf("META-INF/mods.toml")) {
            expand("version" to "${project.property("mod.version")}", "mcversion" to project.property("minecraft.version"), "sversion" to reducedScorgeVer)
        }

//        configurations["embedded"].forEach { dep ->
//            from(project.zipTree(dep)) {
//                exclude("*", "META-INF", "META-INF/**")
//            }
//        }
        manifest {
            attributes(
                    mapOf(
                            "Specification-Title" to "opencomputers",
                            "Specification-Vendor" to "li.cil.oc",
                            "Specification-Version" to "1",
                            "Implementation-Title" to project.name,
                            "Implementation-Version" to version,
                            "Implementation-Vendor" to project.property("mod.group"),
                            "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
                    )
            )
        }
    }

//    named<Javadoc>("javadoc") {
//        include("li/cil/oc/api/**")
//    }

    register<Jar>("apiJar") {
        from(sourceSets.main.get().allSource)
        from(sourceSets.main.get().output)
        classifier = "api"
        include("li/cil/oc/api/**")
    }

//    register<Jar>("javadocJar") {
//        dependsOn(tasks.named<Javadoc>("javadoc"))
//        from(tasks.named<Javadoc>("javadoc").get().destinationDir)
//        classifier = "javadoc"
//    }
}

artifacts {
    archives(tasks.named("apiJar"))
//    archives(tasks.named("javadocJar"))
}

//publishing {
//    publications {
//        create<MavenPublication>("mavenJava") {
//            groupId = project.group.toString()
//            artifactId = project.name
//            version = project.property("mod.version").toString()
//            artifact(tasks.named("jar"))
//            artifact(tasks.named("apiJar"))
//            artifact(tasks.named("javadocJar"))
//        }
//    }
//    repositories {
//        maven {
//            name = "GitHubPackages"
//            url = uri(System.getenv("GITHUB_MAVEN_URL") ?: "")
//            credentials {
//                username = System.getenv("GITHUB_ACTOR")
//                password = System.getenv("GITHUB_TOKEN")
//            }
//        }
//    }
//}

//curseforge {
//    apiKey = System.getenv("CURSEFORGE_API_KEY") ?: ""
//
//    project(closureOf<com.matthewprenger.cursegradle.CurseProject> {
//        id = project.property("curse.project.id").toString()
//        releaseType = System.getenv("CURSEFORGE_RELEASE_TYPE") ?: "alpha"
//        changelogType = "markdown"
//        changelog = System.getenv("CHANGELOG") ?: "Changelog not available."
//        gameVersionStrings.add(project.property("minecraft.version").toString())
//        gameVersionStrings.add("Java 17")
//        gameVersionStrings.add("Forge")
//
//        mainArtifact(tasks.named<Jar>("jar").get().archiveFile.get().asFile, closureOf<com.matthewprenger.cursegradle.CurseArtifact> {
//            displayName = "Your Project Name ${project.version}"
//        })
//    })
//}

//modrinth {
//    token.set(System.getenv("MODRINTH_API_KEY") ?: "")
//    projectId.set(project.property("modrinth.project.id").toString())
//    versionName.set("${project.rootProject.name}-${project.version}")
//    versionNumber.set(project.property("mod.version").toString())
//    versionType.set(System.getenv("MODRINTH_RELEASE_TYPE") ?: "alpha")
//    changelog.set(System.getenv("CHANGELOG") ?: "Changelog not available.")
//    gameVersions.add(project.property("minecraft.version").toString())
//    loaders.add("forge")
//    uploadFile.set(tasks.named<Jar>("jar").get().archiveFile.get().asFile)
//}
